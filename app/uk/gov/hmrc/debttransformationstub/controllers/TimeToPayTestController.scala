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
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.models.errors.{TTPRequestsCreationError, TTPRequestsError}
import uk.gov.hmrc.debttransformationstub.services.TTPRequestsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton()
class TimeToPayTestController @Inject()(cc: ControllerComponents, ttpRequestsService: TTPRequestsService)(implicit val executionContext: ExecutionContext)
  extends BackendController(cc) with BaseController {

  private val logger = LogFactory.getLog(classOf[TimeToPayTestController])
  val XCorrelationId = "X-Correlation-Id"

  def getTTPRequests(): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getTTPRequests().map { result: immutable.Seq[RequestDetailsResponse] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def getUnprocessedRequests(): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getUnprocesedTTPRequests().map { result: immutable.Seq[RequestDetailsResponse] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def getTTPRequest(requestId: String): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getTTPRequest(requestId).map { result: Option[RequestDetailsResponse] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def createTTPRequests: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[RequestDetailsRequest] { requestDetailsRequest: RequestDetailsRequest =>

      logger.info("Persist the TTP requests")
      ttpRequestsService.addRequestDetails(requestDetailsRequest = requestDetailsRequest).map(toResult)
    }
  }

  def deleteTTPRequest(requestId: String): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.deleteTTPRequest(requestId).map {
      case Right(result) => Results.Ok(Json.toJson(result)).withHeaders(XCorrelationId -> result)
      case Left(error)   => errorToResult(error)
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
