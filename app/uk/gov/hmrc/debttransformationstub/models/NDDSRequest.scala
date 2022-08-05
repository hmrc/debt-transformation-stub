package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.Json

import java.time.LocalDate

case class Identification(idType: String, idValue: String)

object Identification {
  implicit val format = Json.format[Identification]
}

case class DirectDebitInstruction(
  sortCode: String,
  accountNumber: String,
  accountName: String,
  paperAuddisFlag: Boolean)

object DirectDebitInstruction {
  implicit val format = Json.format[DirectDebitInstruction]
}

case class PaymentPlanCharges(sequence: Int, hodService: String, hodReference: String, chargeAmount: Double)

object PaymentPlanCharges {
  implicit val format = Json.format[PaymentPlanCharges]
}

case class NDDSPaymentPlan(
  paymentPlanType: String,
  initialPaymentAmount: Option[Int],
  initialPaymentDate: Option[LocalDate],
  paymentPlanCollectionAmount: Double,
  paymentPlanStartDate: LocalDate,
  paymentPlanEndDate: LocalDate,
  paymentPlanFrequency: String,
  paymentPlanBalancingAmount: Double,
  paymentPlanBalancingDate: LocalDate,
  paymentPlanCharges: List[PaymentPlanCharges],
  totalLiability: Double)

object NDDSPaymentPlan {
  implicit val format = Json.format[NDDSPaymentPlan]
}

case class NDDSRequest(
  channelIdentifier: String,
  identification: List[Identification],
  directDebitInstruction: DirectDebitInstruction,
  paymentPlan: NDDSPaymentPlan,
  printFlag: Boolean)

object NDDSRequest {
  implicit val format = Json.format[NDDSRequest]
}
