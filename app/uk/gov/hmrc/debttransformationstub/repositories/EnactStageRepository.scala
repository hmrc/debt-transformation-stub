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
import org.mongodb.scala.model.{IndexModel, ReturnDocument}
import org.mongodb.scala.result.DeleteResult
import play.api.Logger
import play.api.libs.json.{JsPath, Json, OFormat, Reads, Writes, __, OWrites}
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}
import play.api.libs.functional.syntax._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

case class EnactStage(
  correlationId: Option[String] = None,
  etmp: Option[EnactEtmp],
  nddsRequest: Option[NDDSRequest] = None,
  pegaRequest: Option[UpdateCaseRequest] = None,
  idmsRequest: Option[CreateIDMSMonitoringCaseRequest] = None,
  idmsRequestSA: Option[CreateIDMSMonitoringCaseRequestSA] = None,
  cdcsRequest: Option[CdcsCreateCaseRequest] = None,
  cesaRequest: Option[CesaRequest] = None,
  customerCheckRequest: Option[CustomerCheckRequest] = None,
  hodReferralRequest: Option[HodReferralRequest] = None,
  hodReferralDecryptedXml: Option[String] = None,
  nddsAttempts: Option[Int] = None,
  pegaAttempts: Option[Int] = None,
  idmsAttempts: Option[Int] = None,
  cdcsAttempts: Option[Int] = None,
  cesaAttempts: Option[Int] = None,
  customerCheckAttempts: Option[Int] = None,
  customerCheckStatus: Option[Int] = None,
  hodReferralAttempts: Option[Int] = None,
  hodReferralStatus: Option[Int] = None,
  combinedStageAttempts: Option[Int] = None
)

object EnactStage {
  implicit val format: OFormat[EnactStage] = OFormat(reads, writes)

  private val reads: Reads[EnactStage] = (
    (__ \"correlationId").readNullable[String] and
    __.read[EnactEtmp](EnactEtmp.reads) and
    (__ \ "nddsRequest").readNullable[NDDSRequest] and
    (__ \ "pegaRequest").readNullable[UpdateCaseRequest] and
    (__ \ "idmsRequest").readNullable[CreateIDMSMonitoringCaseRequest] and
    (__ \ "idmsRequestSA").readNullable[CreateIDMSMonitoringCaseRequestSA] and
    (__ \ "cdcsRequest").readNullable[CdcsCreateCaseRequest] and
    (__ \ "cesaRequest").readNullable[CesaRequest] and
    (__ \ "customerCheckRequest").readNullable[CustomerCheckRequest] and
    (__ \ "hodReferralRequest").readNullable[HodReferralRequest] and
    (__ \ "hodReferralDecryptedXml").readNullable[String] and
    (__ \ "nddsAttempts").readNullable[Int] and
    (__ \ "pegaAttempts").readNullable[Int] and
    (__ \ "idmsAttempts").readNullable[Int] and
    (__ \ "cdcsAttempts").readNullable[Int] and
    (__ \ "cesaAttempts").readNullable[Int] and
    (__ \ "customerCheckAttempts").readNullable[Int] and
    (__ \ "customerCheckStatus").readNullable[Int] and
    (__ \ "hodReferralAttempts").readNullable[Int] and
    (__ \ "hodReferralStatus").readNullable[Int] and
    (__ \ "combinedStageAttempts").readNullable[Int]
  )(EnactStage.apply _)

  private val writes: OWrites[EnactStage] = (
    (__ \"correlationId").writeNullable[String] and
      __.write[EnactEtmp](EnactEtmp.writes) and
      (__ \ "nddsRequest").writeNullable[NDDSRequest] and
      (__ \ "pegaRequest").writeNullable[UpdateCaseRequest] and
      (__ \ "idmsRequest").writeNullable[CreateIDMSMonitoringCaseRequest] and
      (__ \ "idmsRequestSA").writeNullable[CreateIDMSMonitoringCaseRequestSA] and
      (__ \ "cdcsRequest").writeNullable[CdcsCreateCaseRequest] and
      (__ \ "cesaRequest").writeNullable[CesaRequest] and
      (__ \ "customerCheckRequest").writeNullable[CustomerCheckRequest] and
      (__ \ "hodReferralRequest").writeNullable[HodReferralRequest] and
      (__ \ "hodReferralDecryptedXml").writeNullable[String] and
      (__ \ "nddsAttempts").writeNullable[Int] and
      (__ \ "pegaAttempts").writeNullable[Int] and
      (__ \ "idmsAttempts").writeNullable[Int] and
      (__ \ "cdcsAttempts").writeNullable[Int] and
      (__ \ "cesaAttempts").writeNullable[Int] and
      (__ \ "customerCheckAttempts").writeNullable[Int] and
      (__ \ "customerCheckStatus").writeNullable[Int] and
      (__ \ "hodReferralAttempts").writeNullable[Int] and
      (__ \ "hodReferralStatus").writeNullable[Int] and
      (__ \ "combinedStageAttempts").writeNullable[Int]
    )(es => (
      es.correlationId,
      es.etmp,
      es.nddsRequest,
      es.pegaRequest,
      es.idmsRequest,
      es.idmsRequestSA,
      es.cdcsRequest,
      es.cesaRequest,
      es.customerCheckRequest,
      es.hodReferralRequest,
      es.hodReferralDecryptedXml,
      es.nddsAttempts,
      es.pegaAttempts,
      es.idmsAttempts,
      es.cdcsAttempts,
      es.cesaAttempts,
      es.customerCheckAttempts,
      es.customerCheckStatus,
      es.hodReferralAttempts,
      es.hodReferralStatus,
      es.combinedStageAttempts,
  ))
}

