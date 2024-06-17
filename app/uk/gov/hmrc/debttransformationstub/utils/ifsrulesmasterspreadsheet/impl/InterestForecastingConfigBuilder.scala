/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl

import play.api.libs.json.JsString

import java.util.Base64

object InterestForecastingConfigBuilder {
  def buildProductionConfig(ifsData: IfsRulesMasterData): Seq[String] =
    buildRules(ifsData)
      .map(_.raw)
      .distinct
      .zipWithIndex
      .flatMap { case (rule, index) =>
        List(
          s"# $rule,",
          s"service-config.rules.$index: ${JsString(Base64.getEncoder.encodeToString(rule.getBytes()))}"
        )
      }

  def buildAppConfig(ifsData: IfsRulesMasterData): Seq[String] =
    buildRules(ifsData).map(_.raw).distinct.flatMap { rule =>
      List(
        s"# $rule,",
        s"${JsString(Base64.getEncoder.encodeToString(rule.getBytes()))},"
      )
    }

  private def buildRules(ifsData: IfsRulesMasterData): Seq[InterestRule] =
    (0 until ifsData.length)
      .map { dataIndex =>
        val mainTransString = ifsData.Interpreted.mainTrans(dataIndex)
        val subTransString = ifsData.Interpreted.subTrans(dataIndex)

        val ifCondition: String = {
          val mainTransClause = s"mainTrans == '${mainTransString: String}'"

          val subTransClause = s"subTrans == '${subTransString: String}'"

          List(mainTransClause, subTransClause).mkString(" AND ")
        }

        val thenClauses: String = {
          val interestRateClause: String =
            s"intRate = ${ifsData.Interpreted.interestBearing(dataIndex): Int}"

          val interestOnlyDebtClause: String =
            s"interestOnlyDebt = ${ifsData.Interpreted.interestOnlyDebt(dataIndex): Boolean}"

          val useChargeReferenceClause: Option[String] =
            ifsData.Interpreted
              .useChargeReference(dataIndex)
              .map(useChargeRef => s"useChargeReference = ${useChargeRef: Boolean}")

          val subClauses: Seq[String] =
            List(Some(interestRateClause), Some(interestOnlyDebtClause), useChargeReferenceClause).flatten

          subClauses.mkString(" AND ")
        }

        (
          mainTransString,
          subTransString,
          InterestRule(index = dataIndex, raw = s"""IF ${ifCondition: String} -> ${thenClauses: String}""")
        )
      }
      .sortBy({ case (mainTrans, subTrans, _: InterestRule) =>
        (mainTrans, subTrans)
      })
      .map(_._3)

  private final case class InterestRule(index: Int, raw: String)
}
