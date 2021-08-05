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
import java.time.{LocalDate, LocalDateTime}
import javax.inject.{Inject, Singleton}
import reactivemongo.api.commands.WriteResult
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.debttransformationstub.actions.requests.RequestDetailsRequest
import uk.gov.hmrc.debttransformationstub.actions.responses.RequestDetailsResponse
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.models.errors.{TTPRequestsCreationError, TTPRequestsError}
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestErrorsRepository
import uk.gov.hmrc.http.HeaderCarrier

@ImplementedBy(classOf[DefaultTTPRequestErrorsService])
trait TTPRequestErrorsService {

  def getTTPRequestErrors(): Future[List[RequestDetailsResponse]]
  def logTTPRequestError(requestDetailsRequest: RequestDetailsRequest)(implicit hc: HeaderCarrier): Future[Either[TTPRequestsError, String]]

}

@Singleton
class DefaultTTPRequestErrorsService @Inject()(ttpRequestErrorsRepository: TTPRequestErrorsRepository )
        extends TTPRequestErrorsService {

  override def getTTPRequestErrors(): Future[List[RequestDetailsResponse]] = {
    val ttpDetailsResponse = ttpRequestErrorsRepository.findTTPRequestErrors() map { requestDetails =>
      requestDetails.map { requestDetails => RequestDetailsResponse(requestDetails) }
    }
    ttpDetailsResponse
  }

  override def logTTPRequestError(requestDetailsRequest: RequestDetailsRequest)(implicit hc: HeaderCarrier): Future[Either[TTPRequestsError, String]] = {
    val currentDate = LocalDateTime.now()
    val requestDetails = RequestDetail(requestDetailsRequest.requestId, requestDetailsRequest.content, requestDetailsRequest.uri, requestDetailsRequest.isResponse, requestDetailsRequest.processed, Some(currentDate))
    println(s"REQUEST DETAILS --> $requestDetails")

    val writeResultF = ttpRequestErrorsRepository.logTTPRequestError(requestDetails)
    writeResultF.flatMap { wr: WriteResult =>
      wr.writeErrors.headOption match {
        case Some(err) => Future(Left(TTPRequestsCreationError(StatusCodes.InternalServerError.intValue, Some(s"Failed to insert the document: ${err.errmsg}"))))
        case None      => Future(Right(s"Successfully inserted the ttp request"))
      }
    }
  }
}
