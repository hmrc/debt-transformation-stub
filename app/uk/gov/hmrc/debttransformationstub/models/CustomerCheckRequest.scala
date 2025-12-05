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

package uk.gov.hmrc.debttransformationstub.models

import play.api.libs.json.{ Format, Json, OFormat }

case class CustomerCheckRequest(
  userId: Option[CustomerCheckUserId],
  userLocation: Option[CustomerCheckUserLocation],
  customers: List[CustomerCheckCustomer]
)

object CustomerCheckRequest {
  implicit val format: OFormat[CustomerCheckRequest] = Json.format[CustomerCheckRequest]
}

case class CustomerCheckUserId(value: String) extends AnyVal

object CustomerCheckUserId {
  implicit val format: Format[CustomerCheckUserId] = Json.valueFormat[CustomerCheckUserId]
}

case class CustomerCheckUserLocation(value: String) extends AnyVal

object CustomerCheckUserLocation {
  implicit val format: Format[CustomerCheckUserLocation] = Json.valueFormat[CustomerCheckUserLocation]
}

case class CustomerCheckCustomer(
  universalCustomerId: Option[CustomerCheckUniversalCustomerId],
  nino: Option[CustomerCheckNino],
  empRef: Option[CustomerCheckEmpRef]
)

object CustomerCheckCustomer {
  implicit val format: OFormat[CustomerCheckCustomer] = Json.format[CustomerCheckCustomer]
}

case class CustomerCheckUniversalCustomerId(value: String) extends AnyVal

object CustomerCheckUniversalCustomerId {
  implicit val format: Format[CustomerCheckUniversalCustomerId] = Json.valueFormat[CustomerCheckUniversalCustomerId]
}

case class CustomerCheckNino(value: String) extends AnyVal

object CustomerCheckNino {
  implicit val format: Format[CustomerCheckNino] = Json.valueFormat[CustomerCheckNino]
}

case class CustomerCheckEmpRef(value: String) extends AnyVal

object CustomerCheckEmpRef {
  implicit val format: Format[CustomerCheckEmpRef] = Json.valueFormat[CustomerCheckEmpRef]
}
