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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.controllers.CustomBaseController.returnStatusBasedOnIdValue
import uk.gov.hmrc.debttransformationstub.models
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequest.CdcsCreateCaseRequestIdentification
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes.{CdcsCreateCaseRequestIdTypeReference, CdcsCreateCaseRequestLastName}
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes.{ CdcsCreateCaseRequestIdTypeReference, CdcsCreateCaseRequestLastName }
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.debttransformationstub.models.errors.NO_RESPONSE
import uk.gov.hmrc.debttransformationstub.repositories.{EnactStage, EnactStageRepository}
import uk.gov.hmrc.debttransformationstub.repositories.{ EnactStage, EnactStageRepository }
import uk.gov.hmrc.debttransformationstub.services.TTPPollingService
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
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
          .find(id => id.idType.equalsIgnoreCase("UTR"))
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
    withCustomJsonBody[CreateIDMSMonitoringCaseRequest] { req =>
      logger.info(
        s"Received request to create IDMS monitoring case with correlationId: $correlationId and ddiReference: ${req.ddiReference}"
      )
      for {
        _            <- enactStageRepository.addIDMSStage(correlationId, req)
        fileResponse <- constructResponse(s"/idms.createTTPMonitoringCase/", s"${req.ddiReference}.json")
      } yield fileResponse
    }
  }

  def idmsCreateSAMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.info("Request body for idmsCreateSAMonitoringCase: " + request.body)
    logger.info("Request headers for idmsCreateSAMonitoringCase: " + request.headers)
    val correlationId = getCorrelationIdHeader(request.headers)
    withCustomJsonBody[CreateIDMSMonitoringCaseRequestSA] { req =>
      logger.info(
        s"Received request to create SA IDMS monitoring case with correlationId: $correlationId and idValue: ${req.idValue}"
      )
      for {
        _            <- enactStageRepository.addIDMSStageSA(correlationId, req)
        fileResponse <- constructResponse(s"/idms.createSAMonitoringCase/", s"${req.idValue}.json")
      } yield fileResponse
    }
  }

  def cesaCancelCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCancelPlanRequest] { req =>
      val testDataPackage = "/cesa.cancelCase/"

      val firstIdentifierOrUtr: Option[String] =
        if (req.identifications.length == 1) {
          req.identifications.map(_.idValue).headOption
        } else {
          req.identifications.find(_.idType == "UTR").map(_.idValue)
        }

      val response =
        firstIdentifierOrUtr
          .flatMap {
            case "cesaCancelPlan_error_400" =>
              buildResponseFromFileAndStatus(testDataPackage, BadRequest, "cesaCancelPlan_error_400.json")
            case "cesaCancelPlan_error_404" =>
              buildResponseFromFileAndStatus(testDataPackage, NotFound, "cesaCancelPlan_error_404.json")
            case "cesaCancelPlan_error_409" =>
              buildResponseFromFileAndStatus(testDataPackage, Conflict, "cesaCancelPlan_error_409.json")
            case "6642083101" =>
              buildResponseFromFileAndStatus(testDataPackage, InternalServerError, "cesaCancelPlan_error_500.json")
            case "cesaCancelPlan_error_502" =>
              buildResponseFromFileAndStatus(testDataPackage, BadGateway, "cesaCancelPlan_error_502.json")
            case _ => buildResponseFromFileAndStatus(testDataPackage, Ok, "cesaCancelPlanSuccess.json")
          }
          .getOrElse(NotFound("file not found"))

      Future.successful(response)
    }
  }

  def cdcsCreateCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CdcsCreateCaseRequest] { req =>
      val testDataPackage = "/cdcs.createCase/"

//      val maybeUtrIdentifier = req.customer.individual.identifications
//        .find(_.idType == CdcsCreateCaseRequestIdTypeReference.UTR)
//        .map(_.idValue)

      val preferredSubstring = "cdcs"

      val maybeUtrIdentifier: Option[String] = {
        val utrValues: List[String] =
          req.customer.individual.identifications.collect {
            case CdcsCreateCaseRequestIdentification(CdcsCreateCaseRequestIdTypeReference.UTR, idVal) => idVal.value
          }

        utrValues.find(_.contains(preferredSubstring))    // prefer the one that contains the text
          .orElse(utrValues.lift(1))                      // else: second UTR (original behaviour)
      }

      val lastName = req.customer.individual.lastName
      logger.info(s"maybeUtrIdentifier*******: $maybeUtrIdentifier")

      enactStageRepository.addCDCSStage(getCorrelationIdHeader(request.headers), req).map { _ =>
        val responseFromUtr: Option[Result] =
          maybeUtrIdentifier.flatMap { utr =>
            logger.info(s"UTR candidate = $utr")
            returnStatusBasedOnIdValue("cdcsResponse_error_", utr) match {
              case Some(forcedStatus) =>
                // Forced status branch: if file exists, return its JSON with the forced status.
                // If file is missing or unparsable, return the forced status with an empty body.
                val fileName = s"$utr.json"
                findFile(testDataPackage, fileName) match {
                  case Some(file) =>
                    val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
                    scala.util.Try(Json.parse(fileString)).toOption match {
                      case Some(js) => Some(forcedStatus(js)) // e.g. 401 + JSON body
                      case None     =>
                        logger.error(s"failing stub cannot parse file $testDataPackage$fileName")
                        Some(forcedStatus("")) // forced status, empty body
                    }
                  case None =>
                    logger.error(s"file not found $testDataPackage$fileName")
                    Some(forcedStatus("")) // forced status, empty body
                }
              case None =>
                // Default path: 200 OK with UTR-named file using your helper
                buildResponseFromFileAndStatus(testDataPackage, Ok, s"$utr.json") // Option[Result]
            }
          }
        responseFromUtr
          .orElse {
            lastName match {
              case CdcsCreateCaseRequestLastName("STUB_FAILURE_500") => new
                  Some(Status(INTERNAL_SERVER_ERROR))
              case CdcsCreateCaseRequestLastName("STUB_FAILURE_400") =>
                buildResponseFromFileAndStatus(testDataPackage, BadRequest, "cdcsCreateCaseFailure_400.json")
              case CdcsCreateCaseRequestLastName("STUB_FAILURE_422") =>
                buildResponseFromFileAndStatus(testDataPackage, UnprocessableEntity, "cdcsCreateCaseFailure_422.json")
              case _ => buildResponseFromFileAndStatus(testDataPackage, Ok, "cdcsCreateCaseSuccessResponse.json")
            }
          }
          .getOrElse(NotFound("file not found"))
      }
    }
  }

  def cesaCreateRequest(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCreateRequest] { req =>
      val testDataPackage = "/cesa.createRequest/"

      val maybeUtrIdentifier = req.identifications.find(_.idType == "UTR").map(_.idValue)
      val startDate = req.ttpStartDate

      enactStageRepository.addCESAStage(getCorrelationIdHeader(request.headers), req).map { _ =>
        maybeUtrIdentifier
          .flatMap(utr => buildResponseFromFileAndStatus(testDataPackage, Ok, s"$utr.json"))
          .orElse {
            maybeUtrIdentifier match {
              case Some("1062431399") =>
                buildResponseFromFileAndStatus(
                  testDataPackage,
                  InternalServerError,
                  "cesaCreateRequestFailure_400.json"
                )
              case Some("3193095982") =>
                buildResponseFromFileAndStatus(testDataPackage, BadRequest, "cesaCreateRequestFailure_400.json")
              case _ => None
            }
          }
          .orElse {
            startDate match {
              case Some("2019-06-08") =>
                buildResponseFromFileAndStatus(testDataPackage, BadGateway, "cesaCreateRequestFailure_502.json")
              case Some("2020-06-08") =>
                buildResponseFromFileAndStatus(testDataPackage, BadRequest, "cesaCreateRequestFailure_400.json")
              case Some("2021-06-08") =>
                buildResponseFromFileAndStatus(testDataPackage, Conflict, "cesaCreateRequestFailure_409.json")
              case Some("2025-06-01") =>
                buildResponseFromFileAndStatus(testDataPackage, NotFound, "cesaCreateRequestFailure_404.json")
              case _ => buildResponseFromFileAndStatus(testDataPackage, Ok, "cesaCreateRequestSuccessResponse.json")
            }
          }
          .getOrElse(NotFound("file not found"))
      }
    }
  }

  private def buildResponseFromFileAndStatus(testDataPackage: String, responseStatus: Status, fileName: String) =
    findFile(testDataPackage, fileName) match {
      case Some(file) =>
        val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
        Try(Json.parse(fileString)).toOption match {
          case Some(jsValue) => Some(responseStatus(jsValue))
          case None          => Some(InternalServerError(s"failing stub cannot parse file $fileName"))
        }
      case None => None
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
        logger.error(s"Status $NOT_FOUND, message: file not found $path FileName: $fileName")
        Future successful NotFound(s"file not found Path: $path FileName: $fileName")
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
