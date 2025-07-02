package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Json, OFormat }

final case class CdcsChargeInfoRequest(
  channelIdentifier: String,
  identifications: Seq[Identification],
  regimeType: String
)

object CdcsChargeInfoRequest {
  implicit val format: OFormat[CdcsChargeInfoRequest] = Json.format[CdcsChargeInfoRequest]
}
