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

import play.api.libs.json.{ Format, Json, OFormat }

case class CesaCreateRequest(
  identifications: List[CESAIdentification],
  transitionedIndicator: Option[Boolean],
  ttpStartDate: Option[String],
  ttpEndDate: Option[String],
  ttpFirstPaymentDate: Option[String],
  ttpFirstPaymentAmt: Option[BigDecimal],
  ttpRegularPaymentAmt: Option[BigDecimal],
  ttpPaymentFrequency: Option[Int],
  ttpReviewDate: Option[String],
  ttpInitials: Option[String],
  ttpEnfActToTake: Option[String],
  noteLines: List[String]
)

case class CESAIdentification(idType: String, idValue: String)

object CESAIdentification {
  implicit val format: OFormat[CESAIdentification] = Json.format[CESAIdentification]

}

object CesaCreateRequest {
  implicit val format: OFormat[CesaCreateRequest] = Json.format[CesaCreateRequest]
}
