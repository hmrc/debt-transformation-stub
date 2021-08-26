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

import java.time.LocalDate

import play.api.libs.json.Json
import uk.gov.hmrc.debttransformationstub.controllers.PaymentPlanType

final case class ChannelIdentifier(channelIdentifier: String) extends AnyVal

object ChannelIdentifier extends ValueTypeFormatter {
  implicit val format =
    valueTypeFormatter(ChannelIdentifier.apply, ChannelIdentifier.unapply)
}

final case class Plan(
                       quoteType: QuoteType,
                       quoteDate: LocalDate,
                       instalmentStartDate: LocalDate,
                       instalmentAmount: BigDecimal,
                       frequency: Frequency,
                       duration: Duration,
                       initialPaymentAmount: Option[BigDecimal],
                       initialPaymentDate: Option[LocalDate],
                       paymentPlanType: PaymentPlanType
                     )

object Plan {
  implicit val format = Json.format[Plan]
}


final case class GenerateQuoteRequest(
                                       customerReference: CustomerReference,
                                       channelIdentifier: ChannelIdentifier,
                                       plan: Plan,
                                       customerPostCodes: List[CustomerPostCode],
                                       debtItems: List[DebtItem])


object GenerateQuoteRequest {
  implicit val format = Json.format[GenerateQuoteRequest]
}
