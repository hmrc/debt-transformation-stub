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

package uk.gov.hmrc.debttransformationstub.controllers

import play.api.Environment
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.io.Source
import scala.math.Ordering.Implicits.infixOrderingOps
import scala.util.Try

class ETMPController @Inject() (environment: Environment, cc: ControllerComponents) extends BackendController(cc) {

  private val basePath = "conf/resources/data/etmp.eligibility"

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def paymentPlanEligibility(
    regimeType: String,
    idType: String,
    idValue: String
  ): Action[AnyContent] = Action { request =>
    val queryKeys: List[String] =
      List("showIds", "showAddresses", "showSignals", "showFiling", "showCharges", "addressFromDate")
    val queries: Map[String, Option[String]] = queryKeys.map(key => (key, request.getQueryString(key))).toMap
    queries("showIds")
    val relativePath = s"$basePath" + "." + regimeType + "/" + s"$idValue.json"
    environment.getExistingFile(relativePath) match {
      case Some(file) =>
        Try(Json.parse(paymentPlanEligibilityString(file, idValue))).toOption
          .map(Ok(_))
          .getOrElse(InternalServerError(s"stub failed to parse file $relativePath"))
      case _ =>
        NotFound("file not found")
    }
  }

  private def paymentPlanEligibilityString(file: File, idValue: String): String = {
    val currentDate = LocalDate.now()
    val responseTemplate: String = Source.fromFile(file).mkString

    /** Valid should mean in the past, but not too far in the past. */
    def validAsnDate(monthsAgo: Int): LocalDate = {
      val result = currentDate.withDayOfMonth(22).minusMonths(monthsAgo)
      require(currentDate > result)
      result
    }

    val dueDateInPast = currentDate.minusDays(24)
    val dueDateToday = currentDate
    val dueDateInFuture = currentDate.plusDays(24)
    val dueDateOverMaxDebtAgeVATC = currentDate.minusDays(29)
    val dueDateEqualsMaxDebtAgeVATC = currentDate.minusDays(28)
    val dueDateOverMaxDebtAgePAYE = currentDate.minusDays(1826)
    val dueDateEqualsMaxDebtAgePAYE = currentDate.minusDays(1825)

    val initialOverride: String =
      (1 to 24)
        .foldLeft(responseTemplate) {
          case (accumulatingResponseTemplate, monthsAgo) =>
            val validAsnDateString = validAsnDate(monthsAgo = monthsAgo).format(dateFormatter)
            accumulatingResponseTemplate.replaceAll(s"<VALID_DUE_DATE_$monthsAgo>", validAsnDateString)
        }

    val result =
      initialOverride
        .replaceAll("<DUE_DATE>", dueDateInPast.format(dateFormatter))
        .replaceAll("<DUE_DATE_TODAY>", dueDateToday.format(dateFormatter))
        .replaceAll("<DUE_DATE_FOR_FUTURE>", dueDateInFuture.format(dateFormatter))
        .replaceAll("<DUE_DATE_OVER_MAX_DEBT_AGE_VATC>", dueDateOverMaxDebtAgeVATC.format(dateFormatter))
        .replaceAll("<DUE_DATE_EQUALS_MAX_DEBT_AGE_VATC>", dueDateEqualsMaxDebtAgeVATC.format(dateFormatter))
        .replaceAll("<DUE_DATE_OVER_MAX_DEBT_AGE_PAYE>", dueDateOverMaxDebtAgePAYE.format(dateFormatter))
        .replaceAll("<DUE_DATE_EQUALS_MAX_DEBT_AGE_PAYE>", dueDateEqualsMaxDebtAgePAYE.format(dateFormatter))

    println(
      s"""====================
         |$result
         |====================
         |""".stripMargin
    )
    result
  }
}

object ETMPController {
  val SingleErrorIdNumber = "012X012345"
  val MultipleErrorsIdNumber = "023X023456"
}
