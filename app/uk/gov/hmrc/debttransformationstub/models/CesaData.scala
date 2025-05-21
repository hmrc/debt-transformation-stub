/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{ Json, OFormat }

final case class CesaData(
                                   debitIdentifiers: List[CesaDebitIdentifier]
                                 )

object CesaData {
  implicit val format: OFormat[CesaData] = Json.format[CesaData]
}

final case class CesaDebitIdentifier(
                                         UTR: String,
                                         saTaxYearEnd: Int,
                                         creationDate: String,
                                         chargeType: String,
                                         tieBreaker: String,
                                         originalCreationDate: String,
                                         originalChargeType: Option[String],
                                         originalTieBreaker: Option[String],
                                         chargeReference: String,
                                         parentChargeReference: String,
                                         processingOutcome: String,
                                       )

object CesaDebitIdentifier {
  implicit val format: OFormat[CesaDebitIdentifier] = Json.format[CesaDebitIdentifier]
}
