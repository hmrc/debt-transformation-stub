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

import play.api.libs.json.Json

import java.time.LocalDate

final case class Instalments(dutyId: String,
                             debtId: String,
                             dueDate: LocalDate,
                             amountDue: BigDecimal,
                             balance: BigDecimal,
                             interest: Double,
                             interestRate: Double,
                             instalmentNumber: Int)

object Instalments {
  implicit val format = Json.format[Instalments]
}

final case class CreatePlanRequest(
                                    customerReference: String,
                                    planId: String,
                                    paymentMethod: String,
                                    paymentReference: String,
                                    thirdPartyBank: Boolean,
                                    instalments: Seq[Instalments],
                                    numberOfInstalments: String,
                                    totalDebtAmount: BigDecimal,
                                    totalInterest: Double
                                  )


object CreatePlanRequest {
  implicit val format = Json.format[CreatePlanRequest]
}