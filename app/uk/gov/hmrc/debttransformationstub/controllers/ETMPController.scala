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
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.io.Source
import scala.util.Try

class ETMPController @Inject() (environment: Environment, cc: ControllerComponents) extends BackendController(cc) {

  private val basePath = "conf/resources/data/etmp.eligibility"

  def paymentPlanEligibility(
    regimeType: String,
    idType: String,
    idValue: String
  ): Action[AnyContent] = Action { request =>
    val queryKeys: List[String] =
      List("showIds", "showAddresses", "showSignals", "showFiling", "showCharges", "addressFromDate")
    val queries: Map[String, Option[String]] = queryKeys.map(key => (key, request.getQueryString(key))).toMap
    queries("showIds")
    val relativePath = s"$basePath" + "." + regimeType + "/" + s"$idValue.json"
    environment.getExistingFile(relativePath) match {
      case Some(file) =>
        Try(Json.parse(paymentPlanEligibilityString(file, idValue))).toOption
          .map(Ok(_))
          .getOrElse(InternalServerError(s"stub failed to parse file $relativePath"))
      case _ =>
        NotFound("file not found")
    }
  }

  def paymentPlanEligibilityString(file: File, idValue: String): String = {

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dueDateInPast = LocalDate.now().minusDays(24).toString
    val dueDateToday = LocalDate.now().toString
    val dueDateInFuture = LocalDate.now().plusDays(24).toString
    val dueDateOverMaxDebtAge = LocalDate.now().minusDays(29).toString
    val dueDateEqualsMaxDebtAge = LocalDate.now().minusDays(28).toString
    val responseTemplate = Source.fromFile(file).mkString

    responseTemplate
      .replaceAll("<DUE_DATE>", LocalDate.parse(dueDateInPast, formatter).toString)
      .replaceAll("<DUE_DATE_TODAY>", LocalDate.parse(dueDateToday, formatter).toString)
      .replaceAll("<DUE_DATE_FOR_FUTURE>", LocalDate.parse(dueDateInFuture, formatter).toString)
      .replaceAll("<DUE_DATE_OVER_MAX_DEBT_AGE>", LocalDate.parse(dueDateOverMaxDebtAge, formatter).toString)
      .replaceAll("<DUE_DATE_EQUALS_MAX_DEBT_AGE>", LocalDate.parse(dueDateEqualsMaxDebtAge, formatter).toString)

  }
}

object ETMPController {
  val SingleErrorIdNumber = "012X012345"
  val MultipleErrorsIdNumber = "023X023456"
}
