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

/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

import play.api.libs.json.{ Json, OFormat }

final case class CreateIDMSMonitoringCaseRequestSA(
  regimeType: String,
  idType: String,
  idValue: String,
  arrangementAgreedDate: String,
  paymentPlanStartDate: String,
  totalLiability: BigDecimal,
  initialPaymentDate: Option[String],
  initialPaymentAmount: Option[BigDecimal],
  paymentPlanCollectionAmount: BigDecimal,
  paymentPlanFrequency: String,
  arrangementReviewDate: String,
  cesaSuccess: Boolean,
  ddiReference: String,
  chargeIdentifiers: Seq[ChargeIdentifier]
)

final case class ChargeIdentifier(
  saTaxYearEnd: Int,
  creationDate: String,
  chargeType: String,
  tieBreaker: Int,
  originalChargeCreationDate: Option[String] = None,
  originalChargeType: Option[String] = None,
  originalTieBreaker: Option[Int] = None
)

object CreateIDMSMonitoringCaseRequestSA {
  implicit val format: OFormat[CreateIDMSMonitoringCaseRequestSA] = Json.format[CreateIDMSMonitoringCaseRequestSA]
}

object ChargeIdentifier {
  implicit val format: OFormat[ChargeIdentifier] = Json.format[ChargeIdentifier]
}
