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

import javax.inject.Inject
import scala.io.Source

class ETMPController @Inject()(environment: Environment,
                               cc: ControllerComponents)
  extends BackendController(cc) {
  private val getFinancialsCasePath = "conf/resources/data/etmp/getFinancials/"
  private val getPAYEMasterCasePath = "conf/resources/data/paye/getMaster/"

  def getFinancials(idType: String, idNumber: String,
                    regimeType: String): Action[AnyContent] = Action { request =>
    environment.getExistingFile(s"$getFinancialsCasePath$idNumber.json") match {
      case Some(file) =>
        Ok(Source.fromFile(file).mkString)
      case _ =>
        NotFound("file not found") // TODO - use error message from Get financials spec
    }

  }

  def getPAYEMaster(idType: String, latest:String): Action[AnyContent] = Action { request =>
    environment.getExistingFile(s"$getPAYEMasterCasePath$idType.json") match {
      case Some(file) =>
        Ok(Source.fromFile(file).mkString)
      case _ =>
        NotFound("file not found " + getPAYEMasterCasePath + "$idType.json") // TODO - use error message from Get PAYE master
    }

  }

}