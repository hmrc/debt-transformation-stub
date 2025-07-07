/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class CdcsRequest(
  regimeType: Int,
  identifications: List[CdcsIdentification],
  chargeReferences: List[CdcsChargeReference],
  debtItem: List[CdcsDebtItem],
  customerType: String,
  address: CdcsAddress
)

object CdcsRequest {
  implicit val format: OFormat[CdcsRequest] = Json.format[CdcsRequest]
}

final case class CdcsIdentification(idType: Int, idValue: String)

object CdcsIdentification {
  implicit val format: OFormat[CdcsIdentification] = Json.format[CdcsIdentification]
}

final case class CdcsChargeReference(
  chargeReference: String
)
object CdcsChargeReference {
  implicit val format: OFormat[CdcsChargeReference] = Json.format[CdcsChargeReference]
}

final case class CdcsDebtItem(
                                        outstandingDebtAmount: String,
                                        debtItemChargeId: DebtItemChargeId,
                                        debtItemOriginalDueDate: Option[LocalDate],
                                        accruedInterest: BigDecimal,
                                        isInterestBearingCharge: Boolean,
                                        originalDebtAmount: BigDecimal,
                                        interestStartDate: Option[LocalDate],
                                        paymentHistory: Seq[Payment]
                                      )
object CdcsDebtItem {
  implicit val format: OFormat[CdcsDebtItem] = Json.format[CdcsDebtItem]
}

final case class CdcsAddress(
                              addressType: String,
                              addressLine1: String,
                              addressLine2: Option[String],
                              addressLine3: Option[String],
                              addressLine4: Option[String],
                              postCode: Option[String]
                            )

object CdcsAddress {
  implicit val format: OFormat[CdcsAddress] = Json.format[CdcsAddress]
}