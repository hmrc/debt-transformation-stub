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

import org.mockito.ArgumentCaptor
import org.mockito.scalatest.MockitoSugar
import org.mongodb.scala.result.InsertOneResult
import org.scalatest.WordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.debttransformationstub.config.AppConfig
import uk.gov.hmrc.debttransformationstub.models.RequestDetail
import uk.gov.hmrc.debttransformationstub.repositories.TTPRequestsRepository

import java.time.LocalDateTime
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

class DebtManagementAPIPollingServiceSpec extends WordSpec with Matchers with MockitoSugar {

  "the DebtManagementAPIPollingService" should {
    "not rewrite a URL for field collections charge, stripping path params" in {
      insertRequestFor(
        env = "qa",
        inputUri = "/individuals/debts/field-collections/param1/param2/charge",
        expectedUri = "/individuals/debts/field-collections/param1/param2/charge",
        isCharge = true
      )
    }

    "rewrite a URL to api platform for non local requests" in {
      insertRequestFor(
        env = "qa",
        inputUri = "/individuals/subcontractor/idms/wmfid/SomewmfId",
        expectedUri = "/individuals/subcontractor/idms/wmfid/SomewmfId",
        isCharge = false
      )
    }

    "not rewrite a URL to api platform for local requests" in {
      insertRequestFor(
        env = "localhost",
        inputUri = "/individuals/subcontractor/idms/wmfid/SomewmfId",
        expectedUri = "/individuals/subcontractor/idms/wmfid/SomewmfId",
        isCharge = false
      )
    }
  }

  private def insertRequestFor(env: String, inputUri: String, expectedUri: String, isCharge: Boolean) = {
    val mockTTPRequestsRepository = mock[TTPRequestsRepository]
    val mockAppConfig = mock[AppConfig]
    val pollingService = new DebtManagementAPIPollingService(mockTTPRequestsRepository, mockAppConfig)

    val stubbedRequestDetail =
      RequestDetail(
        requestId = "89446eb1-e961-49d5-a426-3ffb1a76a6f8",
        content = Json.parse("{}"),
        uri = Some(inputUri),
        isResponse = false,
        createdOn = Some(LocalDateTime.now()),
        status = None
      )

    val captor = ArgumentCaptor.forClass(classOf[RequestDetail])
    val mockWriteResult = mock[InsertOneResult]

    when(mockAppConfig.pollingIntervals).thenReturn(1)
    when(mockAppConfig.pollingSleep).thenReturn(1)

    when(mockTTPRequestsRepository.insertRequestsDetails(captor.capture()))
      .thenReturn(Future.successful(mockWriteResult))
    when(mockTTPRequestsRepository.getResponseByRequestId(any[String]))
      .thenReturn(Future.successful(Some(stubbedRequestDetail)))
    val result = {
      if (isCharge) {
        pollingService.insertFCChargeRequestAndServeResponse(
          Json.obj(),
          "mock correlationId",
          "POST",
          "/individuals/debts/field-collections/param1/param2/charge"
        )
      } else {
        pollingService.insertRequestAndServeResponse(Json.obj(), inputUri)
      }
    }

    val requestDetail = captor.getValue.asInstanceOf[RequestDetail]

    Await.result(result, Duration.Inf)

    requestDetail.uri should contain(expectedUri)
  }
}
