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

package uk.gov.hmrc.debttransformationstub.repositories

import com.google.inject.{ ImplementedBy, Singleton }
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.{ Filters, IndexModel, IndexOptions }
import org.mongodb.scala.result.{ DeleteResult, InsertOneResult }
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

@ImplementedBy(classOf[TTPRequestsRepositoryImpl])
trait TTPRequestsRepository {
  def deleteTTPRequest(requestId: String): Future[DeleteResult]
  def findRequestDetails(): Future[List[RequestDetail]]
  def findUnprocessedRequestDetails(): Future[List[RequestDetail]]
  def getByRequestId(id: String): Future[Option[RequestDetail]]
  def getResponseByRequestId(id: String): Future[Option[RequestDetail]]
  def insertRequestsDetails(requestDetail: RequestDetail): Future[InsertOneResult]

}

@Singleton
class TTPRequestsRepositoryImpl @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[RequestDetail](
      mongo,
      "ttp-requests",
      RequestDetail.requestDetailFormat,
      indexes =
        Seq(IndexModel(ascending("referenceId"), IndexOptions().name("referenceIdUnique").unique(true).sparse(true)))
    ) with TTPRequestsRepository {

  override def findRequestDetails(): Future[List[RequestDetail]] = collection.find().toFuture().map(_.toList)

  override def findUnprocessedRequestDetails(): Future[List[RequestDetail]] =
    collection.find(Filters.equal("isResponse", false)).toFuture().map(_.toList)

  override def getByRequestId(id: String): Future[Option[RequestDetail]] =
    collection.find(Filters.equal("requestId", id)).toFuture().map(_.headOption)

  override def getResponseByRequestId(id: String): Future[Option[RequestDetail]] =
    collection
      .find(Filters.and(Filters.equal("requestId", id), Filters.equal("isResponse", true)))
      .toFuture()
      .map(_.headOption)

  override def insertRequestsDetails(requestDetail: RequestDetail): Future[InsertOneResult] =
    deleteTTPRequest(requestDetail.requestId).flatMap(_ => collection.insertOne(requestDetail).toFuture())

  override def deleteTTPRequest(requestId: String): Future[DeleteResult] =
    collection
      .deleteOne(Filters.and(Filters.equal("requestId", requestId), Filters.equal("isResponse", false)))
      .toFuture()
}
