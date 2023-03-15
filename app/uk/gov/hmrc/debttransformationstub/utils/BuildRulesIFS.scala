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

package uk.gov.hmrc.debttransformationstub.utils

import uk.gov.hmrc.http.HeaderCarrier

import java.util.Base64

object BuildRulesIFS extends App {
//Uncomment the below encodedList method (PR builder does not allow for println())

//  def encodeList(notEncodedRuleValues: List[String], num: Int): Unit = {
//        if (notEncodedRuleValues.isEmpty) {
//          Nil
//        } else {
//          val rule = notEncodedRuleValues.head
//          val encoded = new String(Base64.getEncoder.encodeToString(rule.getBytes()))
//          val rules = s"service-config.rules.$num: " + encoded + "\n" + s"# $rule"
//          println(rules)
//          encodeList(notEncodedRuleValues.tail, num + 1)
//        }
//  }

//     e.g.  encodeList(
//        List("IF mainTrans == '4794' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true",
//        "IF mainTrans == '4797' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true"),
//        174)

}
