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

final case class CesaDataRequest(
                                   debitIdentifiers: List[DebitIdentifier]
                                 )

object CesaDataRequest {
  implicit val format: OFormat[CesaDataRequest] = Json.format[CesaDataRequest]
}


final case class DebitIdentifier(
  UTR: String,
  saTaxYearEnd: Int,
  creationDate: String,
  chargeType: String,
  tieBreaker: String,
)

object DebitIdentifier {
  implicit val format: OFormat[DebitIdentifier] = Json.format[DebitIdentifier]
}
