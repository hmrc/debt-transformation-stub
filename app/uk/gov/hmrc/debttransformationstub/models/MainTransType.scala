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
}
