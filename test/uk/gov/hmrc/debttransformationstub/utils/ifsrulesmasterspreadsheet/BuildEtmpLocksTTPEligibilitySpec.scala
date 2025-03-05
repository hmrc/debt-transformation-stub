/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet

import org.mockito.MockitoSugar.mock
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.debttransformationstub.utils.etmpLocks.{BuildEtmpLocksTTPEligibility, EtmpLock, LockTypeAndLock}
import uk.gov.hmrc.http.HeaderCarrier

class BuildEtmpLocksTTPEligibilitySpec extends AnyFreeSpec {
  implicit val hc: HeaderCarrier = HeaderCarrier()
    ".filterAndEncodeList()" - {
      "given a list of LockTypeAndLock containing all 4 lock types should correctly encode them in a appropriate application.conf file format" in {
        val locks = List(
          LockTypeAndLock("dunning", EtmpLock("complaint  (level 1)", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock("calculateinterest", EtmpLock("breathing space moratorium act", disallowPaye = false, disallowVat = false, disallowSa = false, disallowSimp = false)),
          LockTypeAndLock("posting/clearing", EtmpLock("await applevy charge", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock("payments", EtmpLock("audit/compliance", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = true)),
        )
        
        BuildEtmpLocksTTPEligibility.filterAndEncodeList(locks) shouldBe
        "dunning = [\n" +
          " { lockReason = \"Y29tcGxhaW50ICAobGV2ZWwgMSk=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
          "\n # lock reason = complaint  (level 1)" +
          "\n]\n" +
          "calculateInterest = [\n" +
          " { lockReason = \"YnJlYXRoaW5nIHNwYWNlIG1vcmF0b3JpdW0gYWN0\", disallowPaye = false, disallowVat = false, disallowSa = false, disallowSimp = false }, " +
          "\n # lock reason = breathing space moratorium act" +
          "\n]\n" +
          "clearingLocks = [\n" +
          " { lockReason = \"YXdhaXQgYXBwbGV2eSBjaGFyZ2U=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
          "\n # lock reason = await applevy charge" +
          "\n]\n" +
          "paymentLocks = [\n" +
          " { lockReason = \"YXVkaXQvY29tcGxpYW5jZQ==\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = true }, " +
          "\n # lock reason = audit/compliance" +
          "\n]"
      }

      "given a list of LockTypeAndLock containing all 4 lock types with spaces and case differences should correctly encode them in a appropriate application.conf file format" in {
        val locks = List(
          LockTypeAndLock("d uNniNg ", EtmpLock("complaint  (level 1)", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock(" Calculate interest", EtmpLock("breathing space moratorium act", disallowPaye = false, disallowVat = false, disallowSa = false, disallowSimp = false)),
          LockTypeAndLock("POsting / cleaRing", EtmpLock("await applevy charge", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock(" PaymEnts ", EtmpLock("audit/compliance", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = true)),
        )

        BuildEtmpLocksTTPEligibility.filterAndEncodeList(locks) shouldBe
          "dunning = [\n" +
            " { lockReason = \"Y29tcGxhaW50ICAobGV2ZWwgMSk=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
            "\n # lock reason = complaint  (level 1)" +
            "\n]\n" +
            "calculateInterest = [\n" +
            " { lockReason = \"YnJlYXRoaW5nIHNwYWNlIG1vcmF0b3JpdW0gYWN0\", disallowPaye = false, disallowVat = false, disallowSa = false, disallowSimp = false }, " +
            "\n # lock reason = breathing space moratorium act" +
            "\n]\n" +
            "clearingLocks = [\n" +
            " { lockReason = \"YXdhaXQgYXBwbGV2eSBjaGFyZ2U=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
            "\n # lock reason = await applevy charge" +
            "\n]\n" +
            "paymentLocks = [\n" +
            " { lockReason = \"YXVkaXQvY29tcGxpYW5jZQ==\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = true }, " +
            "\n # lock reason = audit/compliance" +
            "\n]"
      }

      "given a list of LockTypeAndLock containing some lock types should correctly encode them in a appropriate application.conf file format, leaving sections of other types empty" in {
        val locks = List(
          LockTypeAndLock("dunning ", EtmpLock("complaint  (level 1)", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock("posting/clearing", EtmpLock("await applevy charge", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false)),
          LockTypeAndLock("ahhhh", EtmpLock("await applevy charge", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false))
        )

        BuildEtmpLocksTTPEligibility.filterAndEncodeList(locks) shouldBe
          "dunning = [\n" +
            " { lockReason = \"Y29tcGxhaW50ICAobGV2ZWwgMSk=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
            "\n # lock reason = complaint  (level 1)" +
            "\n]\n" +
            "calculateInterest = [\n ]\n" +
            "clearingLocks = [\n" +
            " { lockReason = \"YXdhaXQgYXBwbGV2eSBjaGFyZ2U=\", disallowPaye = false, disallowVat = true, disallowSa = true, disallowSimp = false }, " +
            "\n # lock reason = await applevy charge" +
            "\n]\n" +
            "paymentLocks = [\n ]"
      }


    }
}
