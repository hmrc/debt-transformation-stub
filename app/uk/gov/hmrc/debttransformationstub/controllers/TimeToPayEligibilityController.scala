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

import org.apache.commons.io.FileUtils
import play.api.Environment
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import uk.gov.hmrc.debttransformationstub.models.chargeinfo.ChargeInfoRequest
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.io.File
import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.Try

class TimeToPayEligibilityController @Inject() (
  cc: ControllerComponents,
  environment: Environment
) extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data"

  def getCharges(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withCustomJsonBody[ChargeInfoRequest] { req =>
      logger.info(s"Request body for get-charges: ${request.body}")
      val path = "/ttpe.proxy/"

      val maybeUtr: Option[String] =
        if (req.identifications.length == 1)
          req.identifications.headOption.find(_.idType == "UTR").map(_.idValue)
        else
          req.identifications.find(_.idType == "UTR").map(_.idValue)
      logger.info(s"Maybe UTR provided: $maybeUtr")

      val maybeResultByUtr: Option[Result] = maybeUtr flatMap { utr =>
        constructResponse(path, s"$utr.json")
      }

      Future.successful(
        maybeResultByUtr.getOrElse(Results.InternalServerError(s"Could not find file $maybeUtr in path $path"))
      )
    }
  }

  private def constructResponse(path: String, fileName: String)(implicit hc: HeaderCarrier): Option[Result] =
    findFile(path, fileName) map { file =>
      val fileString = FileUtils.readFileToString(file, Charset.defaultCharset())

      Try(Json.parse(fileString)).toOption match {
        case Some(fileJson) =>
          logger.info(s"""constructResponse() → Successfully parsed fileName: $fileName
                         |Returning body:
                         |$fileJson
                         |""".stripMargin)
          Results.Ok(fileJson)
        case None =>
          logger.info(s"constructResponse() → Failed to parse fileName: $fileName")
          Results.InternalServerError(s"stub failed to parse file $path$fileName")
      }
    }

  private def findFile(path: String, fileName: String): Option[File] =
    environment.getExistingFile(s"$basePath$path$fileName")
}
