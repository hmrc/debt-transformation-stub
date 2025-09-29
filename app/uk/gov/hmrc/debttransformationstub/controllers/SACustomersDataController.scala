/*
 * Copyright 2024 HM Revenue & Customs
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
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.debttransformationstub.models.{CustomerDataRequest, Identity}
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.debttransformationstub.controllers.CustomBaseController.returnStatusBasedOnIdValue

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}
class SACustomersDataController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) {

  private lazy val logger = new RequestAwareLogger(this.getClass)

  private val basePath = "conf/resources/data/sa"
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def saCustomerData(): Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[CustomerDataRequest] match {
      case JsError(errors) =>
        BadRequest(s"Unable to parse to CustomerDataRequest: $errors")

      case JsSuccess(req, _) =>
        // Safely extract the UTR idValue (first UTR in the identifications)
        val utrOpt: Option[String] =
          req.identifications
            .getOrElse(Nil)
            .collectFirst { case Identity("UTR", idValue) => idValue }

        utrOpt match {
          case None =>
            NotFound("IdValue for UTR not provided")

          case Some(s) if s.trim.isEmpty =>
            NotFound("IdValue for UTR not provided")

          case Some(idValue) =>
            returnStatusBasedOnIdValue("saCustomerData_error_", idValue) match {
              case Some(status) =>
                val relativePath = s"$basePath/$idValue.json"
                environment.getExistingFile(relativePath) match {
                  case Some(file) =>
                    Try(Json.parse(saCustomerDataString(file))) match {
                      case Success(js) => status(js)
                      case Failure(ex) =>
                        logger.error(s"Failed to parse the file $relativePath", ex)
                        status(Json.obj("error" -> s"stub failed to parse file $relativePath"))
                    }
                  case None =>
                    status(Json.obj("error" -> s"file not found: $relativePath"))
                }
              case None =>
                val relativePath = s"$basePath/$idValue.json"
                environment.getExistingFile(relativePath) match {
                  case Some(file) =>
                    Try(Json.parse(saCustomerDataString(file))) match {
                      case Success(js) => Ok(js)
                      case Failure(ex) =>
                        logger.error(s"Failed to parse the file $relativePath")
                        InternalServerError(s"stub failed to parse file $relativePath")
                    }
                  case None =>
                    NotFound("file not found")
                }
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

}
