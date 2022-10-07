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

import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.result.{ DeleteResult, InsertOneResult }
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{ Codecs, PlayMongoRepository }
import play.api.libs.json.Json

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

case class EnactStage(
  correlationId: String,
  nddsRequest: Option[NDDSRequest] = None,
  etmpRequest: Option[PaymentLockRequest] = None,
  idmsRequest: Option[CreateMonitoringCaseRequest] = None
)

object EnactStage {
  implicit val format = Json.format[EnactStage]
}

@Singleton
class EnactStageRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[EnactStage](
      mongo,
      "enact-stages",
      EnactStage.format,
      indexes = Seq(IndexModel(ascending("stageName")))
    ) {

  // TODO: upsert here
  def addNDDSStage(correlationId: String, request: NDDSRequest): Future[InsertOneResult] = {
    val item = EnactStage(correlationId = correlationId, nddsRequest = Some(request))
    collection.insertOne(item).toFuture
  }

  def addETMPStage(correlationId: String, request: PaymentLockRequest): Future[EnactStage] =
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        set("etmpRequest", Codecs.toBson(request))
      )
      .toFuture

  def addIDMSStage(correlationId: String, request: CreateMonitoringCaseRequest): Future[EnactStage] =
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        set("idmsRequest", Codecs.toBson(request))
      )
      .toFuture

  def deleteAll(): Future[DeleteResult] = collection.deleteMany(Document()).toFuture

  // Get completed enact stages by correlationId
  def findByCorrelationId(correlationId: String): Future[Option[EnactStage]] =
    collection.find(equal("correlationId", correlationId)).headOption

}
