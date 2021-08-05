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

package uk.gov.hmrc.debttransformationstub.controllers

import javax.inject.{Inject, Singleton}
import org.apache.commons.logging.LogFactory
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import scala.collection.immutable
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.debttransformationstub.actions.requests.RequestDetailsRequest
import uk.gov.hmrc.debttransformationstub.actions.responses.RequestDetailsResponse
import uk.gov.hmrc.debttransformationstub.models.errors.{TTPRequestsCreationError, TTPRequestsError}
import uk.gov.hmrc.debttransformationstub.services.{TTPRequestErrorsService, TTPRequestsService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton()
class TimeToPayTestErrorsController @Inject()(cc: ControllerComponents, ttpRequestErrorsService: TTPRequestErrorsService)(implicit val executionContext: ExecutionContext)
  extends BackendController(cc) with BaseController {

  private val logger = LogFactory.getLog(classOf[TimeToPayTestErrorsController])
  val XCorrelationId = "X-Correlation-Id"

  def getTTPRequestErrors(): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestErrorsService.getTTPRequestErrors().map { result: immutable.Seq[RequestDetailsResponse] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def logTTPRequestError: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[RequestDetailsRequest] { requestDetailsRequest: RequestDetailsRequest =>

      logger.info("Persist the TTP request error")
      ttpRequestErrorsService.logTTPRequestError(requestDetailsRequest = requestDetailsRequest).map(toResult)
    }
  }

  private def errorToResult(error: TTPRequestsError): Result = {
    error match {
      case e@TTPRequestsCreationError(statusCode, _, _) => {
        logger.error(s"Error in storing the ttpRequest", e)
        Results.Status(statusCode)(Json.toJson(e.jsonErrorCause))
      }
    }
  }

  private def toResult(eitherResult: Either[TTPRequestsError, String]) = eitherResult match {
    case Right(result) => Results.Ok(Json.toJson(result)).withHeaders(XCorrelationId -> result)
    case Left(error) => errorToResult(error)
  }
}
