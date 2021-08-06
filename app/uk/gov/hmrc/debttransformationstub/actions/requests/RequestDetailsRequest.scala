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

package uk.gov.hmrc.debttransformationstub.actions.requests

import java.time.LocalDateTime
import play.api.libs.json.{Json, OFormat}

case class RequestDetailsRequest(requestId: String, content: String, uri: Option[String], isResponse: Boolean, createdOn: Option[LocalDateTime])


object RequestDetailsRequest {
    implicit val requestDetailsRequestFormat: OFormat[RequestDetailsRequest]  = Json.format[RequestDetailsRequest]

}
