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

import play.api.Environment
import play.api.libs.json.{ JsError, JsSuccess, JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents, Request }
import uk.gov.hmrc.debttransformationstub.models.errors.NO_RESPONSE
import uk.gov.hmrc.debttransformationstub.models.{ CesaData, CustomerDataRequest, Identity }
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.Future
import scala.io.Source
import scala.util.{ Failure, Success, Try, Using }

class CESAController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data/cesa"
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getCESAdata(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    withCustomJsonBody[CesaData] { request =>
      val fileName: String = request.debitIdentifiers.head.UTR
      val relativePath = s"$basePath" + "/" + s"$fileName.json"
      environment.getExistingFile(relativePath) match {
        case None =>
          val message = s"file [$relativePath] not found"
          logger.error(s"Status $NOT_FOUND, message: $message")
          Future successful NotFound(message)
        case Some(file) =>
          val maybeFileContent: Try[String] =
            Using(Source.fromFile(file))(source => source.mkString)
              .recoverWith { case ex: Throwable =>
                // Explain which file failed to be read.
                Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
              }

          maybeFileContent match {
            case Success(value) =>
              // Might throw if parsing fails
              Future.successful(Ok(Json.parse(value)))
            case Failure(exception) =>
              logger.error(s"Failed to parse the file $file", exception)
              Future.successful(InternalServerError(s"Stub failed to parse file $file"))
          }
      }
    }
  }

  def saCustomerData(): Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[CustomerDataRequest] match {
      case JsError(errors) =>
        BadRequest(s"Unable to parse to CustomerDataRequest: $errors")
      case JsSuccess(value, _) =>
        val fileName: String = value.identifications
          .getOrElse(List.empty[Identity])
          .find { case Identity(idType, _) => idType == "UTR" }
          .map(_.idValue)
          .get

        if (fileName.isEmpty) {
          NotFound("IdValue for UTR not provided")
        } else {
          val relativePath = s"$basePath" + "/" + s"$fileName.json"
          environment.getExistingFile(relativePath) match {
            case Some(file) =>
              Try(Json.parse(saCustomerDataString(file))) match {
                case Success(value) => Ok(value)
                case Failure(exception) =>
                  logger.error(s"Failed to parse the file $relativePath", exception)
                  InternalServerError(s"stub failed to parse file $relativePath")
              }
            case _ =>
              NotFound("file not found")
          }
        }
    }
  }

  private def saCustomerDataString(file: File): String = {
    val currentDate = LocalDate.now()

    val responseTemplate: String =
      Using(Source.fromFile(file))(source => source.mkString).recoverWith { case ex: Throwable =>
        // Explain which file failed to be read.
        Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
      }.get // Can throw.

    val dueDateInPast = currentDate.minusDays(24)
    val dueDateToday = currentDate
    val dueDateInFuture = currentDate.plusDays(24)

    val result =
      responseTemplate
        .replaceAll("<DUE_DATE>", dueDateInPast.format(dateFormatter))
        .replaceAll("<DUE_DATE_TODAY>", dueDateToday.format(dateFormatter))
        .replaceAll("<DUE_DATE_FOR_FUTURE>", dueDateInFuture.format(dateFormatter))

    println(
      s"""====================
         |$result
         |====================
         |""".stripMargin
    )
    result
  }

  def cesaData(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    withCustomJsonBody[CesaData] { request =>
      val fileName = s"$basePath.cesaData/${request.debitIdentifiers}.json"
      environment.getExistingFile(fileName) match {
        case _ if request.debitIdentifiers.exists(_.chargeReference == "cesaProvideChargeReferences") =>
          Future.successful(GatewayTimeout(Json.parse(NO_RESPONSE.jsonErrorCause)))
        case None =>
          val message = s"file [$fileName] not found"
          logger.error(s"Status $NOT_FOUND, message: $message")
          Future successful NotFound(message)
        case Some(file) =>
          val maybeFileContent: Try[String] =
            Using(Source.fromFile(file))(source => source.mkString)
              .map { responseBody =>
                logger.info(s"""Responding from CESA with body from file $file :\n$responseBody""")
                responseBody
              }
              .recoverWith { case ex: Throwable =>
                // Explain which file failed to be read.
                Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
              }

          maybeFileContent match {
            case Success(value) =>
              // Might throw if parsing fails
              Future.successful(Ok(Json.parse(value)))
            case Failure(exception) =>
              logger.error(s"Failed to parse the file $file", exception)
              Future.successful(InternalServerError(s"Stub failed to parse file $file"))
          }
      }
    }
  }
}
