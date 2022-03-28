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
import java.io.File
import javax.inject.Inject
import play.api.Environment
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents, Request }
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.casemanagement.CreateCaseRequest
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.Future
import scala.io.Source

class CaseManagementController @Inject()(
  environment: Environment,
  cc: ControllerComponents
) extends BackendController(cc) with BaseController {
  private lazy val logger = new RequestAwareLogger(this.getClass)

  private val basePath = "conf/resources/data"

  def createCase: Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    withCustomJsonBody[CreateCaseRequest] { createCaseRequest =>
      val fileMaybe: Option[File] = environment.getExistingFile(
        s"$basePath/casemanagement.createcase/create-case-response.json"
      )

      fileMaybe match {
        case None =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future successful NotFound("file not found")
        case Some(file) =>
          val result = Source.fromFile(file).mkString.replaceAll("#PLAN-ID", createCaseRequest.planId.value).stripMargin
          Future successful Ok(result)
      }
    }
  }
}
