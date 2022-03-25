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

package uk.gov.hmrc.debttransformationstub.services

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.libs.json.JsValue
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository
import play.api.libs.json.Json

@Singleton
class DebtManagementAPIPollingService @Inject() (
  ttpRequestsRepository: TTPRequestsRepository,
  appConfig: AppConfig
) {

  def insertFCChargeRequestAndServeResponse(
    request: JsValue,
    correlationId: String,
    method: String
  ): Future[Option[RequestDetail]] =
    process(
      request,
      uri = None,
      uriOverride = Some("/individuals/field-collections/charges"),
      headers = Map("CorrelationId" -> correlationId),
      method = Some(method.toUpperCase)
    )

  def insertTemplateRequestAndServeResponse(
    request: JsValue,
    correlationId: String
  ): Future[Option[RequestDetail]] =
    process(
      request,
      uri = None,
      uriOverride = Some("/individuals/field-collections/template"),
      headers = Map("CorrelationId" -> correlationId)
    )

  def insertTaxpayerRequestAndServeResponse(): Future[Option[RequestDetail]] =
    process(
      Json.obj(),
      uri = None,
      uriOverride = Some("/individuals/subcontractor/idms/taxpayer/789"),
      method = Some("GET")
    )

  def insertRequestAndServeResponse(
    request: JsValue,
    uri: String,
  ): Future[Option[RequestDetail]] = process(
    request,
    uri = Some(uri),
    uriOverride = None
  )

  private def process(
    request: JsValue,
    uri: Option[String],
    uriOverride: Option[String],
    method: Option[String] = Some("POST"),
    headers: Map[String, String] = Map.empty
  ): Future[Option[RequestDetail]] = {
    val requestId = UUID.randomUUID().toString
    val requestDetails = RequestDetail(
      requestId = requestId,
      content = Json.parse(request.toString),
      uri = uriOverride.orElse(uri),
      method = method,
      isResponse = false,
      createdOn = Some(LocalDateTime.now()),
      headers = if (headers.isEmpty) None else Some(headers),
    )
    ttpRequestsRepository.insertRequestsDetails(requestDetails).flatMap { _ =>
      pollForResponse(requestId)
    }
  }

  private def pollForResponse(
    requestId: String,
    tries: Int = appConfig.pollingIntervals,
    timeoutMs: Int = appConfig.pollingSleep
  ): Future[Option[RequestDetail]] =
    ttpRequestsRepository.getResponseByRequestId(requestId).flatMap {
      case None =>
        if (tries > 0) {
          Thread.sleep(timeoutMs)
          pollForResponse(requestId, tries - 1)
        } else Future.successful(None)
      case Some(response) => Future.successful(Some(response))
    }
}
