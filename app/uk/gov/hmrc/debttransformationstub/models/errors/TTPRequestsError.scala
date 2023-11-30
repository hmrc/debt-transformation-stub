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

package uk.gov.hmrc.debttransformationstub.models.errors

trait TTPRequestsError extends Exception with Product with Serializable {
  val name: String = productPrefix
}

sealed trait ConnectorError extends TTPRequestsError {
  val statusCode: Int
  val identifier: Option[String] = None
  val reason: Option[String] = None
  val cause = s"$name: statusCode:{$statusCode}, reason:{${reason.mkString}}, uniqueReference:{${identifier.mkString}}"
  val jsonErrorCause =
    s"""{name: $name, statusCode: $statusCode, reason:{${reason.mkString}}, uniqueReference: ${identifier.mkString}}"""
}
case object NO_RESPONSE extends ConnectorError {
  override val statusCode: Int = 500
  override val jsonErrorCause: String = """{"failures": [
                                          |  {
                                          |    "code": "SERVICE_UNAVAILABLE",
                                          |    "reason": "idms not responding"
                                          |  }
                                          |]}""".stripMargin
}
final case class TTPRequestsCreationError(
  statusCode: Int,
  override val reason: Option[String] = None,
  override val identifier: Option[String] = None
) extends ConnectorError

final case class TTPRequestsDeletionError(
  statusCode: Int,
  override val reason: Option[String] = None,
  override val identifier: Option[String] = None
) extends ConnectorError
