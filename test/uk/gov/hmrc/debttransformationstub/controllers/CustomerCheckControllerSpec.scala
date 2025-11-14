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

package uk.gov.hmrc.debttransformationstub.controllers

import org.mockito.ArgumentMatchers.{ any, eq => meq }
import org.mockito.Mockito.{ reset, verify, when }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Environment
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.Helpers._
import play.api.test.{ DefaultAwaitTimeout, FakeRequest, Helpers }
import uk.gov.hmrc.debttransformationstub.models._
import uk.gov.hmrc.debttransformationstub.repositories.{ EnactStage, EnactStageRepository }
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }

class CustomerCheckControllerSpec
    extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with DefaultAwaitTimeout with MockitoSugar
    with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]
  implicit val mat = app.materializer
  val cc: ControllerComponents = Helpers.stubControllerComponents()
  val env: Environment = app.injector.instanceOf[Environment]
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  val mockEnactStageRepository: EnactStageRepository = mock[EnactStageRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEnactStageRepository)
  }

  private val controller = new CustomerCheckController(env, cc, mockEnactStageRepository)

  private val validNinoRequest = CustomerCheckRequest(
    userId = Some(CustomerCheckUserId("user123")),
    userLocation = Some(CustomerCheckUserLocation("location123")),
    customers = List(
      CustomerCheckCustomer(
        universalCustomerId = Some(CustomerCheckUniversalCustomerId("uci123")),
        nino = Some(CustomerCheckNino("AA111111A")),
        empRef = None
      )
    )
  )

  private val validEmpRefRequest = CustomerCheckRequest(
    userId = Some(CustomerCheckUserId("user123")),
    userLocation = Some(CustomerCheckUserLocation("location123")),
    customers = List(
      CustomerCheckCustomer(
        universalCustomerId = Some(CustomerCheckUniversalCustomerId("uci123")),
        nino = None,
        empRef = Some(CustomerCheckEmpRef("864FZ00049"))
      )
    )
  )

  private val noIdentifierRequest = CustomerCheckRequest(
    userId = Some(CustomerCheckUserId("user123")),
    userLocation = Some(CustomerCheckUserLocation("location123")),
    customers = List(
      CustomerCheckCustomer(
        universalCustomerId = Some(CustomerCheckUniversalCustomerId("uci123")),
        nino = None,
        empRef = None
      )
    )
  )

  private val validCorrelationId = "test-correlation-id"

  "POST /scrbroker/accessmgmt" should {
    "return 200 with encrypted data when NINO file exists" in {
      when(mockEnactStageRepository.addCustomerCheckStage(meq(validCorrelationId), any[CustomerCheckRequest]()))
        .thenReturn(Future.successful(EnactStage(validCorrelationId)))

      val fakeRequest = FakeRequest("POST", "/scrbroker/accessmgmt")
        .withHeaders("correlationId" -> validCorrelationId)
        .withBody(Json.toJson(validNinoRequest))

      val result = controller.customerCheck()(fakeRequest)

      status(result) shouldBe Status.OK
      verify(mockEnactStageRepository).addCustomerCheckStage(meq(validCorrelationId), meq(validNinoRequest))
      val jsonResult = contentAsJson(result)
      (jsonResult \ "ephemeralPublicKey").asOpt[String] shouldBe defined
      (jsonResult \ "encryptedData").asOpt[String] shouldBe defined
    }

    "return 200 with encrypted data when empRef file exists" in {
      when(mockEnactStageRepository.addCustomerCheckStage(meq(validCorrelationId), any[CustomerCheckRequest]()))
        .thenReturn(Future.successful(EnactStage(validCorrelationId)))

      val fakeRequest = FakeRequest("POST", "/scrbroker/accessmgmt")
        .withHeaders("correlationId" -> validCorrelationId)
        .withBody(Json.toJson(validEmpRefRequest))

      val result = controller.customerCheck()(fakeRequest)

      status(result) shouldBe Status.OK
      verify(mockEnactStageRepository).addCustomerCheckStage(meq(validCorrelationId), meq(validEmpRefRequest))
      val jsonResult = contentAsJson(result)
      (jsonResult \ "ephemeralPublicKey").asOpt[String] shouldBe defined
      (jsonResult \ "encryptedData").asOpt[String] shouldBe defined
    }

    "return 404 when file does not exist for identifier" in {
      when(mockEnactStageRepository.addCustomerCheckStage(meq(validCorrelationId), any[CustomerCheckRequest]()))
        .thenReturn(Future.successful(EnactStage(validCorrelationId)))

      val nonExistentRequest = validNinoRequest.copy(
        customers = List(
          CustomerCheckCustomer(
            universalCustomerId = Some(CustomerCheckUniversalCustomerId("uci123")),
            nino = Some(CustomerCheckNino("ZZ999999Z")),
            empRef = None
          )
        )
      )

      val fakeRequest = FakeRequest("POST", "/scrbroker/accessmgmt")
        .withHeaders("correlationId" -> validCorrelationId)
        .withBody(Json.toJson(nonExistentRequest))

      val result = controller.customerCheck()(fakeRequest)

      status(result) shouldBe Status.NOT_FOUND
      contentAsString(result) should include("not found")
    }

    "return 400 when no identifier (nino or empRef) is provided" in {
      val fakeRequest = FakeRequest("POST", "/scrbroker/accessmgmt")
        .withHeaders("correlationId" -> validCorrelationId)
        .withBody(Json.toJson(noIdentifierRequest))

      val result = controller.customerCheck()(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "No identifier found in request"
    }
  }
}
