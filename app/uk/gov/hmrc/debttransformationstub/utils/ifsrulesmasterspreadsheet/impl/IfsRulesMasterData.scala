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

package uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl

import play.api.libs.json.{ JsObject, JsString, JsValue, Json }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.IfsRulesMasterData.{ KnownHeadings, RegimeUsage }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.TableData.{ CellValue, Heading }

import enumeratum.{ Enum, EnumEntry }

final case class IfsRulesMasterData(
  tableData: TableData
) {

  private lazy val jsonDescription: JsObject = JsObject(
    List(
      "tableData" -> tableData.jsonDescription
    )
  )

  def maxLength: Int = tableData.dataRows.length

  object Interpreted {
    def mainTrans(index: Int): String = Lookup2D.mainTransAt(index).actual

    def subTrans(index: Int): String = Lookup2D.subTransAt(index).actual

    def interestBearingId(index: Int): Int =
      if (isInterestBearing(index)) 4 else 0

    def isInterestBearing(index: Int): Boolean =
      Lookup2D.interestBearingAt(index).actual match {
        case ""  => false
        case "N" => false
        case "Y" => true
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(tableData.dataRowAt(index).values.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert interestBearing=${JsString(unknown)} to boolean; check the code. Row values: $rowDisplay"
          )
      }

    def interestOnlyDebt(index: Int): Boolean =
      Lookup2D.interestOnlyDebtAt(index).actual match {
        case ""  => false
        case "N" => false
        case "Y" => true
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(tableData.dataRowAt(index).values.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert interestOnlyDebt=${JsString(unknown)} to boolean; check the code. Row values: $rowDisplay"
          )
      }

    def useChargeReference(index: Int): Option[Boolean] = {
      val chargeReference: String = Lookup2D.chargeReferenceAt(index).actual

      val saRegimeList = List(
        RegimeUsage.`SA SSTTP AND NOT into IFS and SoL`,
        RegimeUsage.`SA SSTTP AND into IFS and SoL`,
        RegimeUsage.`SA SSTTP AND into IFS and SoL AND Op Led`
      )

      chargeReference.toLowerCase match {
        case "n/a"        => if (saRegimeList.contains(regimeUsage(index))) Some(false) else None
        case "charge ref" => Some(true)
        case "asn"        => Some(false)
        case "vrn"        => Some(false)
        case "utr"        => Some(false)
        case _ =>
          val rowDisplay: JsValue = Json.toJson(tableData.dataRowAt(index).values.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert useChargeReference=${JsString(chargeReference)} to boolean; check the code. Row values: $rowDisplay"
          )
      }
    }

    def regimeUsage(index: Int): RegimeUsage =
      Lookup2D.regimeUsage(index).actual match {
        case "CDCS"                                     => RegimeUsage.Cdcs
        case "PAYE"                                     => RegimeUsage.Paye
        case "VAT"                                      => RegimeUsage.Vat
        case "SA into IFS and SoL AND NOT SSTTP"        => RegimeUsage.`SA into IFS and SoL AND NOT SSTTP`
        case "SA SSTTP AND NOT into IFS and SoL"        => RegimeUsage.`SA SSTTP AND NOT into IFS and SoL`
        case "SA SSTTP AND into IFS and SoL"            => RegimeUsage.`SA SSTTP AND into IFS and SoL`
        case "SA SSTTP AND into IFS and SoL AND Op Led" => RegimeUsage.`SA SSTTP AND into IFS and SoL AND Op Led`
        case "SIA"                                      => RegimeUsage.Sia
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(tableData.dataRowAt(index).values.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert regimeUsage=${JsString(unknown)} to RegimeUsage; check the code. Row values: $rowDisplay"
          )
      }
  }

  private object Lookup2D {
    def mainTransAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.mainTrans)

    def subTransAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.subTrans)

    def interestKeyAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.interestKey)

    def interestBearingAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.interestBearing)

    def interestOnlyDebtAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.interestOnlyDebt)

    def chargeReferenceAt(index: Int): CellValue = cellValueAt(index, KnownHeadings.chargeReference)

    def regimeUsage(index: Int): CellValue = cellValueAt(index, KnownHeadings.regimeUsage)

    def cellValueAt(rowIndex: Int, heading: Heading): CellValue =
      tableData.dataRowAt(rowIndex)(heading)
  }

  override def toString: String = Json.prettyPrint(jsonDescription)
}

object IfsRulesMasterData {

  def fromMasterSpreadsheetCsv(csv: Seq[String]): IfsRulesMasterData =
    // Simply splitting by comma is kind of dumb because there MAY be escaped/quoted commas.
    fromMasterSpreadsheetDelimited(rows = csv, cellSeparator = ",", keepRow = csvOrTsvRowNonEmpty)

  def fromMasterSpreadsheetTsv(tsv: Seq[String]): IfsRulesMasterData =
    fromMasterSpreadsheetDelimited(rows = tsv, cellSeparator = "\t", keepRow = csvOrTsvRowNonEmpty)

  private def csvOrTsvRowNonEmpty(row: String): Boolean = !row.matches("^[\\s,]*$")

  private def fromMasterSpreadsheetDelimited(
    rows: Seq[String],
    cellSeparator: String,
    keepRow: String => Boolean
  ): IfsRulesMasterData = {

    val tableData: TableData = TableData.fromCsvOrTsvRowsWithHeadings(
      rowsWithHeadings = rows.filter(keepRow),
      separator = cellSeparator
    )

    IfsRulesMasterData(tableData)
  }

  private object KnownHeadings {
    val mainTrans: Heading = Heading("Main Trans")
    val subTrans: Heading = Heading("Sub Trans")
    val interestBearing: Heading = Heading("Interest bearing")
    val interestKey: Heading = Heading("Interest key")
    val interestOnlyDebt: Heading = Heading("Interest only Debt")
    val chargeReference: Heading = Heading("Charge Ref")
    val regimeUsage: Heading = Heading("Regime Usage")
  }

  sealed abstract class RegimeUsage(val isForSelfServe: Boolean, val isForIfs: Boolean) extends EnumEntry

  object RegimeUsage extends Enum[RegimeUsage] {
    case object Cdcs extends RegimeUsage(isForSelfServe = true, isForIfs = true)
    case object Paye extends RegimeUsage(isForSelfServe = true, isForIfs = true)
    case object Vat extends RegimeUsage(isForSelfServe = true, isForIfs = true)
    case object `SA into IFS and SoL AND NOT SSTTP` extends RegimeUsage(isForSelfServe = false, isForIfs = true)
    case object `SA SSTTP AND NOT into IFS and SoL` extends RegimeUsage(isForSelfServe = true, isForIfs = true)
    case object Sia extends RegimeUsage(isForSelfServe = true, isForIfs = true)

    /** Debts that can be used for both SSTTP and Operator led. */
    case object `SA SSTTP AND into IFS and SoL` extends RegimeUsage(isForSelfServe = true, isForIfs = true)
    case object `SA SSTTP AND into IFS and SoL AND Op Led` extends RegimeUsage(isForSelfServe = true, isForIfs = true)

    override def values: IndexedSeq[RegimeUsage] = findValues
  }
}
