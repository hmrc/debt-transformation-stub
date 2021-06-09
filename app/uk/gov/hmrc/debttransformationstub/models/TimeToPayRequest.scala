/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.models

import java.time.LocalDate

import play.api.libs.json.Json

case class BreathingSpace(debtRespiteFrom: LocalDate,
                          debtRespiteTo: LocalDate,
                          paymentDate: LocalDate,
                          paymentAmount: BigDecimal)

object BreathingSpace {
  implicit val format = Json.format[BreathingSpace]
}

final case class Duty(
                       dutyId: String,
                       subtrans: String,
                       originalDebtAmount: BigDecimal,
                       interestStartDate: LocalDate,
                       breathingSpaces: List[BreathingSpace])

object Duty {
  implicit val format = Json.format[Duty]
}

final case class Debts(
                        debtId: String,
                        mainTrans: String,
                        duties: Seq[Duty])

object Debts {
  implicit val format = Json.format[Debts]
}

case class Customer(quoteType: String,
                    instalmentStartDate: String,
                    instalmentAmount: Int,
                    frequency: String,
                    duration: String,
                    initialPaymentAmount: Int,
                    initialPaymentDate: LocalDate,
                    paymentPlanType: String)

object Customer {
  implicit val format = Json.format[Customer]
}

case class TimeToPayRequest(
                             customerReference: String,
                             debtAmount: BigDecimal,
                             customer: List[Customer],
                             debts: List[Debts])


object TimeToPayRequest {
  implicit val format = Json.format[TimeToPayRequest]
}