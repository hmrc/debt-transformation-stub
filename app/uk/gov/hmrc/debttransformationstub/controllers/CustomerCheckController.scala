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
import play.api.mvc.{ Action, ControllerComponents, Headers, Request, Result, Results }
import uk.gov.hmrc.debttransformationstub.models.CustomerCheckRequest
import uk.gov.hmrc.debttransformationstub.repositories.EnactStageRepository
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source
import scala.util.{ Failure, Success, Try, Using }

class CustomerCheckController @Inject() (
  environment: Environment,
  cc: ControllerComponents,
  enactStageRepository: EnactStageRepository
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data/customerCheck"

  def customerCheck(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    val correlationId = getCorrelationIdHeader(rawRequest.headers)
    val testDataPackage = "/customerCheck/"

    def respond(fileName: String, status: ResultStatus): Option[Result] = {
      logger.info(s"Preparing response for file: $fileName with status: ${status.header.status}")
      constructResponse(testDataPackage, fileName).map { baseResult =>
        val requestedCode = status.header.status
        baseResult.copy(header = baseResult.header.copy(status = requestedCode))
      }
    }

    withCustomJsonBody[CustomerCheckRequest] { request =>
      // Extract identifier from the first customer in the request
      // This will be either nino or empRef based on the regime type
      val identifierMaybe = request.customers.headOption.flatMap { customer =>
        customer.nino.map(_.value).orElse(customer.empRef.map(_.value))
      }

      identifierMaybe match {
        case Some(identifier) =>
          val encryptedPath = s"$basePath/$identifier-encrypted.json"
          val plainPath = s"$basePath/$identifier.json"

          val fileOption = environment
            .getExistingFile(encryptedPath)
            .orElse(environment.getExistingFile(plainPath))

          fileOption match {
            case None =>
              val message = s"file [$encryptedPath] or [$plainPath] not found"
              logger.error(s"Status $NOT_FOUND, message: $message")
              Future successful NotFound(message)
            case Some(file) =>
              val maybeFileContent: Try[String] =
                Using(Source.fromFile(file))(source => source.mkString)
                  .recoverWith { case ex: Throwable =>
                    Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
                  }

              maybeFileContent match {
                case Success("Error400") =>
                  Future.successful(
                    respond("customerCheckFailure_400.json", Results.BadRequest).getOrElse(
                      Results.NotFound("file not found")
                    )
                  )
                case Success("Error403") =>
                  Future.successful(
                    respond("customerCheckFailure_403.json", Results.Forbidden).getOrElse(
                      Results.NotFound("file not found")
                    )
                  )
                case Success("Error500") =>
                  Future.successful(
                    respond("customerCheckFailure_500.json", Results.InternalServerError).getOrElse(
                      Results.NotFound("file not found")
                    )
                  )
                case Success("Error503") =>
                  Future.successful(
                    respond("customerCheckFailure_503.json", Results.ServiceUnavailable).getOrElse(
                      Results.NotFound("file not found")
                    )
                  )
                case Success(value) =>
                  enactStageRepository
                    .addCustomerCheckStage(correlationId, request)
                    .map(_ => Ok(Json.parse(value)))
                case Failure(exception) =>
                  logger.error(s"Failed to parse the file $file", exception)
                  Future.successful(InternalServerError(s"Stub failed to parse file $file"))
              }
          }
        case None =>
          val message = "No identifier found in request"
          logger.error(s"Status $BAD_REQUEST, message: $message")
          Future successful BadRequest(message)
      }
    }
  }

  private def findFile(path: String, fileName: String): Option[File] =
    environment.getExistingFile(s"$basePath$path$fileName")

  private def constructResponse(path: String, fileName: String)(implicit hc: HeaderCarrier): Option[Result] = {
    logger.info(s"constructResponse++++++() → Looking for file: $path$fileName")

    // Look for the file if it didn’t match any special prefixes above
    findFile(path, fileName).map { file =>
      val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())
      logger.info(
        s"constructResponse() → Reading file: $path$fileName, content:\n$fileString"
      )

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

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
