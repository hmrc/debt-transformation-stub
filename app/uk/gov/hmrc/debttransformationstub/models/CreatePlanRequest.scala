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

import play.api.libs.json.Json
import java.time.LocalDate

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import uk.gov.hmrc.debttransformationstub.controllers.PaymentPlanType

final case class Instalment(debtItemChargeId: DebtItemChargeId,
                            dueDate: LocalDate,
                            amountDue: BigDecimal,
                            expectedPayment: BigDecimal,
                            interestRate: Double,
                            instalmentNumber: Int,
                            instalmentInterestAccrued: BigDecimal,
                            instalmentBalance: BigDecimal)

object Instalment {
  implicit val format = Json.format[Instalment]
}

final case class QuoteId(value: String) extends AnyVal

object QuoteId extends ValueTypeFormatter {
  implicit val format = valueTypeFormatter(QuoteId.apply, QuoteId.unapply)
}

sealed abstract class PaymentMethod(override val entryName: String) extends EnumEntry

object PaymentMethod extends Enum[PaymentMethod] with PlayJsonEnum[PaymentMethod] {
  val values: scala.collection.immutable.IndexedSeq[PaymentMethod] = findValues

  case object DirectDebit extends PaymentMethod("directDebit")
  case object Bacs extends PaymentMethod("BACS")
  case object Cheque extends PaymentMethod("cheque")
  case object CardPayment extends PaymentMethod("cardPayment")
}

final case class PlanToCreatePlan(quoteId: QuoteId,
                                  quoteType: QuoteType,
                                  quoteDate: LocalDate,
                                  instalmentStartDate: LocalDate,
                                  instalmentAmount: BigDecimal,
                                  paymentPlanType: PaymentPlanType,
                                  thirdPartyBank: Boolean,
                                  numberOfInstalments: Int,
                                  frequency: Frequency,
                                  duration: Duration,
                                  initialPaymentDate: LocalDate,
                                  initialPaymentAmount: BigDecimal,
                                  totalDebtincInt: BigDecimal,
                                  totalInterest: BigDecimal,
                                  interestAccrued: BigDecimal,
                                  planInterest: BigDecimal)

object PlanToCreatePlan {
  implicit val format = Json.format[PlanToCreatePlan]
}


final case class PaymentReference(value: String) extends AnyVal

object PaymentReference extends ValueTypeFormatter {
  implicit val format =
    valueTypeFormatter(PaymentReference.apply, PaymentReference.unapply)
}

final case class PaymentInformation(paymentMethod: PaymentMethod, paymentReference: PaymentReference)

object PaymentInformation {
  implicit val format = Json.format[PaymentInformation]
}

final case class CreatePlanRequest(customerReference: CustomerReference,
                                   quoteReference: QuoteReference,
                                   channelIdentifier: ChannelIdentifier,
                                   plan: PlanToCreatePlan,
                                   debtItemCharges: Seq[DebtItemForCreatePlan],
                                   payments: Seq[PaymentInformation],
                                   customerPostCodes: Seq[CustomerPostCode],
                                   instalments: Seq[Instalment]
                                  )

object CreatePlanRequest {
  implicit val format = Json.format[CreatePlanRequest]
}
