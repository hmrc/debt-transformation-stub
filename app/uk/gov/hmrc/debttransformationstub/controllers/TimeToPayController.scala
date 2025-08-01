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

import org.apache.commons.io.FileUtils
import play.api.Environment
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes.CdcsCreateCaseRequestLastName
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.debttransformationstub.repositories.{ EnactStage, EnactStageRepository }
import uk.gov.hmrc.debttransformationstub.services.TTPPollingService
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scala.util.Try

class TimeToPayController @Inject() (
  environment: Environment,
  cc: ControllerComponents,
  appConfig: AppConfig,
  ttpPollingService: TTPPollingService,
  enactStageRepository: EnactStageRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with CustomBaseController {

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

  def generateAffordabilityQuote: Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    if (appConfig.isPollingEnv) {
      ttpPollingService.insertRequestAndServeResponse(Json.toJson(""), Some(request.uri)).map {
        case Some(v) => Status(v.status.getOrElse(200))(v.content)
        case None    => ServiceUnavailable
      }
    } else {
      environment.getExistingFile(s"$basePath/ttp.generateAffordabilityQuote/affordabilityQuoteResponse.json") match {
        case Some(file) => Future.successful(Ok(Source.fromFile(file).mkString))
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future.successful(NotFound("file not found"))
      }
    }
  }

  def getExistingQuote(customerReference: String, pegaId: String): Action[AnyContent] = Action.async {
    implicit request =>
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

  def updateQuote(customerReference: String, pegaId: String): Action[AnyContent] = Action.async { implicit request =>
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

  def createPlan: Action[JsValue] = Action.async(parse.json) { implicit request =>
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
    val correlationId = getCorrelationIdHeader(request.headers)
    withCustomJsonBody[NDDSRequest] { req =>
      val requestChargeHodServices = req.paymentPlan.paymentPlanCharges.map(_.hodService)
      if (requestChargeHodServices.contains("PAYE")) {
        val brocsId = req.identification
          .find(_.idType.equalsIgnoreCase("BROCS"))
          .map(_.idValue)
          .getOrElse(throw new IllegalArgumentException("BROCS id is required for PAYE NDDS enact arrangement"))
        for {
          _            <- enactStageRepository.addNDDSStage(correlationId, req)
          fileResponse <- constructResponse(s"/ndds.enactArrangement/", s"$brocsId.json")
        } yield fileResponse
      } else if (requestChargeHodServices.contains("VAT")) {
        val vrnId = req.identification
          .find(_.idType.equalsIgnoreCase("VRN"))
          .map(_.idValue)
          .getOrElse(throw new IllegalArgumentException("VRN id is required for VAT NDDS enact arrangement"))
        for {
          _            <- enactStageRepository.addNDDSStage(correlationId, req)
          fileResponse <- constructResponse(s"/ndds.enactArrangement/", s"$vrnId.json")
        } yield fileResponse
      } else if (requestChargeHodServices.contains("SAFE")) {
        val ninoBrocsId = req.identification
          .find(id => id.idType.equalsIgnoreCase("NINO") || id.idType.equalsIgnoreCase("BROCS"))
          .map(_.idValue)
          .getOrElse(
            throw new IllegalArgumentException("NINO or BROCS id is required for SIMP or PAYE NDDS enact arrangements")
          )
        for {
          _            <- enactStageRepository.addNDDSStage(correlationId, req)
          fileResponse <- constructResponse(s"/ndds.enactArrangement/", s"$ninoBrocsId.json")
        } yield fileResponse
      } else if (requestChargeHodServices.contains("CESA")) {
        val ninoUTRId = req.identification
          .find(id => id.idType.equalsIgnoreCase("NINO") || id.idType.equalsIgnoreCase("UTR"))
          .map(_.idValue)
          .getOrElse(
            throw new IllegalArgumentException("NINO or UTR id is required for SA NDDS enact arrangements")
          )
        for {
          _            <- enactStageRepository.addNDDSStage(correlationId, req)
          fileResponse <- constructResponse(s"/ndds.enactArrangement/", s"$ninoUTRId.json")
        } yield fileResponse
      } else {
        throw new IllegalArgumentException(
          "Either BROCS, VRN, SAFE or UTR id types are required for PAYE, VAT, SIMP or SA enact arrangements"
        )
      }
    }
  }

  def pegaUpdateCase(caseId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val maybeAuthToken: Option[String] = request.headers.get("Authorization")
    val isSchedulerCall = maybeAuthToken.contains("Bearer scheduled-pega-access-token")
    if (isSchedulerCall) {
      val correlationId = getCorrelationIdHeader(request.headers)
      withCustomJsonBody[UpdateCaseRequest] { req =>
        for {
          _            <- enactStageRepository.addPegaStage(correlationId, req)
          fileResponse <- constructResponse(s"/pega.updateCase/", s"$caseId.json")
        } yield fileResponse
      }
    } else {
      val errorMessage = s"expected token to be 'Bearer scheduled-pega-access-token', got $maybeAuthToken"
      logger.error(errorMessage)
      Future successful UnprocessableEntity(errorMessage)
    }

  }

  def etmpExecutePaymentLock: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val correlationId = getCorrelationIdHeader(request.headers)
    withCustomJsonBody[PaymentLockRequest] { req =>
      for {
        _            <- enactStageRepository.addETMPStage(correlationId, req)
        fileResponse <- constructResponse(s"/etmp.executePaymentLock/", s"${req.idValue}.json")
      } yield fileResponse
    }
  }

  def idmsCreateTTPMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val correlationId = getCorrelationIdHeader(request.headers)
    withCustomJsonBody[CreateMonitoringCaseRequest] { req =>
      for {
        _            <- enactStageRepository.addIDMSStage(correlationId, req)
        fileResponse <- constructResponse(s"/idms.createTTPMonitoringCase/", s"${req.ddiReference}.json")
      } yield fileResponse
    }
  }

  def cdcsCreateCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    def buildResponse(responseStatus: Status, fileName: String) =
      findFile(s"/cdcs.createCase/", fileName) match {
        case Some(file) =>
          val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
          Try(Json.parse(fileString)).toOption match {
            case Some(jsValue) => responseStatus(jsValue)
            case None          => InternalServerError(s"failing stub cannot parse file $fileName")
          }
        case None => NotFound("file not found")
      }

    withCustomJsonBody[CdcsCreateCaseRequest] { req =>
      enactStageRepository.addCDCSStage(getCorrelationIdHeader(request.headers), req).map { _ =>
        req.customer.individual.lastName match {
          case CdcsCreateCaseRequestLastName("STUB_FAILURE_500") => new Status(INTERNAL_SERVER_ERROR)
          case CdcsCreateCaseRequestLastName("STUB_FAILURE_400") =>
            buildResponse(BadRequest, "cdcsCreateCaseFailure_400.json")
          case CdcsCreateCaseRequestLastName("STUB_FAILURE_422") =>
            buildResponse(UnprocessableEntity, "cdcsCreateCaseFailure_422.json")
          case _ => buildResponse(Ok, "cdcsCreateCaseSuccessResponse.json")
        }
      }
    }
  }

  def enactStage(correlationId: String): Action[AnyContent] = Action.async { request =>
    enactStageRepository.findByCorrelationId(correlationId).map { stage: Option[EnactStage] =>
      Ok(Json.toJson(stage))
    }
  }

  private def findFile(path: String, fileName: String): Option[File] =
    environment.getExistingFile(s"$basePath$path$fileName")

  private def constructResponse(path: String, fileName: String)(implicit hc: HeaderCarrier): Future[Result] = {
    val fileMaybe: Option[File] = findFile(path, fileName)

    fileMaybe match {
      case None if fileName.toUpperCase().startsWith("PA400") =>
        val msg = "intentional stubbed bad request"
        logger.error(s"Status $BAD_REQUEST, message: $msg")
        Future successful BadRequest(msg)
      case None if fileName.toUpperCase().startsWith("PA422") =>
        val msg = "intentional stubbed unprocessable entity"
        logger.error(s"Status $UNPROCESSABLE_ENTITY, message: $msg")
        Future successful UnprocessableEntity(msg)
      case None =>
        logger.error(s"Status $NOT_FOUND, message: file not found")
        Future successful NotFound("file not found")
      case Some(file) =>
        val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
        val result = Try(Json.parse(fileString)).toOption
          .map(Ok(_))
          .getOrElse(InternalServerError(s"stub failed to parse file $basePath$path"))
        Future successful result
    }
  }

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
