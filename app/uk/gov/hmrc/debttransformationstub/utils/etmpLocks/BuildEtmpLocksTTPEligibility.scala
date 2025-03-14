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

package uk.gov.hmrc.debttransformationstub.utils.etmpLocks

import kantan.csv._
import kantan.csv.ops.toCsvInputOps
import uk.gov.hmrc.debttransformationstub.utils.RequestAwareLogger
import uk.gov.hmrc.http.HeaderCarrier

import java.util.Base64
import scala.io.Source
import scala.util.{ Failure, Success, Try, Using }

/** INSTRUCTIONS From the business spreadsheet you will have copied it into a csv file and added it to
  * test/resources/disallowedSpreadsheets in time-to-pay-eligibility. Add this same file to this location in this repo,
  * replacing the existing one: test/uk/gov/hmrc/debttransformationstub/utils/etmpLocks/etmpLocksV5.csv Rename the csv
  * file to reflect the spreadsheet version and update the place it is referenced in this file. In this file run
  * BuildEtmpLocksTTPEligibility object. Copy the output into the application.conf file in time-to-pay-eligibility
  * time-to-pay-eligibility -> application.conf -> etmpLocks { PASTE OUTPUT HERE }
  */

case class EtmpLock(
  lockReason: String,
  disallowPaye: Boolean,
  disallowVat: Boolean,
  disallowSa: Boolean,
  disallowSimp: Boolean
)
case class LockTypeAndLock(lockType: String, etmpLock: EtmpLock)

// If the business name (in the spreadsheet) or the config name for the locks change update them here
sealed abstract class LockTypes(val businessLockName: String, val configName: String)
object LockTypes {
  case object Dunning extends LockTypes("dunning", "dunning")
  case object CalculateInterest extends LockTypes("calculateinterest", "calculateInterest")
  case object ClearingLocks extends LockTypes("posting/clearing", "clearingLocks")
  case object PaymentLocks extends LockTypes("payments", "paymentLocks")

  val all: Set[LockTypes] = Set(Dunning, CalculateInterest, ClearingLocks, PaymentLocks)

  def fromName(name: String): Option[LockTypes] =
    all.find(_.businessLockName.replaceAll(" ", "").equalsIgnoreCase(name.replaceAll(" ", "")))
}

object BuildEtmpLocksTTPEligibility extends App {
  private lazy val logger = new RequestAwareLogger(this.getClass)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  println(filterAndEncodeList(CsvData.expectedItems))

  def filterAndEncodeList(csvLocks: List[LockTypeAndLock])(implicit hc: HeaderCarrier): String = {
    val validLockTypes = LockTypes.all.map(_.businessLockName.toLowerCase.replaceAll(" ", ""))

    val unrecognizedLocks =
      csvLocks.filterNot(lock => validLockTypes.contains(lock.lockType.toLowerCase.replaceAll(" ", "")))

    if (unrecognizedLocks.nonEmpty) {
      logger.info(s"Unrecognized LockTypes: ${unrecognizedLocks.map(_.lockType).distinct.mkString(", ")}")
    }

    def encodeLock(lock: EtmpLock): String =
      s"  { lockReason = \"${Base64.getEncoder.encodeToString(lock.lockReason.getBytes)}\", " +
        s"disallowPaye = ${lock.disallowPaye}, disallowVat = ${lock.disallowVat}, " +
        s"disallowSa = ${lock.disallowSa}, disallowSimp = ${lock.disallowSimp} },\n" +
        s"  # lock reason = ${lock.lockReason}\n"

    def filteredAndEncodedLocks(lockType: LockTypes): String =
      csvLocks
        .collect {
          case LockTypeAndLock(lockTypeStr, lock) if LockTypes.fromName(lockTypeStr).contains(lockType) =>
            encodeLock(lock)
        }
        .sorted
        .mkString

    LockTypes.all.map(lockType => s"${lockType.configName} = [\n${filteredAndEncodedLocks(lockType)}]\n").mkString
  }
}

object CsvData {
  // update the csv file name for the locks here
  private val maybeCsvData: Try[String] =
    Using(Source.fromFile("app/uk/gov/hmrc/debttransformationstub/utils/etmpLocks/etmpLocksV5.csv"))(_.mkString)

  private val unprocessedRows: List[List[String]] = maybeCsvData match {
    case Failure(exception) => throw new IllegalStateException(s"Exception: $exception")
    case Success(csvData) =>
      csvData
        .asCsvReader[List[String]](rfc.withHeader())
        .collect { case Right(value) =>
          value
        }
        .toList
  }

  val expectedItems: List[LockTypeAndLock] = unprocessedRows.map(readCsvRowOrThrow)

  private def readCsvRowOrThrow(row: List[String]): LockTypeAndLock = row match {
    case lockType :: reason :: paye :: vat :: sa :: simp :: _ =>
      LockTypeAndLock(
        lockType,
        EtmpLock(reason.trim, extractBoolean(paye), extractBoolean(vat), extractBoolean(sa), extractBoolean(simp))
      )
    case other => throw new IllegalStateException(s"Row does not match pattern: $other")
  }

  private def extractBoolean(value: String): Boolean = value.trim match {
    case "yes"   => true
    case "no"    => false
    case invalid => throw new IllegalStateException(s"Illegal value: $invalid")
  }
}
