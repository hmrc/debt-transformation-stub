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

import akka.http.scaladsl.model.StatusCodes
import com.google.inject.ImplementedBy
import reactivemongo.api.commands.WriteResult
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.models.errors.{ TTPRequestsCreationError, TTPRequestsDeletionError, TTPRequestsError }
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[DefaultTTPRequestsService])
trait TTPRequestsService {
  def getTTPRequests(): Future[List[RequestDetail]]

  def getUnprocesedTTPRequests(): Future[List[RequestDetail]]

  def getTTPRequest(requestId: String): Future[Option[RequestDetail]]

  def addRequestDetails(requestDetailsRequest: RequestDetail)(implicit
    hc: HeaderCarrier
  ): Future[Either[TTPRequestsError, String]]

  def deleteTTPRequest(requestId: String)(implicit hc: HeaderCarrier): Future[Either[TTPRequestsDeletionError, String]]
}

@Singleton
class DefaultTTPRequestsService @Inject() (ttpRequestsRepository: TTPRequestsRepository) extends TTPRequestsService {

  override def addRequestDetails(
    requestDetailsRequest: RequestDetail
  )(implicit hc: HeaderCarrier): Future[Either[TTPRequestsError, String]] = {

    val currentDate = LocalDateTime.now()
    val requestDetails = requestDetailsRequest.copy(createdOn = Some(currentDate))

    val writeResultF = ttpRequestsRepository.insertRequestsDetails(requestDetails)
    writeResultF.flatMap { wr: WriteResult =>
      wr.writeErrors.headOption match {
        case Some(err) =>
          Future(
            Left(
              TTPRequestsCreationError(
                StatusCodes.InternalServerError.intValue,
                Some(s"Failed to insert the document: ${err.errmsg}")
              )
            )
          )
        case None => Future(Right(s"Successfully inserted the ttp request"))
      }
    }
  }

  override def getTTPRequests(): Future[List[RequestDetail]] = ttpRequestsRepository.findRequestDetails()

  override def getTTPRequest(requestId: String): Future[Option[RequestDetail]] =
    ttpRequestsRepository.getByRequestId(requestId)

  override def getUnprocesedTTPRequests(): Future[List[RequestDetail]] =
    ttpRequestsRepository.findUnprocessedRequestDetails()

  override def deleteTTPRequest(
    requestId: String
  )(implicit hc: HeaderCarrier): Future[Either[TTPRequestsDeletionError, String]] =
    ttpRequestsRepository.deleteTTPRequest(requestId) map { x: WriteResult =>
      if (x.ok)
        Right("Successfully deleted the TTP request with request Id: " + requestId)
      else
        Left(TTPRequestsDeletionError(x.code.fold(404)((e: Int) => e), None))
    }
}
