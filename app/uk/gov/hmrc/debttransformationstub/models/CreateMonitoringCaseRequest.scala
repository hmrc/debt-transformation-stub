package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.Json

import java.time.LocalDate

final case class CreateMonitoringCaseRequest(
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
  etmpChargesMarked: Boolean,
  ddiReference: String,
  chargeReferences: List[String])

object CreateMonitoringCaseRequest {
  implicit val format = Json.format[CreateMonitoringCaseRequest]
}
