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

import play.api.Environment
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.{ CreateMonitoringCaseRequest, CreatePlanRequest, GenerateQuoteRequest, NDDSRequest, PaymentLockRequest }
import uk.gov.hmrc.debttransformationstub.services.TTPPollingService
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source

class TimeToPayController @Inject()(
  environment: Environment,
  cc: ControllerComponents,
  appConfig: AppConfig,
  ttpPollingService: TTPPollingService
) extends BackendController(cc) with BaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data"

  def generateQuote: Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    withCustomJsonBody[GenerateQuoteRequest] { req =>
      if (appConfig.isPollingEnv) {
        ttpPollingService.insertRequestAndServeResponse(Json.toJson(req), Some(request.uri)).map {
          case Some(v) => Status(v.status.getOrElse(200))(v.content)
          case None    => ServiceUnavailable
        }
      } else {
        val fileMaybe: Option[File] =
          environment.getExistingFile(s"$basePath/ttp.generateQuote/${req.customerReference.value}.json")

        fileMaybe match {
          case None =>
            logger.error(s"Status $NOT_FOUND, message: file not found")
            Future successful NotFound("file not found")
          case Some(file) =>
            val result = Source.fromFile(file).mkString.stripMargin
            Future successful Ok(result)
        }
      }
    }
  }

  def getExistingQuote(customerReference: String, pegaId: String) = Action.async { implicit request =>
    if (appConfig.isPollingEnv) {
      ttpPollingService.insertRequestAndServeResponse(Json.toJson(""), Some(request.uri)).map {
        case Some(v) => Status(v.status.getOrElse(200))(v.content)
        case None    => ServiceUnavailable
      }
    } else {
      environment.getExistingFile(s"$basePath/ttp.viewPlan/$pegaId.json") match {
        case Some(file) => Future.successful(Ok(Source.fromFile(file).mkString))
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future.successful(NotFound("file not found"))
      }
    }
  }

  def updateQuote(customerReference: String, pegaId: String) = Action.async { implicit request =>
    if (appConfig.isPollingEnv) {
      ttpPollingService.insertRequestAndServeResponse(Json.toJson(""), Some(request.uri)).map {
        case Some(v) => Status(v.status.getOrElse(200))(v.content)
        case None    => ServiceUnavailable
      }
    } else {
      environment.getExistingFile(s"$basePath/ttp.updatePlan/$customerReference.json") match {
        case Some(file) => Future.successful(Ok(Source.fromFile(file).mkString))
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future.successful(NotFound("file not found"))
      }
    }
  }

  def createPlan = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CreatePlanRequest] { req =>
      if (appConfig.isPollingEnv) {
        ttpPollingService.insertRequestAndServeResponse(Json.toJson(req), Some(request.uri)).map {
          case Some(v) => Status(v.status.getOrElse(200))(v.content)
          case None    => ServiceUnavailable
        }
      } else {
        val fileMaybe: Option[File] =
          environment.getExistingFile(s"$basePath/ttp.createPlan/${req.plan.quoteId.value}.json")

        fileMaybe match {
          case None =>
            logger.error(s"Status $NOT_FOUND, message: file not found")
            Future successful NotFound("file not found")
          case Some(file) =>
            val result = Source.fromFile(file).mkString.stripMargin
            Future successful Ok(result)
        }
      }
    }
  }

  def nddsEnactArrangement: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[NDDSRequest] { req =>
      findFile(s"/ndds.enactArrangement/${req.channelIdentifier}.json")
    }
  }

  def etmpExecutePaymentLock: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[PaymentLockRequest] { req =>
      findFile(s"/etmp.executePaymentLock/${req.idValue}.json")
    }
  }

  def idmsCreateTTPMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CreateMonitoringCaseRequest] { req =>
      findFile(s"/idms.createTTPMonitoringCase/${req.channelIdentifier}.json")
    }
  }

  private def findFile(path: String)(implicit hc: HeaderCarrier): Future[Result] = {
    val fileMaybe: Option[File] =
      environment.getExistingFile(s"$basePath$path")

    fileMaybe match {
      case None =>
        logger.error(s"Status $NOT_FOUND, message: file not found")
        Future successful NotFound("file not found")
      case Some(file) =>
        val result = Source.fromFile(file).mkString.stripMargin
        Future successful Ok(result)
    }
  }

}
