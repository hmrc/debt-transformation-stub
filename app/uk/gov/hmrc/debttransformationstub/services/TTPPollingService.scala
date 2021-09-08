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

package uk.gov.hmrc.debttransformationstub.services

import akka.http.scaladsl.model.StatusCodes
import com.google.inject.ImplementedBy
import play.api.libs.json.Json

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.debttransformationstub.actions.requests.RequestDetailsRequest
import uk.gov.hmrc.debttransformationstub.actions.responses.RequestDetailsResponse
import uk.gov.hmrc.debttransformationstub.models.{GenerateQuoteRequest, RequestDetail}
import uk.gov.hmrc.debttransformationstub.models.errors.{TTPRequestsCreationError, TTPRequestsDeletionError, TTPRequestsError}
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository
import uk.gov.hmrc.http.HeaderCarrier

@ImplementedBy(classOf[DefaultTTPRequestsService])
trait TTPRequestsService {

  def generateQuote(request: GenerateQuoteRequest, uri: Option[String]): Future[Option[RequestDetail]]
}

@Singleton
class DefaultTTPRequestsService @Inject()(ttpRequestsRepository: TTPRequestsRepository)
  extends TTPRequestsService {


  override def generateQuote(request: GenerateQuoteRequest, uri: Option[String]): Future[Option[RequestDetail]] = {
    val requestId = "someGeneratedRequestId"
    ttpRequestsRepository.insertRequestsDetails(RequestDetail(requestId, Json.toJson(request).toString(), uri, false, Some(LocalDateTime.now()))).flatMap {
      result =>
        pollForResponse(requestId)
    }
  }

  private def pollForResponse(requestId: String): Future[Option[RequestDetail]] = {
    ttpRequestsRepository.getResponseByRequestId(requestId).flatMap {
      case None => pollForResponse(requestId)
      case Some(response) => Future.successful(Some(response))
    }
  }

}
