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

import play.api.Environment
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.io.Source

class ETMPController @Inject()(environment: Environment, cc: ControllerComponents) extends BackendController(cc) {

  private val getETMPResponse = "conf/resources/data/etmp.eligibility/864FZ00049"
  private val basePath = "conf/resources/data/etmp.eligibility/"

  def getEligibilityRequest(): Action[AnyContent] = Action { request =>
    environment.getExistingFile(s"$getETMPResponse.json") match {
      case Some(file) =>
        Ok(Source.fromFile(file).mkString)
      case _ =>
        NotFound("file not found")
    }

  }

  def paymentPlanEligibility(
    regimeType: String,
    idType: String,
    idValue: String,
  ): Action[AnyContent] = Action { request =>
    val queryKeys: List[String] =
      List("showIds", "showAddresses", "showSignals", "showFiling", "showCharges", "addressFromDate")
    val queries: Map[String, Option[String]] = queryKeys.map(key => (key, request.getQueryString(key))).toMap
    queries("showIds")
    environment.getExistingFile(s"$basePath" + s"$idValue.json") match {
      case Some(file) =>
        Ok(paymentPlanEligibilityString(file))
      case _ =>
        NotFound("file not found")
    }
  }

  def paymentPlanEligibilityString(file: File): String = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dueDate = LocalDate.now().minusDays(24).toString
    val responseTemplate = Source.fromFile(file).mkString

    responseTemplate.replaceAll("<DUE_DATE>", LocalDate.parse(dueDate, formatter).toString)
  }

}

object ETMPController {
  val SingleErrorIdNumber = "012X012345"
  val MultipleErrorsIdNumber = "023X023456"
}
