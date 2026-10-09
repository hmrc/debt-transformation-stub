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

sealed abstract class MainTransType(override val entryName: String) extends EnumEntry

object MainTransType extends Enum[MainTransType] with PlayJsonEnum[MainTransType] {
  val values: immutable.IndexedSeq[MainTransType] = findValues

  case object ChBDebt extends MainTransType("5330")
  case object ChBMigratedDebt extends MainTransType("5350")
  case object DrierIt extends MainTransType("1085")
  case object TPSSAFT extends MainTransType("1511")
  case object TPSSFailureToSubmit extends MainTransType("1515")
  case object TPSSPenalty extends MainTransType("1520")
  case object TPSSAccTaxAssessment extends MainTransType("1525")
  case object IBFANDOFPInterestBearing extends MainTransType("3996")
  case object IBFANDOFPNonInterestBearing extends MainTransType("3997")
  case object IBF extends MainTransType("4618")
  case object TPSSAFTAssessment extends MainTransType("1526")
  case object TPSSSchemaSanction extends MainTransType("1530")
  case object TPSSSchemaSanctionNT extends MainTransType("1531")
  case object TPSSLumpSum extends MainTransType("1535")
  case object TPSSLumpSumINT extends MainTransType("1536")
  case object TPSSUnreportedLiability extends MainTransType("1540")
  case object TPSSUnreportedLiabilityINT extends MainTransType("1541")
  case object TPSSContractSettlement extends MainTransType("1545")
  case object TPSSContractSettlementINT extends MainTransType("1546")
  case object TCOGFee extends MainTransType("2421")
  case object NIDistraintCosts extends MainTransType("1441")
  case object SAOpLed extends MainTransType("5210")
  case object SAOpLed4000 extends MainTransType("4000")
  case object SAOpLed4001 extends MainTransType("4001")
  case object SAOpLed4002 extends MainTransType("4002")
  case object SAOpLed4003 extends MainTransType("4003")
  case object SAOpLed4026 extends MainTransType("4026")
  case object SAOpLed4910 extends MainTransType("4910")
  case object SAOpLed4911 extends MainTransType("4911")
  case object SAOpLed4913 extends MainTransType("4913")
  case object SAOpLed4915 extends MainTransType("4915")
  case object SAOpLed4920 extends MainTransType("4920")
  case object SAOpLed4930 extends MainTransType("4930")
  case object SAOpLed4941 extends MainTransType("4941")
  case object SAOpLedCreatePlan extends MainTransType("4980")
  case object PenaltyReformCharge6010 extends MainTransType("6010")
  case object PenaltyReformCharge4027 extends MainTransType("4027")
  case object PenaltyReformCharge4028 extends MainTransType("4028")
  case object PenaltyReformCharge4029 extends MainTransType("4029")
  case object PenaltyReformCharge4031 extends MainTransType("4031")
  case object PenaltyReformCharge4032 extends MainTransType("4032")
  case object PenaltyReformCharge4033 extends MainTransType("4033")
  case object AmcCharge1425 extends MainTransType("1425")
  case object AmcCharge1430 extends MainTransType("1430")
  case object AmcCharge1435 extends MainTransType("1435")
  case object AmcCharge1440 extends MainTransType("1440")
  case object AmcCharge1615 extends MainTransType("1615")
  case object AmcCharge1617 extends MainTransType("1617")
  case object AmcCharge1618 extends MainTransType("1618")
  case object AmcCharge1620 extends MainTransType("1620")
  case object AmcCharge1625 extends MainTransType("1625")
  case object AmcCharge2420 extends MainTransType("2420")
  case object AmcCharge6231 extends MainTransType("6231")
  case object AmcCharge6232 extends MainTransType("6232")
  case object AmcCharge6233 extends MainTransType("6233")
  case object AmcCharge6234 extends MainTransType("6234")
  case object AmcCharge6235 extends MainTransType("6235")
  case object AmcCharge6236 extends MainTransType("6236")
  case object AmcCharge6237 extends MainTransType("6237")
  case object AmcCharge6238 extends MainTransType("6238")
}
