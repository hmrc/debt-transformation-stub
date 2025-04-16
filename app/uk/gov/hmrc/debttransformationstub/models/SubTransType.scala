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

package uk.gov.hmrc.debttransformationstub.models

import enumeratum.{ Enum, EnumEntry, PlayJsonEnum }
import scala.collection.immutable

sealed abstract class SubTransType(override val entryName: String) extends EnumEntry

object SubTransType extends Enum[SubTransType] with PlayJsonEnum[SubTransType] {
  val values: immutable.IndexedSeq[SubTransType] = findValues

  case object ChBDebt extends SubTransType("7006")
  case object GuardiansGBDebt extends SubTransType("7010")
  case object GuardiansNIDebt extends SubTransType("7011")
  case object ChBMigratedDebt extends SubTransType("7012")
  case object GuardiansGBChBMigratedDebt extends SubTransType("7014")
  case object GuardiansNIChBMigratedDebt extends SubTransType("7013")
  case object IT extends SubTransType("1000")
  case object IBFANDOFPInterestBearing extends SubTransType("1091")
  case object IBFANDOFPNonInterestBearing extends SubTransType("2091")
  case object NICGB extends SubTransType("1020")
  case object NICNI extends SubTransType("1025")
  case object HIPG extends SubTransType("1180")
  case object INTIT extends SubTransType("2000")
  case object TGPEN extends SubTransType("1090")
  case object TakingControlFee extends SubTransType("1150")
  case object SAOpLed extends SubTransType("1553")
  case object SAOpLedCreatePlan extends SubTransType("1090")
}
