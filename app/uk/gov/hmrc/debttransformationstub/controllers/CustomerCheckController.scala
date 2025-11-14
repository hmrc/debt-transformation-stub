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
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents, Headers, Request }
import uk.gov.hmrc.debttransformationstub.models.CustomerCheckRequest
import uk.gov.hmrc.debttransformationstub.repositories.EnactStageRepository
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

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
  private val basePath = "conf/resources/data/customercheck"

  def customerCheck(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    val correlationId = getCorrelationIdHeader(rawRequest.headers)

    withCustomJsonBody[CustomerCheckRequest] { request =>
      // Extract identifier from the first customer in the request
      // This will be either nino or empRef based on the regime type
      val identifierMaybe = request.customers.headOption.flatMap { customer =>
        customer.nino.map(_.value).orElse(customer.empRef.map(_.value))
      }

      identifierMaybe match {
        case Some(identifier) =>
          val relativePath = s"$basePath/$identifier-encrypted.json"
          environment.getExistingFile(relativePath) match {
            case None =>
              val message = s"file [$relativePath] not found"
              logger.error(s"Status $NOT_FOUND, message: $message")
              Future successful NotFound(message)
            case Some(file) =>
              val maybeFileContent: Try[String] =
                Using(Source.fromFile(file))(source => source.mkString)
                  .recoverWith { case ex: Throwable =>
                    Failure(new RuntimeException(s"Failed to read file: ${file.getPath}", ex))
                  }

              maybeFileContent match {
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

  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
