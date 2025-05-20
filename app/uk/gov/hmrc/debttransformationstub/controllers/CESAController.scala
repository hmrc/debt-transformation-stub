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
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.debttransformationstub.models.errors.NO_RESPONSE
import uk.gov.hmrc.debttransformationstub.models.{CustomerDataRequest, Identity, IdmsRequestForSa, PaymentPlanEligibilityDmRequest, cesaRequestForSa}
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

class CESAController @Inject() (environment: Environment, cc: ControllerComponents)
  extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data/cesa"


  def cesaData(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    withCustomJsonBody[cesaRequestForSa] { request =>
      val fileName = s"$basePath.cesaDebitIdentifier/${request.idValue}.json"
      environment.getExistingFile(fileName) match {
        case _ if request.idValue.equals("chargeReferences") =>
          Future.successful(GatewayTimeout(Json.parse(NO_RESPONSE.jsonErrorCause)))
        case None =>
          val message = s"file [$fileName] not found"
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
}