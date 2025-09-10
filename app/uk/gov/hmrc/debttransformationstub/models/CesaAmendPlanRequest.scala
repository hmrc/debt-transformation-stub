package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Json, OFormat }

case class CesaAmendPlanRequest(
  identifications: List[CesaCancelPlanRequestIdentification]
)

object CesaAmendPlanRequest {
  implicit val format: OFormat[CesaAmendPlanRequest] = Json.format[CesaAmendPlanRequest]
}
