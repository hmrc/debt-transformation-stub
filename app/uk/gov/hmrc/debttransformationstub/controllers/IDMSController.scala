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
import play.api.mvc.{ Action, ControllerComponents, Request }
import uk.gov.hmrc.debttransformationstub.models.PaymentPlanEligibilityDmRequest
import uk.gov.hmrc.debttransformationstub.models.errors.NO_RESPONSE
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.nio.charset.Charset
import javax.inject.Inject
import scala.concurrent.Future

class IDMSController @Inject() (environment: Environment, cc: ControllerComponents)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)
  private val basePath = "conf/resources/data/idms"

  def paymentPlanEligibilityDm(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
    withCustomJsonBody[PaymentPlanEligibilityDmRequest] { request =>
      val fileName = s"$basePath.eligibilityDm/${request.idValue}.json"
      environment.getExistingFile(fileName) match {
        case _ if request.idValue.equals("idmsNoResultDebtAllowance") =>
          Future.successful(GatewayTimeout(Json.parse(NO_RESPONSE.jsonErrorCause)))
        case None =>
          val message = s"file [$fileName] not found"
          logger.error(s"Status $NOT_FOUND, message: $message")
          Future successful NotFound(message)
        case Some(file) =>
          val result = FileUtils.readFileToString(file, Charset.defaultCharset())
          Future successful Ok(Json.parse(result))

      }
    }
  }
}
