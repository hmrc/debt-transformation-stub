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

package uk.gov.hmrc.debttransformationstub.controllers

import org.apache.commons.logging.LogFactory
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.models.errors.{ TTPRequestsCreationError, TTPRequestsError }
import uk.gov.hmrc.debttransformationstub.services.{ TTPPollingService, TTPRequestsService }
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.collection.immutable
import scala.concurrent.ExecutionContext

@Singleton()
class TimeToPayTestController @Inject() (
  cc: ControllerComponents,
  appConfig: AppConfig,
  ttpRequestsService: TTPRequestsService,
  ttpPollingService: TTPPollingService
)(implicit val executionContext: ExecutionContext)
    extends BackendController(cc) with BaseController {

  private val logger = new RequestAwareLogger(this.getClass)
  val XCorrelationId = "X-Correlation-Id"

  def getTTPRequests(): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getTTPRequests().map { result: immutable.Seq[RequestDetail] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def getUnprocessedRequests(): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getUnprocesedTTPRequests().map { result: immutable.Seq[RequestDetail] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def getTTPRequest(requestId: String): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.getTTPRequest(requestId).map { result: Option[RequestDetail] =>
      Results.Ok(Json.toJson(result))
    }
  }

  def createTTPRequests: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[RequestDetail] { requestDetailsRequest: RequestDetail =>
      logger.info("Persist the TTP requests")
      ttpRequestsService.addRequestDetails(requestDetailsRequest = requestDetailsRequest).map(toResult)
    }
  }

  def deleteTTPRequest(requestId: String): Action[AnyContent] = Action.async { implicit request =>
    ttpRequestsService.deleteTTPRequest(requestId).map {
      case Right(result) => Results.Ok(Json.toJson(result)).withHeaders(XCorrelationId -> result)
      case Left(error) =>
        logger.error(s"TTPRequestDeletionError: $error")
        errorToResult(error)
    }
  }

  private def errorToResult(error: TTPRequestsError): Result =
    error match {
      case e @ TTPRequestsCreationError(statusCode, _, _) =>
        Results.Status(statusCode)(Json.toJson(e.jsonErrorCause))
    }

  private def toResult(eitherResult: Either[TTPRequestsError, String]) = eitherResult match {
    case Right(result) => Results.Ok(Json.toJson(result)).withHeaders(XCorrelationId -> result)
    case Left(error) =>
      errorToResult(error)
  }
}
