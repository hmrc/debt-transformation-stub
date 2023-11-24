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

import play.api.libs.json.{ JsObject, Json }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.TableData.{ CellValue, Heading }

import scala.collection.immutable.ListMap

/** Parsed CSV or TSV data. */
final case class TableData(headings: Vector[Heading], dataRows: Vector[Vector[CellValue]]) {

  lazy val jsonDescription: JsObject = JsObject(
    List(
      "headings" -> Json.toJson(headings.map(_.name)),
      "rows"     -> Json.toJson(dataRows.map(_.map(_.actual)))
    )
  )

  private val columnNumbersByHeadings: ListMap[Int, Heading] = ListMap.from(headings.zipWithIndex.map(_.swap))

  locally {
    require(
      columnNumbersByHeadings.size == headings.size,
      s"Found duplicated headings: ${Json.toJson(headings.map(_.name))}"
    )

    dataRows.zipWithIndex.foreach { case (dataRow, dataRowIndex: Int) =>
      require(dataRow.size == width, s"Data row $dataRowIndex has width ${dataRow.size} instead of $width.")
    }
  }

  def width: Int = headings.size

  def dataRowAt(dataRowIndex: Int): ListMap[Heading, CellValue] = {
    val columnEntries: Seq[(Heading, CellValue)] =
      dataRows(dataRowIndex).zipWithIndex
        .map { case (cell, columnIndex) =>
          (columnNumbersByHeadings(columnIndex), cell) // No validation of duplicates.
        }

    ListMap.from(columnEntries)
  }

  override def toString: String = Json.prettyPrint(jsonDescription)
}

object TableData {
  def fromCsvOrTsvRowsWithHeadings(rowsWithHeadings: Seq[String], separator: String): TableData = {
    val parsedRowsWithRawRows: Vector[(Vector[String], String)] =
      rowsWithHeadings
        .map(rawRow => (splitCsvOrTsvRow(row = rawRow, separator = separator), rawRow))
        .toVector

    val headings = parsedRowsWithRawRows.head._1.map(Heading)
    val headingCount = headings.size

    val rows: Vector[Vector[CellValue]] =
      parsedRowsWithRawRows.zipWithIndex.tail
        .map { case ((parsedRow, rawRow), rowIndex) =>
          val rawRowPrintable = Json.toJson(rawRow)
          val headingsPrintable = Json.toJson(headings.map(_.name))
          require(
            parsedRow.size <= headingCount,
            s"In CSV row $rowIndex, expected $headingCount columns but found ${parsedRow.size}: $rawRowPrintable for headings: $headingsPrintable"
          )
          parsedRow.map(CellValue(_)) ++ Iterator.fill(headingCount - parsedRow.size)(CellValue.empty)
        }

    TableData(headings, rows)
  }

  private def splitCsvOrTsvRow(row: String, separator: String): Vector[String] =
    // Splitting by commas in CSV isn't ideal because there may be escaping.
    row.split(separator).toVector

  final case class Heading(name: String) extends AnyVal

  final case class CellValue(actual: String) extends AnyVal
  object CellValue {
    def empty: CellValue = CellValue("")
  }
}
