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

package uk.gov.hmrc.debttransformationstub.utils

import java.util.Base64

object BuildEtmpLocksTTPEligibility extends App {

  case class EtmpLock(lockReason: String, disallowPAYE: Boolean, disallowVAT: Boolean)

  def encodeList(notEncodedLockReasons: List[EtmpLock]): List[String] =
    notEncodedLockReasons.map { item =>
      val encodedReason =
        item.copy(lockReason = new String(Base64.getEncoder.encodeToString(item.lockReason.getBytes())))
      s"\n{ lockReason =  ${encodedReason.lockReason}, disallowPAYE = ${item.disallowPAYE}, disallowVAT = ${item.disallowVAT} }, \n # lock reason = ${item.lockReason}"
    }

  //encodeList(List(EtmpLock(lockReason = "example lock reason", disallowPAYE = true, disallowVAT = false)))
  // when you call this method put it in a print line

}
