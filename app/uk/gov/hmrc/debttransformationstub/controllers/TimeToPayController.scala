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
        constructResponse("/ndds.enactArrangement/", s"$id.json")
          .getOrElse(NotFound(s"file not found: /ndds.enactArrangement/$id.json"))

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
            constructResponse("/pega.updateCase/", s"$caseId.json")
              .getOrElse(NotFound(s"file not found: /pega.updateCase/$caseId.json"))
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
          constructResponse("/etmp.executePaymentLock/", s"${req.idValue}.json")
            .getOrElse(NotFound(s"file not found: /etmp.executePaymentLock/${req.idValue}.json"))
        }
    }
  }

  def idmsCreateTTPMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val correlationId = getCorrelationIdHeader(request.headers)

    withCustomJsonBody[CreateIDMSMonitoringCaseRequest] { req =>
      logger.info(
        s"Received request to create IDMS monitoring case with correlationId: $correlationId and ddiReference: ${req.ddiReference}"
      )

      enactStageRepository
        .addIDMSStage(correlationId, req) // Future[Unit]
        .map { _ =>
          constructResponse("/idms.createTTPMonitoringCase/", s"${req.ddiReference}.json")
            .getOrElse(NotFound(s"file not found: /idms.createTTPMonitoringCase/${req.ddiReference}.json"))
        }
    }
  }

  def idmsCreateSAMonitoringCase: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.info(s"Request body for idmsCreateSAMonitoringCase: ${request.body}")
    logger.info(s"Request headers for idmsCreateSAMonitoringCase: ${request.headers}")

    val correlationId = getCorrelationIdHeader(request.headers)

    withCustomJsonBody[CreateIDMSMonitoringCaseRequestSA] { req =>
      logger.info(
        s"Received request to create SA IDMS monitoring case with correlationId: $correlationId and idValue: ${req.idValue}"
      )

      enactStageRepository
        .addIDMSStageSA(correlationId, req)
        .map { _ =>
          constructResponse("/idms.createSAMonitoringCase/", s"${req.idValue}.json")
            .getOrElse(NotFound(s"file not found: /idms.createSAMonitoringCase/${req.idValue}.json"))
        }
    }
  }

  def cesaCancelCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCancelPlanRequest] { req =>
      val testDataPackage = "/cesa.cancelCase/"

      val firstIdentifierOrUtr: Option[String] =
        if (req.identifications.length == 1)
          req.identifications.map(_.idValue).headOption
        else
          req.identifications.find(_.idType == "UTR").map(_.idValue)

      val response: Result = firstIdentifierOrUtr
        .flatMap {
          case "cesaCancelPlan_error_400" =>
            constructResponse(testDataPackage, "cesaCancelPlan_error_400.json")
              .map(res => res.copy(header = res.header.copy(status = BAD_REQUEST)))
          case "cesaCancelPlan_error_404" =>
            constructResponse(testDataPackage, "cesaCancelPlan_error_404.json")
              .map(res => res.copy(header = res.header.copy(status = NOT_FOUND)))
          case "cesaCancelPlan_error_409" =>
            constructResponse(testDataPackage, "cesaCancelPlan_error_409.json")
              .map(res => res.copy(header = res.header.copy(status = CONFLICT)))
          case "6642083101" =>
            constructResponse(testDataPackage, "cesaCancelPlan_error_500.json")
              .map(res => res.copy(header = res.header.copy(status = INTERNAL_SERVER_ERROR)))
          case "cesaCancelPlan_error_502" =>
            constructResponse(testDataPackage, "cesaCancelPlan_error_502.json")
              .map(res => res.copy(header = res.header.copy(status = BAD_GATEWAY)))
          case "cesaSuccessNonJSON" =>
            constructResponse(testDataPackage, "cesaSuccessNonJSON.json")
              .map(res => res.copy(header = res.header.copy(status = BAD_GATEWAY)))

          case _ =>
            constructResponse(testDataPackage, "cesaCancelPlanSuccess.json")
              .map(res => res.copy(header = res.header.copy(status = OK)))
        }
        .getOrElse(NotFound("file not found"))

      Future.successful(response)
    }
  }

  def cdcsCreateCase(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CdcsCreateCaseRequest] { req =>
      val testDataPackage = "/cdcs.createCase/"

      val identifiers = req.customer.individual.identifications
        .filter(_.idType == CdcsCreateCaseRequestIdTypeReference.UTR)
        .map(_.idValue.value)

      val lastName = req.customer.individual.lastName
      logger.info(s"CDCS create case identifiers are: ${identifiers.mkString(", ")}")

      enactStageRepository.addCDCSStage(getCorrelationIdHeader(request.headers), req).map { _ =>
        // Match on UTRs
        val byUtr: Option[Result] =
          identifiers.foldLeft(None: Option[Result]) { (acc, identifier) =>
            if (acc.isDefined) acc
            else {
              identifier match {
                case "3145760528" =>
                  Some(Results.InternalServerError("intentional stubbed 500"))
                case "2001234567" =>
                  constructResponse(testDataPackage, "2001234567.json")
                    .map(res => res.copy(header = res.header.copy(status = OK)))

                case "3153830017" => Some(Status(INTERNAL_SERVER_ERROR))
                case "3145760528" => Some(Status(INTERNAL_SERVER_ERROR))
                case s if s.startsWith("cdcsResponse_error_") =>
                  val code = s.stripPrefix("cdcsResponse_error_").takeWhile(_.isDigit).toInt
                  Some(Results.Status(code)("intentional stubbed error"))

                case _ =>
                  None
              }
            }
          }

        // Match on last name if no UTR case matched
        val finalResult: Option[Result] =
          byUtr.orElse {
            lastName match {
              case CdcsCreateCaseRequestLastName("STUB_FAILURE_500") =>
                Some(Results.InternalServerError("intentional stubbed 500"))

              case CdcsCreateCaseRequestLastName("STUB_FAILURE_400") =>
                constructResponse(testDataPackage, "cdcsCreateCaseFailure_400.json")
                  .map(res => res.copy(header = res.header.copy(status = BAD_REQUEST)))

              case CdcsCreateCaseRequestLastName("STUB_FAILURE_422") =>
                constructResponse(testDataPackage, "cdcsCreateCaseFailure_422.json")
                  .map(res => res.copy(header = res.header.copy(status = UNPROCESSABLE_ENTITY)))

              case _ =>
                constructResponse(testDataPackage, "cdcsCreateCaseSuccessResponse.json")
                  .map(res => res.copy(header = res.header.copy(status = OK)))
            }
          }
        finalResult.getOrElse(Results.NotFound("file not found"))
      }
    }
  }

  def cesaCreateRequest(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[CesaCreateRequest] { req =>
      val testDataPackage = "/cesa.createRequest/"
      val maybeUtrIdentifier = req.identifications.find(_.idType == "UTR").map(_.idValue)
      val startDate = req.ttpStartDate

      def respond(fileName: String, status: ResultStatus): Option[Result] =
        constructResponse(testDataPackage, fileName).map { res =>
          val code = status.header.status
          if (code == OK) res else res.copy(header = res.header.copy(status = code))
        }

      enactStageRepository.addCESAStage(getCorrelationIdHeader(request.headers), req).map { _ =>
        val byUtr: Option[Result] = maybeUtrIdentifier.flatMap {
          case "1062431399" => respond("cesaCreateRequestFailure_400.json", Results.InternalServerError)
          case "3193095982" => respond("cesaCreateRequestFailure_400.json", Results.BadRequest)
          case "8625159625" => respond("8625159625.json", Results.UnprocessableEntity)
          case "2001234567" => respond("2001234567.json", Results.Ok)
          case "cesaSuccessNonJSON" => respond("cesaSuccessNonJSON.json", Results.Ok)
          case utr          => respond(s"$utr.json", Results.Ok)
        }

        // 🧠 Only check startDate if there were no UTR matches
        val finalResult: Option[Result] =
          if (byUtr.isDefined) byUtr
          else {
            startDate match {
              case Some("2019-06-08") => respond("cesaCreateRequestFailure_502.json", Results.BadGateway)
              case Some("2020-06-08") => respond("cesaCreateRequestFailure_400.json", Results.BadRequest)
              case Some("2021-06-08") => respond("cesaCreateRequestFailure_409.json", Results.Conflict)
              case Some("2025-06-01") => respond("cesaCreateRequestFailure_404.json", Results.NotFound)
              case _                  => respond("cesaCreateRequestSuccessResponse.json", Results.Ok)
            }
          }
        finalResult.getOrElse(Results.NotFound("file not found"))
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

  private def constructResponse(path: String, fileName: String)(implicit hc: HeaderCarrier): Option[Result] = {

    // 🧠 Check prefixes before attempting to find or read the file
    if (fileName.startsWith("PA400")) {
      val msg = "intentional stubbed bad request"
      logger.info(s"constructResponse() → fileName starts with PA400, returning 400 Bad Request")
      logger.error(s"Status $BAD_REQUEST, message: $msg")
      return Some(Results.BadRequest(msg))
    }

    if (fileName.startsWith("PA422")) {
      val msg = "intentional stubbed unprocessable entity"
      logger.info(s"constructResponse() → fileName starts with PA422, returning 422 Unprocessable Entity")
      logger.error(s"Status $UNPROCESSABLE_ENTITY, message: $msg")
      return Some(Results.UnprocessableEntity(msg))
    }

    if (fileName.startsWith("PA404")) {
      val msg = "intentional stubbed not found"
      logger.info(s"constructResponse() → fileName starts with PA404, returning 404 Not Found")
      logger.error(s"Status $NOT_FOUND, message: $msg")
      return Some(Results.NotFound(msg))
    }

    // Look for the file if it didn’t match any special prefixes above
    findFile(path, fileName).map { file =>
      val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
      logger.info(s"constructResponse() → Reading file: $path$fileName, content:\n$fileString")

      if (fileName.startsWith("200")) {
        // 200 files for DTD-3883: OK with JSON if parsable, else OK with raw text
        Try(Json.parse(fileString)).toOption.map(Results.Ok(_)).getOrElse(Results.Ok(fileString))
      } else {
        // Others: OK with JSON if parsable, else 500
        Try(Json.parse(fileString)).toOption
          .map(Results.Ok(_))
          .getOrElse(Results.InternalServerError(s"stub failed to parse file $path$fileName"))
      }
    }
  }

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
