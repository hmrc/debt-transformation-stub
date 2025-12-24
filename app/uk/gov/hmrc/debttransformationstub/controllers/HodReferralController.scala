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

import play.api.libs.json.{ JsValue, Json }
import play.api.mvc.{ Action, ControllerComponents, Headers, Request }
import uk.gov.hmrc.debttransformationstub.models.HodReferralRequest
import uk.gov.hmrc.debttransformationstub.repositories.EnactStageRepository
import uk.gov.hmrc.debttransformationstub.services.HodReferralDecryptionService
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.xml.XML


class HodReferralController @Inject() (
  cc: ControllerComponents,
  enactStageRepository: EnactStageRepository,
  decryptionService: HodReferralDecryptionService
)(implicit ec: ExecutionContext)
    extends BackendController(cc) with CustomBaseController {

  private lazy val logger = new RequestAwareLogger(this.getClass)

  def processEncryptedReferrals(): Action[JsValue] = Action.async(parse.json) { implicit rawRequest: Request[JsValue] =>
//    val correlationId = getCorrelationIdHeader(rawRequest.headers)
    logger.info(s"processEncryptedReferrals request isssss ${rawRequest.body}")

    withCustomJsonBody[HodReferralRequest] { request =>
//      logger.info(s"[DEBUG] HodReferral called with correlationId=$correlationId")
      logger.info(s"[DEBUG] HodReferral requestt: ${Json.toJson(request)}")

      // Attempt to decrypt the message content
      val decryptedXml = decryptionService.decrypt(request)
      decryptedXml match {
        case Some(xml) => logger.info(s"[DEBUG] HodReferral decryption successful, XML length: ${xml.length}")
        case None      => logger.warn(s"[DEBUG] HodReferral decryption failed - could not decrypt with test keys")
      }

      val idValue: String = extractNinoFromHodXml(decryptedXml.toString)
      logger.info(s"[DEBUG] Extracted NINO from decrypted XML: $idValue")

      // Record the request in the repository (including decrypted content if available)
      enactStageRepository
        .addHodReferralStage(idValue, request, 200, decryptedXml)
        .map { _ =>
          logger.info(s"[DEBUG] HodReferral stage recorded in EnactStage repository with status 200")

          // Return a success response with current timestamp in ISO 8601 format
          Ok(
            Json.obj(
              "status"             -> "Success",
              "processingDateTime" -> Instant.now().toString
            )
          )
        }
    }
  }
  val NinoRegex =
    """(?:name="NINO"\s+type="String"\s+value="|label="NINO"\s+value=")([A-Z]{2}\d{6}[A-Z])""".r

  def extractNinoFromHodXml(xmlStr: String): String =
    NinoRegex
      .findFirstMatchIn(xmlStr)
      .map(_.group(1))
      .getOrElse("")


  def getCorrelationIdHeader(headers: Headers): String =
    headers.get("correlationId").getOrElse(throw new Exception("Missing required correlationId header"))

}
