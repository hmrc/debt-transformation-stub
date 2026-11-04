/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.*
import java.time.LocalDate

import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes.*

class CdcsCreateCaseRequestWrappedTypesSpec extends AnyFreeSpec with Matchers {

  "CDCS request wrappers" - {

    "serialise String values" in {
      Json.toJson(CdcsCreateCaseRequestAddressType("Home")) shouldBe JsString("Home")
    }

    "serialise Int values" in {
      Json.toJson(CdcsCreateCaseRequestInstalmentNumber(12)) shouldBe JsNumber(12)
    }

    "serialise BigInt values" in {
      Json.toJson(CdcsCreateCaseRequestInstalmentAmount(BigInt(1000))) shouldBe JsNumber(1000)
    }

    "serialise BigDecimal values" in {
      Json.toJson(CdcsCreateCaseRequestDebtItemAmount(BigDecimal("100.50"))) shouldBe JsNumber(BigDecimal("100.50"))
    }

    "serialise LocalDate values" in {
      val original = CdcsCreateCaseRequestInstalmentStartDate(LocalDate.parse("2026-11-04"))
      val json = Json.toJson(original)

      json.as[CdcsCreateCaseRequestInstalmentStartDate] shouldBe original
    }
  }
}
