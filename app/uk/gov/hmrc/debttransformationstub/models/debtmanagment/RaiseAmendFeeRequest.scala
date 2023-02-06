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

package uk.gov.hmrc.debttransformationstub.models.debtmanagment

import java.time.LocalDate
import play.api.libs.json.{ Json, OFormat }

final case class RaiseAmendFeeRequest(
  chargeDetails: ChargeDetails
)

final case class ChargeDetails(
  chargeType: String,
  idType: String,
  idValue: String,
  periodFrom: LocalDate,
  periodTo: LocalDate,
  noticeIssueDate: LocalDate,
  chargeAmount: BigDecimal,
  chargeReference: String
)

object ChargeDetails {
  implicit val ChargeDetailsFormat: OFormat[ChargeDetails] = Json.format[ChargeDetails]
}

object RaiseAmendFeeRequest {
  implicit val RaiseAmendFeeRequestFormat: OFormat[RaiseAmendFeeRequest] = Json.format[RaiseAmendFeeRequest]
}
