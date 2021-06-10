/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.JsValue
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.models.{CreatePlanRequest, GenerateQuoteRequest}

import scala.concurrent.Future
import scala.io.Source

class TimeToPayController @Inject()(environment: Environment, cc: ControllerComponents)
  extends BackendController(cc) with BaseController {
  private val basePath = "conf/resources/data"

  def generateQuote: Action[JsValue] = Action.async(parse.json) { implicit request => {
    withCustomJsonBody[GenerateQuoteRequest] { req =>
      val fileMaybe: Option[File] = environment.getExistingFile(s"$basePath/ttp.generateQuote/${req.customerReference}.json")

      fileMaybe match {
        case None => Future successful NotFound("file not found")
        case Some(file) =>
          val result = Source.fromFile(file).mkString.stripMargin
          Future successful Ok(result)
      }
    }
  }
  }

  def getExistingQuote(customerReference: String, pegaId: String) = Action { implicit request =>
    environment.getExistingFile(s"$basePath/ttp.existingQuote/$pegaId.json") match {
      case Some(file) => Ok(Source.fromFile(file).mkString)
      case _ => NotFound("file not found")
    }
  }

  def updateQuote(customerReference: String, pegaId: String) = Action { implicit request =>
    environment.getExistingFile(s"$basePath/ttp.updateQuote/$customerReference.json") match {
      case Some(file) => Ok(Source.fromFile(file).mkString)
      case _ => NotFound("file not found")
    }
  }

  def createPlan = Action.async(parse.json) { implicit request => {
    withCustomJsonBody[CreatePlanRequest] { req =>
      val fileMaybe: Option[File] = environment.getExistingFile(s"$basePath/ttp.createPlan/${req.pegaPlanId}.json")

      fileMaybe match {
        case None => Future successful NotFound("file not found")
        case Some(file) =>
          val result = Source.fromFile(file).mkString.stripMargin
          Future successful Ok(result)
      }
    }
  }

  }
}
