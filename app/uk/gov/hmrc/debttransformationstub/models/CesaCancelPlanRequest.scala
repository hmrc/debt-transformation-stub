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

import play.api.libs.json.{ Json, OFormat }

case class CesaCancelPlanRequest(
  identifications: List[Identification],
  noteLines: List[String],
  transitionedIndicator: Option[Boolean],
  ttpStartDate: Option[String],
  ttpEndDate: Option[String],
  ttpFirstPaymentDate: Option[String],
  ttpFirstPaymentAmt: Option[BigDecimal],
  ttpRegularPaymentAmt: Option[BigDecimal],
  ttpPaymentFrequency: Option[Int],
  ttpReviewDate: Option[String],
  ttpInitials: Option[String],
  ttpEnfActToTake: Option[String]
)

object CesaCancelPlanRequest {
  implicit val format: OFormat[CesaCancelPlanRequest] = Json.format[CesaCancelPlanRequest]
}
