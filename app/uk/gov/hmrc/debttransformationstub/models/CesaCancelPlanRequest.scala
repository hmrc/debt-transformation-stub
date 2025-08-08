package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Json, OFormat }

case class Identification(idType: String, idValue: String)

object Identification {
  implicit val format: OFormat[Identification] = Json.format[Identification]
}

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
