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
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.utils.{ ListHelper, RequestAwareLogger }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{ Inject, Singleton }
import scala.io.Source

@Singleton()
class CaseManagementSystemController @Inject() (
  environment: Environment,
  cc: ControllerComponents
) extends BackendController(cc) with BaseController {

  private val basePath = "conf/resources/data"
  private val casePath = "/debt/"

  private val listHelper: ListHelper = new ListHelper()
  private lazy val logger = new RequestAwareLogger(this.getClass)

  def getCaseDetails(debtID: String, duties: Option[String]) = Action { implicit request =>
    val testOnlyResponseCode: Option[String] = request.headers.get("testOnlyResponseCode")
    if (testOnlyResponseCode.isDefined) {
      Results.Status(testOnlyResponseCode.map(_.toInt).getOrElse(500))
    } else {
      environment.getExistingFile(basePath + casePath + debtID + ".json") match {
        case Some(file) => Ok(Source.fromFile(file).mkString)
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          NotFound("file not found")
      }
    }
  }

  def getDebtCaseManagement(customerUniqueRef: String, debtId: String, dutyIds: String) = Action { implicit request =>
    val maybeBearerToken: Option[String] = request.headers.get("Authorization")
    if (maybeBearerToken.isDefined) {
      environment.getExistingFile(basePath + casePath + debtId + ".json") match {
        case Some(file) =>
          Ok(Source.fromFile(file).mkString)
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          NotFound("file not found")
      }
    } else Unauthorized("invalid token provided")
  }

  def getList() = Action {
    Ok(listHelper.getList(basePath + casePath))
  }
}