case class EnactEtmp(
  etmpRequest: Option[PaymentLockRequest] = None,
  etmpRemoveRequest: Option[ETMPRemoveRequest] = None,
  etmpAttempts: Option[Int] = None,
)

object EnactEtmp {
  val reads: Reads[EnactEtmp] = (
    (__ \ "etmpRequest").readNullable[PaymentLockRequest] and
      (__ \ "etmpRemoveRequest").readNullable[ETMPRemoveRequest] and
      (__ \ "etmpAttempts").readNullable[Int]
    )(EnactEtmp.apply _)

  val writes: Writes[EnactEtmp] = (
    (__ \ "etmpRequest").writeNullable[PaymentLockRequest] and
      (__ \ "etmpRemoveRequest").writeNullable[ETMPRemoveRequest] and
      (__ \ "etmpAttempts").writeNullable[Int]
    )(ee => (ee.etmpRequest, ee.etmpRemoveRequest, ee.etmpAttempts))
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
        combine(set("etmpRequest", Codecs.toBson(request)), inc("etmpAttempts", 1), inc("combinedStageAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addETMPRemoveChargeStage(correlationId: String, request: ETMPRemoveRequest): Future[EnactStage] = {
    logger.warn(s"Recording ETMP Remove Charge correlationId $correlationId")
    logger.info(s"ETMP Remove Charge stage request being recorded: ${Json.prettyPrint(Json.toJson(request))}")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("etmpRemoveRequest", Codecs.toBson(request)), inc("etmpAttempts", 1), inc("combinedStageAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addIDMSStage(idValue: String, request: CreateIDMSMonitoringCaseRequest): Future[EnactStage] = {
    logger.info(s"Recording IDMS stage request $idValue")
    logger.info(s"IDMS Request being recorded: ${Json.prettyPrint(Json.toJson(request))}")
    collection
      .findOneAndUpdate(
        equal("idValue", idValue),
        combine(set("idmsRequest", Codecs.toBson(request)), inc("idmsAttempts", 1), inc("combinedStageAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addIDMSStageSA(idValue: String, request: CreateIDMSMonitoringCaseRequestSA): Future[EnactStage] = {
    logger.info(s"Recording IDMS SA stage request $idValue")
    logger.info(s"IDMS SA Request being recorded: ${Json.prettyPrint(Json.toJson(request))}")
    collection
      .findOneAndUpdate(
        equal("idValue", idValue),
        combine(set("idmsRequestSA", Codecs.toBson(request)), inc("idmsAttempts", 1), inc("combinedStageAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addCDCSStage(idValue: String, request: CdcsCreateCaseRequest): Future[EnactStage] = {
    logger.info(s"Recording CDCS stage request $idValue")
    logger.info(s"CDCS Request being recorded: ${Json.prettyPrint(Json.toJson(request))}")
    collection
      .findOneAndUpdate(
        equal("idValue", idValue),
        combine(set("cdcsRequest", Codecs.toBson(request)), inc("cdcsAttempts", 1), inc("combinedStageAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addCESAStage(correlationId: String, request: CesaRequest): Future[EnactStage] = {
    logger.warn(s"Recording CESA stage request $correlationId")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(set("cesaRequest", Codecs.toBson(request)), inc("cesaAttempts", 1)),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addCustomerCheckStage(
    correlationId: String,
    request: CustomerCheckRequest,
    statusCode: Int
  ): Future[EnactStage] = {
    logger.warn(s"Recording CustomerCheck stage request $correlationId with status code $statusCode")
    collection
      .findOneAndUpdate(
        equal("correlationId", correlationId),
        combine(
          set("customerCheckRequest", Codecs.toBson(request)),
          set("customerCheckStatus", statusCode),
          inc("customerCheckAttempts", 1),
          inc("combinedStageAttempts", 1)
        ),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def addHodReferralStage(
    idValue: String,
    correlationId: String,
    request: HodReferralRequest,
    statusCode: Int,
    decryptedXml: Option[String] = None
  ): Future[EnactStage] = {
    logger.warn(s"Recording HodReferral stage request $idValue with status code $statusCode")
    val updates = Seq(
      set("correlationId", correlationId),
      set("hodReferralRequest", Codecs.toBson(request)),
      set("hodReferralStatus", statusCode),
      inc("hodReferralAttempts", 1)
    ) ++ decryptedXml.map(xml => set("hodReferralDecryptedXml", xml)).toSeq

    collection
      .findOneAndUpdate(
        equal("idValue", idValue),
        combine(updates: _*),
        new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
  }

  def findByCorrelationId(correlationId: String): Future[Option[EnactStage]] =
    collection.find(equal("correlationId", correlationId)).headOption()

  def findByIdValue(idValue: String): Future[Option[EnactStage]] =
    collection.find(equal("idValue", idValue)).headOption()

  def deleteAll(): Future[DeleteResult] = collection.deleteMany(Document()).toFuture()

}
