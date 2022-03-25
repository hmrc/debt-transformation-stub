/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.models.casemanagement
import java.time.LocalDate

import play.api.libs.json.Json
import uk.gov.hmrc.debttransformationstub.controllers.PaymentPlanType
import uk.gov.hmrc.debttransformationstub.models.{ Duration, Frequency, QuoteId, QuoteType }

final case class CaseManagementPlan(
  quoteId: QuoteId,
  quoteType: QuoteType,
  quoteDate: LocalDate,
  instalmentStartDate: LocalDate,
  instalmentAmount: Option[BigDecimal],
  paymentPlanType: PaymentPlanType,
  thirdPartyBank: Boolean,
  numberOfInstalments: Int,
  frequency: Option[Frequency],
  duration: Option[Duration],
  initialPaymentDate: Option[LocalDate],
  initialPaymentAmount: Option[BigDecimal],
  totalDebtincInt: BigInt,
  totalInterest: BigDecimal,
  interestAccrued: BigInt,
  planInterest: BigDecimal
) {
  require(!quoteId.value.trim().isEmpty(), "quoteId should not be empty")
}

object CaseManagementPlan {
  implicit val format = Json.format[CaseManagementPlan]
}
