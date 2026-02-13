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
import play.api.http.ContentTypes
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.Results.{ Status => ResultStatus }
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes.{ CdcsCreateCaseRequestIdTypeReference, CdcsCreateCaseRequestLastName }
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
          case Some(v) => Status(v.status.getOrElse(201))(v.content)
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
            Future successful Created(result)
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
          case Some(v) => Status(v.status.getOrElse(201))(v.content)
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
            Future successful Created(result)
        }
      }
    }
  }

  def nddsEnactArrangement: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val correlationId = getCorrelationIdHeader(request.headers)

    withCustomJsonBody[NDDSRequest] { req =>
      val services = req.paymentPlan.paymentPlanCharges.map(_.hodService).toSet

      def needId(pred: Identification => Boolean, missingMsg: String): Either[Result, String] =
        req.identification.find(pred).map(_.idValue).toRight(BadRequest(missingMsg))

      def fileResponse(id: String): Result =
        constructResponseHandlingNotFound("/ndds.enactArrangement/", s"$id.json")

      val idEither: Either[Result, String] =
        if (services.contains("PAYE"))
          needId(_.idType.equalsIgnoreCase("BROCS"), "BROCS id is required for PAYE NDDS enact arrangement")
        else if (services.contains("VAT"))
          needId(_.idType.equalsIgnoreCase("VRN"), "VRN id is required for VAT NDDS enact arrangement")
        else if (services.contains("SAFE"))
          needId(
            id => id.idType.equalsIgnoreCase("NINO") || id.idType.equalsIgnoreCase("BROCS"),
            "NINO or BROCS id is required for SIMP or PAYE NDDS enact arrangements"
          )
        else if (services.contains("CESA"))
          needId(_.idType.equalsIgnoreCase("UTR"), "NINO or UTR id is required for SA NDDS enact arrangements")
        else
          Left(
            BadRequest(
              "Either BROCS, VRN, SAFE or UTR id types are required for PAYE, VAT, SIMP or SA enact arrangements"
            )
          )

      idEither match {
        case Left(errorResult) => Future.successful(errorResult)
        case Right(id) =>
          enactStageRepository.addNDDSStage(correlationId, req).map(_ => fileResponse(id))
      }
    }
  }

  def pegaUpdateCase(caseId: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val maybeAuthToken: Option[String] = request.headers.get("Authorization")
    val isSchedulerCall = maybeAuthToken.contains("Bearer scheduled-pega-access-token")
    if (isSchedulerCall) {
      val correlationId = getCorrelationIdHeader(request.headers)
      withCustomJsonBody[UpdateCaseRequest] { req =>
        enactStageRepository
          .addPegaStage(correlationId, req) // Future[Unit]
          .map { _ =>
            constructResponseHandlingNotFound("/pega.updateCase/", s"$caseId.json")
          }
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
      enactStageRepository
        .addETMPStage(correlationId, req) // Future[Unit]
        .map { _ =>
          constructResponseHandlingNotFound("/etmp.executePaymentLock/", s"${req.idValue}.json")
        }
    }
  }

  def idmsCreateTTPMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CreateIDMSMonitoringCaseRequest] { req =>
      val idValue = req.idValue

      val reference =
        if (Option(req.ddiReference).exists(_.trim.nonEmpty)) req.ddiReference
        else "ddiReference"

      logger.info(
        s"Received request to create IDMS monitoring case with idValue: $idValue and reference: $reference"
      )

      enactStageRepository
        .addIDMSStage(idValue, req)
        .map { _ =>
          constructResponseHandlingNotFound("/idms.createTTPMonitoringCase/", s"$reference.json")
        }
    }
  }

  def idmsCreateSAMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.info(s"Request body for idmsCreateSAMonitoringCase: ${request.body}")
    logger.info(s"Request headers for idmsCreateSAMonitoringCase: ${request.headers}")

    withCustomJsonBody[CreateIDMSMonitoringCaseRequestSA] { req =>
      logger.info(
        s"Received request to create SA IDMS monitoring case with idValue: ${req.idValue}"
      )
      val idValue = req.idValue

      enactStageRepository
        .addIDMSStageSA(idValue, req)
        .map { _ =>
          constructResponseHandlingNotFound("/idms.createSAMonitoringCase/", s"${req.idValue}.json")
        }
    }
  }

  // Call made to CESA for the route: /cancel of time-to-pay
  def cesaCancelCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCancelPlanRequest] { req =>
      val testDataPackage = "/cesa.cancelCase/"

      // Identify the UTR or single identifier
      val maybeUtrIdentifier: Option[String] =
          req.identifications.find(_.idType == "UTR").map(_.idValue)

      // Build the response with the desired status
      def respond(fileName: String, status: ResultStatus): Result = {
        logger.info(s"Preparing cancel response for file: $fileName with status: ${status.header.status}")
        val requestedCode = status.header.status

        val initialResult = constructResponseHandlingNotFound(testDataPackage, fileName)
        initialResult.copy(header = initialResult.header.copy(status = requestedCode))
      }

      // Apply desired response status based on UTR
      val byUtr: Option[Result] = maybeUtrIdentifier.map {
        case "cesaCancelPlan_error_400" =>
          respond("cesaCancelPlan_error_400.json", Results.BadRequest)
        case "cesaCancelPlan_error_409" =>
          respond("cesaCancelPlan_error_409.json", Results.Conflict)
        case "6642083101" =>
          respond("cesaCancelPlan_error_500.json", Results.InternalServerError)
        case "1101733108" =>
          respond("cesaCancelPlan_error_500.json", Results.InternalServerError)
        case "9831098765" =>
          respond("cesaCancelPlan_error_500.json", Results.InternalServerError)
        case "cesaCancelPlan_error_502" =>
          respond("cesaCancelPlan_error_502.json", Results.BadGateway)
        case utr =>
          respond(s"$utr.json", Results.Ok)
      }

      // Return the appropriate stubbed response
      Future.successful(byUtr.getOrElse(Results.BadRequest("UTR missing")))
    }
  }

  def cdcsCreateCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CdcsCreateCaseRequest] { req =>
      val testDataPackage = "/cdcs.createCase/"

      val identifications = req.TTP.customer.individual.identifications
      val maybeUtrIdentifier: Option[String] =
        identifications.find(_.idType == CdcsCreateCaseRequestIdTypeReference.UTR).map(_.idValue.value)

      val lastName: CdcsCreateCaseRequestLastName = req.TTP.customer.individual.lastName
      logger.info(s"CDCS create case identifiers are: ${identifications.mkString(", ")}")

      def fromLastName: Result = {
        lastName match {
          case CdcsCreateCaseRequestLastName("STUB_FAILURE_500") =>
            Results.InternalServerError("intentional stubbed 500")

          case CdcsCreateCaseRequestLastName("STUB_FAILURE_400") =>
            val result = constructResponseHandlingNotFound(testDataPackage, "cdcsCreateCaseFailure_400.json")
            result.copy(header = result.header.copy(status = BAD_REQUEST))

          case CdcsCreateCaseRequestLastName("STUB_FAILURE_422") =>
            val result = constructResponseHandlingNotFound(testDataPackage, "cdcsCreateCaseFailure_422.json")
            result.copy(header = result.header.copy(status = UNPROCESSABLE_ENTITY))

          case _ =>
            val result = constructResponseHandlingNotFound(testDataPackage, "cdcsCreateCaseSuccessResponse.json")
            result.copy(header = result.header.copy(status = OK))
        }
      }

      identifications match {
        case Nil => Future.successful(Results.BadRequest("No identifications supplied"))
        case ::(head, _) =>
          enactStageRepository.addCDCSStage(idValue = head.idValue.value, req).map { _ =>
            maybeUtrIdentifier match {
              case Some(identifier) =>
                identifier match {
                  case "3145760528" =>
                    Results.InternalServerError("intentional stubbed 500")
                  case "2001234567" =>
                    constructResponseHandlingNotFound(testDataPackage, "2001234567.json")

                  case "3153830017" => Results.InternalServerError("intentional stubbed 500")
                  case "3145760528" => Results.InternalServerError("intentional stubbed 500")
                  case s if s.startsWith("cdcsResponse_error_") =>
                    val code = s.stripPrefix("cdcsResponse_error_").takeWhile(_.isDigit).toInt
                    Results.Status(code)("intentional stubbed error")
                  case _ =>
                    fromLastName
                }
              case None =>
                fromLastName
            }
          }
      }
    }
  }

  // Call made to CESA for the routes: /inform and /full-amend of time-to-pay
  def cesaCreateRequest(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCreateRequest] { req =>
      val testDataPackage = "/cesa.createRequest/"
      val maybeUtrIdentifier: Option[String] =
        req.identifications.find(_.idType == "UTR").map(_.idValue) match {
          case None => None
          case Some(value) =>
            if (value.length > 0)
              Some(value)
            else
              None
        }

      val startDate = req.ttpStartDate

      def respond(fileName: String, status: ResultStatus): Either[FileNotFoundError, Result] = {
        logger.info(s"Preparing response for file: $fileName with status: ${status.header.status}")
        val requestedCode = status.header.status
        val initialResult: Either[FileNotFoundError, Result] = constructResponse(testDataPackage, fileName)

        initialResult.map {
          value => value.copy(header = value.header.copy(status = requestedCode))
        }
      }

      enactStageRepository.addCESAStage(getCorrelationIdHeader(request.headers), req).map { _ =>

        val maybeByUtr: Option[Either[FileNotFoundError, Result]] = maybeUtrIdentifier.map {
          case "1062431399" => respond("cesaCreateRequestFailure_400.json", Results.InternalServerError)
          case "3193095982" => respond("cesaCreateRequestFailure_400.json", Results.BadRequest)
          case "8625159625" => respond("8625159625.json", Results.UnprocessableEntity)
          case utr =>
            respond(s"$utr.json", Results.Ok)
        }

        def maybeByStartDate: Either[FileNotFoundError, Result] = {
          startDate match {
            case Some("2019-06-08") => respond("cesaCreateRequestFailure_502.json", Results.BadGateway)
            case Some("2020-06-08") => respond("cesaCreateRequestFailure_400.json", Results.BadRequest)
            case Some("2021-06-08") => respond("cesaCreateRequestFailure_409.json", Results.Conflict)
            case Some("2025-06-01") => respond("cesaCreateRequestFailure_404.json", Results.NotFound)
            case _ => respond("cesaCreateRequestSuccessResponse.json", Results.Ok)
          }
        }

        maybeByUtr match {
          case None =>
            maybeByStartDate match {
              case Left(error) => Results.NotFound(s"UTR is missing and could not find file from startDate\n errors:\n$error")
              case Right(value) => value
            }
          case Some(Left(error1)) =>
            maybeByStartDate match {
              case Left(error2) => Results.NotFound(s"Could not find file from UTR or startDate\n errors:\n $error1\n$error2")
              case Right(value) => value
            }
          case Some(Right(value)) =>
            value
        }
      }
    }
  }

  def enactStage(correlationId: String): Action[AnyContent] = Action.async { request =>
    enactStageRepository.findByCorrelationId(correlationId).map { stage: Option[EnactStage] =>
      Ok(Json.toJson(stage))
    }
  }

  def enactStageByIdValue(idValue: String): Action[AnyContent] = Action.async { request =>
    enactStageRepository.findByIdValue(idValue).map { stage: Option[EnactStage] =>
      Ok(Json.toJson(stage))
    }
  }

  final case class FileNotFoundError(msg: String)

  private def findFile(path: String, fileName: String): Either[FileNotFoundError, File] = {
    val combinedPath = s"$basePath$path$fileName"
    environment.getExistingFile(combinedPath).toRight(FileNotFoundError(s"File not found for path: $path"))
  }

  // Call made to time-to-pay for the routes: /cancel, /inform and /full-amend of time-to-pay-proxy
  def proxyPlanCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[TimeToPayProxyPlanRequest] { req =>
      val testDataPackage = "/ttp.proxy/"

      // Identify the UTR
      val maybeUtrIdentifier: Option[String] =
          req.identifications.find(_.idType == "UTR").map(_.idValue)

      // Build the response with the desired status
      def respond(fileName: String, status: ResultStatus): Result = {
        logger.info(s"Preparing cancel response for file: $fileName with status: ${status.header.status}")
        val requestedCode = status.header.status
        val result = constructResponseHandlingNotFound(testDataPackage, fileName)
        result.copy(header = result.header.copy(status = requestedCode))
      }

      // Apply desired response status based on UTR
      val maybeByUtr: Option[Result] = maybeUtrIdentifier.map {
        case "proxyPlan_error_400" =>
          respond("proxyPlan_error_400.json", Results.BadRequest)
        case "proxyPlan_error_500" =>
          respond("proxyPlan_error_500.json", Results.InternalServerError)
        case utr =>
          respond(s"$utr.json", Results.Ok)
      }

      // Return the appropriate stubbed response
      Future.successful(maybeByUtr.getOrElse(Results.BadRequest("UTR not provided")))
    }
  }

  /** Temporary handling of certain cases to support E2E testing */
  object EndToEndTestDataHandling {

    def handlePA400(fileName: String)(implicit hc: HeaderCarrier): Right[Nothing, Result] = {
      logger.info("FileName: " + fileName + " starts with PA400. Returning 400 Bad Request.")
      val msg = Json.obj(
        "errors" -> Json.obj(
          "processingDateTime" -> "2024-04-11T10:07:55.749038Z",
          "code" -> "BAD_REQUEST",
          "text" -> "idType: must match \"^[A-Z0-9]{1,6}$\""
        )
      )
      logger.info(s"Status $BAD_REQUEST, message: ${Json.stringify(msg)}")
      Right(Results.BadRequest(msg).as(ContentTypes.JSON))
    }

    def handlePA422(fileName: String)(implicit hc: HeaderCarrier): Right[Nothing, Result] = {
      logger.info("FileName: " + fileName + " starts with PA422. Returning 422 Not Found.")
      val msg = Json.obj(
        "errors" -> Json.obj(
          "processingDateTime" -> "2024-04-11T10:09:21.750575Z",
          "code"               -> "UNPROCESSABLE_ENTITY",
          "text" -> "Error: originalChargeCreationDate, originalChargeType & originalTieBreaker all need to be populated if chargeType has value LPI"
        )
      )
      logger.info(s"Status $UNPROCESSABLE_ENTITY, message: ${Json.stringify(msg)}")
      Right(Results.UnprocessableEntity(msg).as(ContentTypes.JSON))
    }

    def handlePA404(fileName: String)(implicit hc: HeaderCarrier): Right[Nothing, Result] = {
      val msg = "FileName: " + fileName + " starts with PA404. Returning 404 Not Found."
      logger.error(s"Status $NOT_FOUND, message: $msg")
      Right(Results.NotFound(msg))
    }

  }

  private def constructResponse(path: String, fileName: String)(implicit hc: HeaderCarrier): Either[FileNotFoundError, Result] = {
    logger.info(s"constructResponse() → Attempting to match on prefix: $path$fileName")

    if (fileName.startsWith("PA400")) {
      EndToEndTestDataHandling.handlePA400(fileName)
    } else if (fileName.startsWith("PA422")) {
      EndToEndTestDataHandling.handlePA422(fileName)
    } else if (fileName.startsWith("PA404")) {
      EndToEndTestDataHandling.handlePA404(fileName)
    } else {
      logger.info(s"constructResponse() →  No match on prefix. Looking for file: $path$fileName")
      findFile(path, fileName).map { file =>
        val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
        logger.info(s"constructResponse() → Reading file: $path$fileName, content:\n$fileString")

        if (fileName.startsWith("200")) {
          // 200 files: OK with JSON if parsable, else OK with raw text
          logger.info(s"constructResponse() → FileName starts with 200, attempting to parse JSON")
          Try(Json.parse(fileString)).toOption.map(Results.Ok(_)).getOrElse(Results.Ok(fileString))
        } else {
          // Others: OK with JSON if parsable, else 500
          logger.info(s"constructResponse() → Attempting to parse JSON")
          Try(Json.parse(fileString)).toOption
            .map(Results.Ok(_))
            .getOrElse(Results.InternalServerError(s"stub failed to parse file $path$fileName"))
        }
      }
    }
  }

  private def constructResponseHandlingNotFound(path: String, fileName: String)(implicit hc: HeaderCarrier): Result =
    constructResponse(path, fileName).getOrElse(NotFound(s"Could not find file from $path/$fileName"))

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
