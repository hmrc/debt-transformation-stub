/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source

import play.api.Environment
import play.api.libs.json.Json
import play.api.mvc.{Action, ControllerComponents}

import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.debtmanagment.RaiseAmendFeeRequest
import uk.gov.hmrc.debttransformationstub.services.DebtManagementAPIPollingService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

@Singleton()
class DebtManagementAPITestController @Inject() (
  appConfig: AppConfig,
  cc: ControllerComponents,
  pollingService: DebtManagementAPIPollingService,
  environment: Environment
)(implicit val executionContext: ExecutionContext) extends BackendController(cc) {
  import RaiseAmendFeeRequest._

  private val basePath = "conf/resources/data"

  def fieldCollectionsCharge(idType: String, idValue: String): Action[RaiseAmendFeeRequest] =
    Action.async(parse.tolerantJson[RaiseAmendFeeRequest]) { request =>
      if (appConfig.isPollingEnv)
        pollingService.insertRequestAndServeResponse(Json.toJson(request.body), request.uri).map {
          case Some(response) => Status(response.status.getOrElse(200))(response.content)
          case None => ServiceUnavailable
        }
      else
        environment.getExistingFile(s"$basePath/dm.raiseAmendFee/charge-${idType}-${idValue}.json") match {
          case None => Future.successful(NotFound("file not found"))
          case Some(file) =>
            val result = Source.fromFile(file).mkString.stripMargin
            Future.successful(Ok(result))
        }
  }

}
