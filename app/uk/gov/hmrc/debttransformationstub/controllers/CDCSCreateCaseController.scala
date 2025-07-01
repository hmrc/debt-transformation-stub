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
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents, Request}
import uk.gov.hmrc.debttransformationstub.models.CdcsRequest
import uk.gov.hmrc.debttransformationstub.models.casemanagement.CreateCaseRequest
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import javax.inject.Inject
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try, Using}

class CDCSCreateCaseController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data/cdcs"

  def cdcsCreateCase: Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    withCustomJsonBody[CdcsCreateCaseRequest] { request =>
      val fileName: String = request.regimeType.head.idValue
      val relativePath = s"$basePath" + "/" + s"$fileName.json"
      environment.getExistingFile(relativePath) match {
      ()
      }

      fileMaybe match {
        case None =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future successful NotFound("file not found")
        case Some(file) =>
          val result = Source.fromFile(file).mkString.replaceAll("#PLAN-ID", cdcsCreateCaseRequest.planId.value).stripMargin
          Future successful Ok(result)
      }
      }
    }
  }