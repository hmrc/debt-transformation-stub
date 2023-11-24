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

import play.api.libs.json.{ JsObject, JsValue, Json }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.IfsRulesMasterData.KnownHeadings
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.TableData.{ CellValue, Heading }

final case class IfsRulesMasterData(cdcs: TableData, paye: TableData, vat: TableData) {

  private lazy val jsonDescription: JsObject = JsObject(
    List(
      "CDCS" -> cdcs.jsonDescription,
      "PAYE" -> paye.jsonDescription,
      "VAT"  -> vat.jsonDescription
    )
  )

  private val masterCollection =
    cdcs.dataRows.zipWithIndex.map { case (dr, idx) => (idx, cdcs, dr) } ++
      paye.dataRows.zipWithIndex.map { case (dr, idx) => (idx, paye, dr) } ++
      vat.dataRows.zipWithIndex.map { case (dr, idx) => (idx, vat, dr) }

  def length: Int = masterCollection.size

  object Interpreted {
    def mainTrans(index: Int): String = Lookup2D.mainTransAt(index).actual

    def subTrans(index: Int): String = Lookup2D.subTransAt(index).actual

    def interestBearing(index: Int): Int =
      Lookup2D.interestBearingAt(index).actual match {
        case ""  => 0
        case "N" => 0
        case "Y" => 4
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(masterCollection(index)._3.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert interestBearing=${Json.toJson(unknown)} to boolean; check the code. Row values: $rowDisplay"
          )
      }

    def interestOnlyDebt(index: Int): Boolean =
      Lookup2D.interestOnlyDebtAt(index).actual match {
        case ""  => false
        case "N" => false
        case "Y" => true
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(masterCollection(index)._3.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert interestOnlyDebt=${Json.toJson(unknown)} to boolean; check the code. Row values: $rowDisplay"
          )
      }

    def useChargeReference(index: Int): Option[Boolean] =
      Lookup2D.chargeReferenceAt(index).actual match {
        case "N/A"        => None
        case "Charge ref" => Some(true)
        case "ASN"        => Some(false)
        case "VRN"        => Some(false)
        case unknown =>
          val rowDisplay: JsValue = Json.toJson(masterCollection(index)._3.map(_.actual))
          throw new IllegalArgumentException(
            s"Cannot convert useChargeReference=${Json.toJson(unknown)} to boolean; check the code. Row values: $rowDisplay"
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

    def cellValueAt(rowIndex: Int, heading: Heading): CellValue =
      masterCollection(rowIndex) match {
        case (index, table, _) => table.dataRowAt(index)(heading)
      }

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

    locally {
      val expectedHeadings =
        Set(TransactionSection.payeSectionHeading.value, TransactionSection.vatSectionHeading.value)

      val headingsLines = rows.filter(expectedHeadings.contains)
      if (headingsLines.size != 2) {
        throw new IllegalArgumentException(
          s"Expected headings ${Json.toJson(expectedHeadings)}, but found only ${Json
            .toJson(headingsLines)} in rows ${Json.prettyPrint(Json.toJson(rows))}"
        )
      }
    }

    val cdcsSectionRaw = rows.takeWhile(row => !row.startsWith(TransactionSection.payeSectionHeading.value))
    val sectionAfterCdcs = rows.drop(cdcsSectionRaw.size + 1)
    val payeSectionRaw = sectionAfterCdcs.takeWhile(row => !row.startsWith(TransactionSection.vatSectionHeading.value))
    val vatSectionRaw = sectionAfterCdcs.drop(payeSectionRaw.size + 1)

    val cdcsData = TableData.fromCsvOrTsvRowsWithHeadings(
      rowsWithHeadings = cdcsSectionRaw.filter(keepRow),
      separator = cellSeparator
    )
    val payeData = TableData.fromCsvOrTsvRowsWithHeadings(
      rowsWithHeadings = payeSectionRaw.filter(keepRow),
      separator = cellSeparator
    )
    val vatData = TableData.fromCsvOrTsvRowsWithHeadings(
      rowsWithHeadings = vatSectionRaw.filter(keepRow),
      separator = cellSeparator
    )

    IfsRulesMasterData(cdcs = cdcsData, paye = payeData, vat = vatData)
  }

  private object KnownHeadings {
    val mainTrans: Heading = Heading("Main Trans")
    val subTrans: Heading = Heading("Sub Trans")
    val interestBearing: Heading = Heading("Interest bearing")
    val interestKey: Heading = Heading("Interest key")
    val interestOnlyDebt: Heading = Heading("Interest only Debt")
    val chargeReference: Heading = Heading("Charge Ref")
  }

  private object TransactionSection {
    val cdcsSectionHeading: None.type = None
    val payeSectionHeading: Some[String] = Some("PAYE")
    val vatSectionHeading: Some[String] = Some("VAT")
  }
}
