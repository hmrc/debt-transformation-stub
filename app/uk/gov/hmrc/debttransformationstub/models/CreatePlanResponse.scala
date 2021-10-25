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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import play.api.libs.json.Json

final case class CaseId(value: String) extends AnyVal

object CaseId extends ValueTypeFormatter {
  implicit val format =
    valueTypeFormatter(CaseId.apply, CaseId.unapply)
}

sealed abstract class PlanStatus(override val entryName: String) extends EnumEntry

object PlanStatus extends Enum[PlanStatus] with PlayJsonEnum[PlanStatus] {
  val values: scala.collection.immutable.IndexedSeq[PlanStatus] = findValues

  case object Success extends PlanStatus("success")
  case object Failure extends PlanStatus("failure")
  case object Complete extends PlanStatus("complete")
  case object Cancelled extends PlanStatus("cancelled")

}

final case class CreatePlanResponse(customerReference: CustomerReference, planId: PlanId, caseId: CaseId, planStatus: PlanStatus)

object CreatePlanResponse {
  implicit val format = Json.format[CreatePlanResponse]
}
