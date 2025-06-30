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

package uk.gov.hmrc.debttransformationstub.repositories

import com.mongodb.client.model.FindOneAndUpdateOptions
import org.mongodb.scala.bson.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{ IndexModel, ReturnDocument }
import org.mongodb.scala.result.DeleteResult
import play.api.Logger
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{ Codecs, PlayMongoRepository }
import play.api.libs.json.{ Json, OFormat }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

case class EnactStage(
  correlationId: String,
  nddsRequest: Option[NDDSRequest] = None,
  pegaRequest: Option[UpdateCaseRequest] = None,
  etmpRequest: Option[PaymentLockRequest] = None,
  idmsRequest: Option[CreateMonitoringCaseRequest] = None,
  cdcsRequest: Option[CdcsCreateCaseRequest] = None,
  nddsAttempts: Option[Int] = None,
  pegaAttempts: Option[Int] = None,
  etmpAttempts: Option[Int] = None,
  idmsAttempts: Option[Int] = None,
  cdcsAttempts: Option[Int] = None
)

object EnactStage {
  implicit val format: OFormat[EnactStage] = Json.format[EnactStage]
}

@Singleton
class EnactStageRepository @Inject() (mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[EnactStage](
      mongo,
      "enact-stages",
      EnactStage.format,
      indexes = Seq.empty[IndexModel],
      replaceIndexes = true
    ) {
  private val logger: Logger = Logger(classOf[EnactStageRepository])

  def testOnlyDeleteAllDocuments(): Future[true] = {
    logger.info("===\nDropping enact stub collection\n===\n")
    collection.deleteMany(filter = empty()).toFuture().map(_ => true)
  }

  def addNDDSStage(correlationId: String, request: NDDSRequest): Future[EnactStage] = {
    logger.warn(s"Recording NDDS stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("nddsRequest", Codecs.toBson(request)), inc("nddsAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addPegaStage(correlationId: String, request: UpdateCaseRequest): Future[EnactStage] = {
    logger.warn(s"Recording PEGA stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("pegaRequest", Codecs.toBson(request)), inc("pegaAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addETMPStage(correlationId: String, request: PaymentLockRequest): Future[EnactStage] = {
    logger.warn(s"Recording ETMP stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("etmpRequest", Codecs.toBson(request)), inc("etmpAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addIDMSStage(correlationId: String, request: CreateMonitoringCaseRequest): Future[EnactStage] = {
    logger.warn(s"Recording IDMS stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("idmsRequest", Codecs.toBson(request)), inc("idmsAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }
  def addCDCSStage(correlationId: String, request: CdcsCreateCaseRequest): Future[EnactStage] = {
    logger.warn(s"Recording CDCS stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("cdcsRequest", Codecs.toBson(request)), inc("cdcsAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def findByCorrelationId(correlationId: String): Future[Option[EnactStage]] =
    collection.find(equal("correlationId", correlationId)).headOption()

  def deleteAll(): Future[DeleteResult] = collection.deleteMany(Document()).toFuture()

}
