/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.debttransformationstub.models.FirstContactDateRequest
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Try

class FirstContactDateController @Inject() (
  cc: ControllerComponents,
  environment: Environment
) extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data"

  def firstContactDate(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val correlationId = getCorrelationIdHeader(request.headers)
    logger.info(s"[DEBUG] firstcontactdate called with correlationId=$correlationId")
    withCustomJsonBody[FirstContactDateRequest] { req =>
      logger.info(s"Request body for firstContactDate: ${request.body}")
      val path = "/firstContactDate/"

      val maybeUtrIdNumber: Option[String] =
        (for {
          iDType   <- req.iDType
          iDNumber <- req.iDNumber
          if iDType.equalsIgnoreCase("UTR") && iDNumber.nonEmpty
        } yield iDNumber)
          .orElse(
            for {
              iDType   <- req.iDType
              iDNumber <- req.iDNumber
              if iDType.equalsIgnoreCase("NINO") && iDNumber.nonEmpty
            } yield iDNumber
          )

      val maybeFirstContactDateBusinessError: Option[String] = req.chargeReference.head match {
        case s"error_$code"  => Some(s"error_$code")
        case s"server_error" => Some("server_error")
      }
      val fileId = maybeFirstContactDateBusinessError.getOrElse(maybeUtrIdNumber.getOrElse("firstContactDateSuccess"))
      logger.info(s"Maybe UTR/NINO provided: $maybeUtrIdNumber")

      def respond(fileName: String, status: ResultStatus): Either[FileNotFoundError, Result] = {
        logger.info(s"Preparing cancel response for file: $fileName with status: ${status.header.status}")
        val requestedCode = status.header.status

        constructResponse(path, fileName).map { baseResult =>
          baseResult.copy(header = baseResult.header.copy(status = requestedCode))
        }
      }

      val maybeResultByIdType: Either[FileNotFoundError, Result] = fileId match {
        case "firstContactDate_eligibility_error_422" =>
          respond("firstContactDate_eligibility_error_422.json", Results.UnprocessableEntity)
        case "firstContactDate_chargeInfo_error_422" =>
          respond("firstContactDate_chargeInfo_error_422.json", Results.UnprocessableEntity)
        case s"error_$code" =>
          respond(s"firstContactDate_eligibility_error_$code.json", Results.UnprocessableEntity)
        case "server_error" =>
          respond("firstContactDate_error_500.json", Results.InternalServerError)
        case utr =>
          respond(s"$utr.json", Results.Created)
        case undefinedValue =>
          Left(FileNotFoundError(s"File not found $undefinedValue"))
      }

      Future.successful(
        maybeResultByIdType match {
          case Left(error)   => Results.NotFound(s"File not found: $error")
          case Right(result) => result
        }
      )
    }
  }

  private def constructResponse(path: String, fileName: String)(implicit
    hc: HeaderCarrier
  ): Either[FileNotFoundError, Result] =
    findFile(path, fileName) map { file =>
      val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())

      Try(Json.parse(fileString)).toOption match {
        case Some(fileJson) =>
          logger.info(s"""constructResponse() → Successfully parsed fileName: $fileName
                         |Returning body:
                         |$fileJson
                         |""".stripMargin)
          Results.Ok(fileJson)
        case None =>
          logger.info(s"constructResponse() → Failed to parse fileName: $fileName")
          Results.InternalServerError(s"stub failed to parse file $path$fileName")
      }
    }

  private final case class FileNotFoundError(msg: String)

  private def findFile(path: String, fileName: String): Either[FileNotFoundError, File] = {
    val combinedPath = s"$basePath$path$fileName"
    environment.getExistingFile(combinedPath).toRight(FileNotFoundError(s"File not found for path: $path"))
  }

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))
}
