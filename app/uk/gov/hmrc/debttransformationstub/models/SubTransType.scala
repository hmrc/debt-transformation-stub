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
  case object SAOpLed1005 extends SubTransType("1005")
  case object SAOpLed1007 extends SubTransType("1007")
  case object SAOpLed1008 extends SubTransType("1008")
  case object SAOpLed1009 extends SubTransType("1009")
  case object SAOpLed1010 extends SubTransType("1010")
  case object SAOpLed1011 extends SubTransType("1011")
  case object SAOpLed1012 extends SubTransType("1012")
  case object SAOpLed1015 extends SubTransType("1015")
  case object SAOpLed1042 extends SubTransType("1042")
  case object SAOpLed1044 extends SubTransType("1044")
  case object SAOpLed1046 extends SubTransType("1046")
  case object SAOpLed1047 extends SubTransType("1047")
  case object SAOpLed1060 extends SubTransType("1060")
  case object SAOpLed1096 extends SubTransType("1096")
  case object SAOpLed1555 extends SubTransType("1555")
  case object SAOpLed1560 extends SubTransType("1560")
  case object SAOpLed1565 extends SubTransType("1565")
  case object SAOpLed1570 extends SubTransType("1570")
  case object SAOpLed1575 extends SubTransType("1575")
  case object SAOpLed1580 extends SubTransType("1580")
  case object SAOpLed1585 extends SubTransType("1585")
  case object SAOpLed1590 extends SubTransType("1590")
  case object SAOpLed1595 extends SubTransType("1595")
  case object SAOpLed1600 extends SubTransType("1600")
  case object SAOpLed1605 extends SubTransType("1605")
  case object SAOpLed1610 extends SubTransType("1610")
  case object SAOpLed1680 extends SubTransType("1680")
  case object SAOpLed1685 extends SubTransType("1685")
  case object SAOpLed2195 extends SubTransType("2195")
  case object SAOpLed2200 extends SubTransType("2200")
  case object SAOpLed2205 extends SubTransType("2205")
  case object SAOpLed2210 extends SubTransType("2210")
  case object SAOpLedCreatePlan extends SubTransType("1090")
  case object PenaltyReformCharge1611 extends SubTransType("1611")
  case object PenaltyReformCharge2090 extends SubTransType("2090")
  case object PenaltyReformCharge2095 extends SubTransType("2095")
  case object PenaltyReformCharge2096 extends SubTransType("2096")
  case object PenaltyReformCharge1080 extends SubTransType("1080")
  case object PenaltyReformCharge1085 extends SubTransType("1085")
  case object PenaltyReformCharge1090 extends SubTransType("1090")
  case object PenaltyReformCharge1095 extends SubTransType("1095")
}
