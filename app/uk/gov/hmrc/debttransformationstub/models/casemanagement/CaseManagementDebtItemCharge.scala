/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.debttransformationstub.models.{ DebtItemChargeId, MainTransType, Payment, SubTransType }

final case class CaseManagementDebtItemCharge(
  debtItemsChargeId: DebtItemChargeId,
  mainTrans: MainTransType,
  subtrans: SubTransType,
  originalDebtAmount: BigDecimal,
  interestStartDate: LocalDate,
  paymentHistory: Seq[Payment]
) {
  require(!debtItemsChargeId.value.trim().isEmpty, "debtItemChargeId should not be empty")
}

object CaseManagementDebtItemCharge {
  implicit val format = Json.format[CaseManagementDebtItemCharge]
}
