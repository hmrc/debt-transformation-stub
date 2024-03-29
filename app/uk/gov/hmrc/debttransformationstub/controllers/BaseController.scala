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

package uk.gov.hmrc.debttransformationstub.controllers

import play.api.http.Status.BAD_REQUEST
import play.api.libs.json._
import play.api.mvc.Results.BadRequest
import play.api.mvc.{ Request, Result }
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

trait CustomBaseController {
  private lazy val logger = new RequestAwareLogger(this.getClass)

  def withCustomJsonBody[T](
    f: T => Future[Result]
  )(implicit request: Request[JsValue], m: Manifest[T], reads: Reads[T], hc: HeaderCarrier): Future[Result] =
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        val reason = errs.map { case (path, _) => invalidJsonMessage(path) }.mkString("\n")
        logger.error(s"Invalid Json: $reason")
        val jsonResponse =
          s"""
             |{
             |	"reason": $reason,
             |	"message": "invalid json"
             |}
             |""".stripMargin
        Future.successful(BadRequest(Json.toJson(jsonResponse)))

      case Failure(e) =>
        logger.error(s"Status $BAD_REQUEST, message: ${e.getMessage}")
        Future.successful(BadRequest(s"Could not parse body due to ${e.getMessage}"))
    }

  private def invalidJsonMessage(path: JsPath) = s"Field at path '$path' missing or invalid"
}
