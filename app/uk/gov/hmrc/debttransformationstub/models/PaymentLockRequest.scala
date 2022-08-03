package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Json, OFormat }

final case class PaymentLockRequest(
  idType: String,
  idValue: String,
  regimeType: String,
  lockReason: String,
  chargeReferences: List[String],
  createNote: Boolean,
  noteType: String,
  noteLines: List[String])

object PaymentLockRequest {
  implicit val format: OFormat[PaymentLockRequest] = Json.format[PaymentLockRequest]
}
