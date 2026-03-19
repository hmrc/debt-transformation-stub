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
import play.api.mvc.{ Action, ControllerComponents, Request, Result }
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.Future
import scala.io.Source
import scala.util.{ Failure, Success, Using }

class AuthController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)

  def getAccessToken(): Action[Map[String, Seq[String]]] = Action(parse.formUrlEncoded).async {
    implicit request: Request[Map[String, Seq[String]]] =>
      val clientIdFileMapping = Map(
        "scheduler-stub-client-id"     -> "scheduled-bearer-token.json",
        "stub-client-id"               -> "non-scheduled-bearer-token.json",
        "cdcs-check-stub-client-id"    -> "cdcs-check-bearer-token.json",
        "customer-data-stub-client-id" -> "customer-data-bearer-token.json"
      )

      val maybeClientId = request.body.get("client_id").toList.flatten
      maybeClientId.headOption match {
        case Some(clientId) =>
          clientIdFileMapping
            .get(clientId)
            .map { fileName =>
              val file = s"conf/resources/data/auth/$fileName"

              Using(Source.fromFile(environment.getFile(file))) { bufferedSource =>
                bufferedSource.mkString
              } match {
                case Failure(exception) =>
                  logger.error(s"failed reading $file", exception)
                  Future successful InternalServerError(exception.getMessage)
                case Success(content) =>
                  Future successful Accepted(content)
              }
            }
            .getOrElse {
              returnClientIdNotMappedError(clientIdFileMapping, maybeClientId)
            }

        case None =>
          returnNoClientIdPassedError
      }
  }

  private def returnClientIdNotMappedError(
    clientIdFileMapping: Map[String, String],
    maybeClientId: List[String]
  )(implicit hc: HeaderCarrier): Future[Result] = {
    val errorMessage =
      s"client id was not mapped, got $maybeClientId, valid options ${clientIdFileMapping.keys}"
    logger.error(errorMessage)
    Future successful UnprocessableEntity(errorMessage)
  }

  private def returnNoClientIdPassedError(implicit hc: HeaderCarrier): Future[Result] = {
    val errorMessage = s"No client_id was passed in the payload"
    logger.error(errorMessage)
    Future successful UnprocessableEntity(errorMessage)
  }
}
