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
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc.{Action, ControllerComponents}
import play.api.Environment
import scala.io.Source

@Singleton()
class DebtManagementAPITestController @Inject() (
  cc: ControllerComponents,
  environment: Environment
)(implicit val executionContext: ExecutionContext) extends BackendController(cc) {

  private val basePath = "conf/resources/data"

  def postFieldCollectionsCharge(idType: String, idValue: String): Action[JsValue] = Action.async(parse.tolerantJson) { implicit request =>
    environment.getExistingFile(s"$basePath/dm.raiseAmendFee/charge-${idType}-${idValue}") match {
      case None => Future.successful(NotFound("file not found"))
      case Some(file) =>
        val result = Source.fromFile(file).mkString.stripMargin
        Future.successful(Ok(result))
    }

  }

}
