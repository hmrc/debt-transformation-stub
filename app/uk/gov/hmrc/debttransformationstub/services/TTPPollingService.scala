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

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.JavaFlowSupport.Source
import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import uk.gov.hmrc.debttransformationstub.models.{GenerateQuoteRequest, RequestDetail}
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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

  private def pollForResponse(requestId: String, tries: Int = 50, timeoutMs: Int = 200): Future[Option[RequestDetail]] = {

    ttpRequestsRepository.getResponseByRequestId(requestId).flatMap {
      case None =>
        if(tries > 0) {
          Thread.sleep(timeoutMs)
          pollForResponse(requestId, tries - 1)
        } else Future.successful(None)
      case Some(response) => Future.successful(Some(response))
    }
  }

}
