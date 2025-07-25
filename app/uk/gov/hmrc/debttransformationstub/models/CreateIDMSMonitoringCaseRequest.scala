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

package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Json, OFormat }

import java.time.LocalDate

final case class CreateIDMSMonitoringCaseRequest(
  channelIdentifier: String,
  arrangementAgreedDate: LocalDate,
  paymentPlanStartDate: LocalDate,
  totalLiability: BigDecimal,
  initialPaymentDate: LocalDate,
  initialPaymentAmount: BigDecimal,
  paymentPlanCollectionAmount: BigDecimal,
  paymentPlanFrequency: String,
  arrangementReviewDate: LocalDate,
  regimeType: String,
  regimeDigitalCorrespondence: Boolean,
  emailAddress: String,
  emailSource: String,
  etmpChargesMarked: Boolean,
  ddiReference: String,
  idType: String,
  idValue: String,
  districtNumber: String,
  address: IdmsAddress,
  chargeReferences: List[IdmsChargeReference]
)

object CreateIDMSMonitoringCaseRequest {
  implicit val format: OFormat[CreateIDMSMonitoringCaseRequest] = Json.format[CreateIDMSMonitoringCaseRequest]
}
