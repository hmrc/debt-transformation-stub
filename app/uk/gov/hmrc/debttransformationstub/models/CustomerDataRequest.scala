package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.debttransformationstub.controllers.Identity

import java.time.LocalDate


final case class Identity(
                           idType: String,
                           idValue: String
                         )
object Identity{
  implicit val format: OFormat[Identity] = Json.format[Identity]
}

case class CustomerDataRequest(
                                showIds: Boolean,
                                showAddresses: Boolean,
                                addressFromDate: Option[LocalDate],
                                showSignals: Boolean,
                                showFiling: Boolean,
                                showCharges: Boolean,
                                showAdditionalCustData: Boolean,
                                identifications: Option[List[Identity]]
                              )

object CustomerDataRequest{
  implicit val format: OFormat[CustomerDataRequest] = Json.format[CustomerDataRequest]
}
