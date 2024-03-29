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
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.debtmanagment.{ FCTemplateRequest, RaiseAmendFeeRequest }
import uk.gov.hmrc.debttransformationstub.services.DebtManagementAPIPollingService
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.io.Source

@Singleton()
class DebtManagementAPITestController @Inject() (
  appConfig: AppConfig,
  cc: ControllerComponents,
  pollingService: DebtManagementAPIPollingService,
  environment: Environment
)(implicit executionContext: ExecutionContext)
    extends BackendController(cc) {
  import RaiseAmendFeeRequest._

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data"

  def fieldCollectionsCharge(idType: String, idValue: String): Action[RaiseAmendFeeRequest] =
    Action.async(parse.tolerantJson[RaiseAmendFeeRequest]) { implicit request =>
      if (appConfig.isPollingEnv)
        request.headers.get("CorrelationId") match {
          case Some(correlationId) =>
            pollingService
              .insertFCChargeRequestAndServeResponse(
                Json.toJson(request.body),
                correlationId,
                request.method,
                uri = request.uri
              )
              .map {
                case Some(response) =>
                  Status(response.status.getOrElse(200))(response.content)
                case None =>
                  ServiceUnavailable
              }
          case None =>
            Future.successful(BadRequest(Json.obj("message" -> "missing CorrelationId header")))
        }
      else
        environment.getExistingFile(s"$basePath/dm.raiseAmendFee/charge-$idType-$idValue.json") match {
          case None =>
            logger.error(s"Status $NOT_FOUND, message: file not found")
            Future.successful(NotFound("file not found"))
          case Some(file) =>
            val result = Source.fromFile(file).mkString.stripMargin
            Future.successful(Ok(result))
        }
    }

  def getDebtDataAndDWISignals(wmfId: String): Action[AnyContent] = Action.async { implicit request =>
    if (appConfig.isPollingEnv)
      pollingService.insertRequestAndServeResponse(Json.obj(), request.uri).map {
        case Some(response) => Status(response.status.getOrElse(200))(response.content)
        case None           => ServiceUnavailable
      }
    else
      environment.getExistingFile(s"$basePath/dm/subcontractor/wmfId.json") match {
        case None =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future.successful(NotFound("file not found"))
        case Some(file) =>
          val result = Source.fromFile(file).mkString.stripMargin
          Future.successful(Ok(result))
      }
  }

  def getTaxpayerData(idKey: String): Action[AnyContent] = Action.async { implicit request =>
    if (appConfig.isPollingEnv)
      pollingService.insertTaxpayerRequestAndServeResponse().map {
        case Some(response) => Status(response.status.getOrElse(200))(response.content)
        case None           => ServiceUnavailable
      }
    else
      environment.getExistingFile(s"$basePath/dm/subcontractor/idKey.json") match {
        case None =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future.successful(NotFound("file not found"))
        case Some(file) =>
          val result = Source.fromFile(file).mkString.stripMargin
          Future.successful(Ok(result))
      }
  }

  def fieldCollectionsTemplates(): Action[FCTemplateRequest] =
    Action.async(parse.tolerantJson[FCTemplateRequest]) { implicit request =>
      if (appConfig.isPollingEnv) {
        request.headers.get("CorrelationId") match {
          case Some(correlationId) =>
            pollingService
              .insertTemplateRequestAndServeResponse(Json.toJson(request.body), correlationId, uri = request.uri)
              .map {
                case Some(response) =>
                  Status(response.status.getOrElse(200))(response.content)
                case None => ServiceUnavailable
              }
          case None =>
            Future.successful(BadRequest(Json.obj("message" -> "missing CorrelationId header")))
        }

      } else
        environment.getExistingFile(s"$basePath/dm.template/fc_template.json") match {
          case None =>
            logger.error(s"Status $NOT_FOUND, message: file not found")
            Future.successful(NotFound("file not found"))
          case Some(file) =>
            val result = Source.fromFile(file).mkString.stripMargin
            Future.successful(Ok(result))
        }
    }
}
