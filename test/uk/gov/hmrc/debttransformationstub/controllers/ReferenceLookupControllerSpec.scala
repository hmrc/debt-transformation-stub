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

package uk.gov.hmrc.debttransformationstub.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Environment
import play.api.http.Status
import play.api.test.Helpers.status
import play.api.test.{DefaultAwaitTimeout, FakeRequest, Helpers}
import uk.gov.hmrc.http.HeaderCarrier

class ReferenceLookupControllerSpec extends AnyWordSpec with Matchers with GuiceOneServerPerSuite with DefaultAwaitTimeout {

  val cc = Helpers.stubControllerComponents()
  val env = app.injector.instanceOf[Environment]
  implicit lazy val hc = HeaderCarrier()

  private val fakeRequest = FakeRequest("GET", "/").withHeaders()
  private val controller = new ReferenceLookupController(env, cc)

  "GET /" should {
    "return 200" in {
      val result = controller.getList()(fakeRequest)
      status(result) shouldBe Status.OK
    }
  }
}
