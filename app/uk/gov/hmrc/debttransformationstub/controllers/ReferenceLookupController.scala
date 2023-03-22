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
import uk.gov.hmrc.debttransformationstub.utils.{ ListHelper, ReferenceDataLookupRequest, RequestAwareLogger }
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import javax.inject.{ Inject, Singleton }
import scala.concurrent.Future
import scala.io.Source

@Singleton()
class ReferenceLookupController @Inject() (
  environment: Environment,
  cc: ControllerComponents
) extends BackendController(cc) with CustomBaseController {

  private val basePath = "conf/resources/data"
  private val refPath = "/data/"

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val listHelper: ListHelper = new ListHelper()

  def getReferenceData(descType: String, mainTrans: String, subTrans: String) = Action { implicit request =>
    val testOnlyResponseCode: Option[String] = request.headers.get("testOnlyResponseCode")
    if (testOnlyResponseCode.isDefined) {
      Results.Status(testOnlyResponseCode.map(_.toInt).getOrElse(500))
    } else {
      environment.getExistingFile(basePath + refPath + descType + "-" + mainTrans + "-" + subTrans + ".json") match {
        case Some(file) => Ok(Source.fromFile(file).mkString)
        case _ =>
          logger.error(s"Status $NOT_FOUND, message: file not found")
          NotFound("file not found")
      }
    }
  }

  def getReferenceDataLookup() = Action(parse.json).async { implicit request =>
    withCustomJsonBody[ReferenceDataLookupRequest] { req =>
      val maybeBearerToken: Option[String] = request.headers.get("Authorization")
      if (maybeBearerToken.isDefined) {
        val files: Seq[File] = req.items.flatMap { item =>
          environment.getExistingFile(
            basePath + refPath + req.`type` + "-" + item.mainTrans + "-" + item.subTrans + ".json"
          )
        }

        if (files.isEmpty) {
          logger.error(s"Status $NOT_FOUND, message: file not found")
          Future successful NotFound("file not found")
        } else {
          val result = files
            .map(file => Source.fromFile(file).mkString)
            .mkString("""{ "ItemList": [ """.stripMargin, ",", """]}""".stripMargin)
          Future successful Ok(result)
        }
      } else Future successful Unauthorized("invalid token provided")
    }
  }

  def getList() = Action {
    Ok(listHelper.getList(basePath + refPath))
  }
}
