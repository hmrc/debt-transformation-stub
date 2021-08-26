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

package uk.gov.hmrc.debttransformationstub.repositories

import com.google.inject.{ImplementedBy, Singleton}
import javax.inject.Inject
import play.api.libs.json.{JsBoolean, JsString, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

@ImplementedBy(classOf[TTPRequestErrorsRepositoryImpl])
trait TTPRequestErrorsRepository {

  def findTTPRequestErrors(): Future[List[RequestDetail]]
  def logTTPRequestError(requestDetail: RequestDetail)(implicit ec: ExecutionContext): Future[WriteResult]

}

@Singleton
class TTPRequestErrorsRepositoryImpl @Inject()(implicit mongo: ReactiveMongoComponent, ec: ExecutionContext)
  extends ReactiveRepository[RequestDetail, BSONObjectID]("ttp-request-errors", mongo.mongoConnector.db, RequestDetail.requestDetailFormat, ReactiveMongoFormats.objectIdFormats)
    with TTPRequestErrorsRepository {

  private lazy val IdField = "referenceId"

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(
      Seq(
        collection.indexesManager(ec)
          .ensure(Index(Seq(IdField -> IndexType.Ascending), name = Some(IdField + "Unique"), unique = true, sparse = true))
      )
    )(implicitly, ec)
  }

  override def logTTPRequestError(requestDetail: RequestDetail)(implicit ec: ExecutionContext): Future[WriteResult] = insert(requestDetail)

  override def findTTPRequestErrors(): Future[List[RequestDetail]] = super.findAll()

}
