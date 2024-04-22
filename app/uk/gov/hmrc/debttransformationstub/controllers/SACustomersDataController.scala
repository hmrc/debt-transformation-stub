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
import play.api.mvc.{ Action, ControllerComponents }
import uk.gov.hmrc.debttransformationstub.models.{ CustomerDataRequest, Identity }
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import javax.inject.Inject
import scala.io.Source
import scala.util.{ Failure, Success, Try, Using }
class SACustomersDataController @Inject()(environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) {

  private lazy val logger = new RequestAwareLogger(this.getClass)

  private val basePath = "conf/resources/data/sa"

  def saCustomerData(): Action[JsValue] = Action(parse.json) { implicit request =>
    request.body.validate[CustomerDataRequest] match {
      case JsError(errors) =>
        BadRequest(s"Unable to parse to CustomerDataRequest: $errors")
      case JsSuccess(value, _) =>
        val fileName: String = value.identifications
          .getOrElse(List.empty[Identity])
          .find { case Identity(idType, _) => idType == 73 }
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

  private def saCustomerDataString(file: File): String =
    Using(Source.fromFile(file))(source => source.mkString).recoverWith {
      case ex: Throwable =>
        // Explain which file failed to be read.
        Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
    }.get // Can throw.

}
