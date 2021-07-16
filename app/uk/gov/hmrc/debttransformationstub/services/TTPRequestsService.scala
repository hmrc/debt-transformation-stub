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
import javax.inject.{Inject, Singleton}
import reactivemongo.api.commands.WriteResult
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.debttransformationstub.actions.requests.RequestDetailsRequest
import uk.gov.hmrc.debttransformationstub.actions.responses.RequestDetailsResponse
//import uk.gov.hmrc.debttransformationstub.models
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.models.errors.{TTPRequestsCreationError, TTPRequestsError}
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository
import uk.gov.hmrc.http.HeaderCarrier

@ImplementedBy(classOf[DefaultTTPRequestsService])
trait TTPRequestsService {
  def getTTPRequests(): Future[List[RequestDetailsResponse]]
  def getUnprocesedTTPRequests(): Future[List[RequestDetailsResponse]]
  def getTTPRequest(requestId: String): Future[Option[RequestDetailsResponse]]
  def addRequestDetails(requestDetailsRequest: RequestDetailsRequest)(implicit hc: HeaderCarrier): Future[Either[TTPRequestsError, String]]
//  def setTTPRequestToProcessed(requestId: String): Future[Either[TTPRequestsError, String]]
}

@Singleton
class DefaultTTPRequestsService @Inject()(ttpRequestsRepository: TTPRequestsRepository )
        extends TTPRequestsService {

  override def addRequestDetails(requestDetailsRequest: RequestDetailsRequest)(implicit hc: HeaderCarrier): Future[Either[TTPRequestsError, String]] = {
    val requestDetails = RequestDetail(requestDetailsRequest.requestId, requestDetailsRequest.content, requestDetailsRequest.uri, requestDetailsRequest.isResponse)
    println(s"REQUEST DETAILS --> $requestDetails")

      val writeResultF = ttpRequestsRepository.insertRequestsDetails(requestDetails)
      writeResultF.flatMap { wr: WriteResult =>
        wr.writeErrors.headOption match {
          case Some(err) => Future(Left(TTPRequestsCreationError(StatusCodes.InternalServerError.intValue, Some(s"Failed to insert the document: ${err.errmsg}"))))
          case None      => Future(Right(s"Successfully inserted the ttp request"))
        }
      }
  }

  override def getTTPRequests(): Future[List[RequestDetailsResponse]] = {
    val ttpDetailsResponse = ttpRequestsRepository.findRequestDetails() map { requestDetails =>
      requestDetails.map { requestDetails => RequestDetailsResponse(requestDetails) }
    }
    ttpDetailsResponse
  }

  override def getTTPRequest(requestId: String): Future[Option[RequestDetailsResponse]] = {
    val ttpDetailsResponse = ttpRequestsRepository.getByRequestId(requestId) map { requestDetails =>
      requestDetails.map { requestDetails => RequestDetailsResponse(requestDetails) }
    }
    ttpDetailsResponse
  }

  override def getUnprocesedTTPRequests(): Future[List[RequestDetailsResponse]] = {
    val ttpDetailsResponse = ttpRequestsRepository.findUnprocessedRequestDetails() map { requestDetails =>
      requestDetails.map { requestDetails => RequestDetailsResponse(requestDetails) }
    }
    ttpDetailsResponse
  }



//  override def setTTPRequestToProcessed(requestId: String, processed:Boolean =true)

  private def setTTPRequestToProcessed(requestId: String, processed: Boolean) = {
//todo updated processed flag
  }
}
