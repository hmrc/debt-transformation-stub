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

import java.util.Base64
import scala.io.Source
import scala.util.{ Failure, Success, Try, Using }

/** INSTRUCTIONS The business spreadsheet that you copy into a csv file and add to test/resources/disallowedSpreadsheets
  * in time-to-pay-eligibility, Add this same file to the file in this repo:
  * test/uk/gov/hmrc/debttransformationstub/utils/etmpLocks/etmpLocksV5.csv Then rename the file to reflect the version
  * of the spreadsheet and update all the places it is referenced. Run BuildEtmpLocksTTPEligibility in this file. Copy
  * the output into the application.conf file in time-to-pay-eligibility time-to-pay-eligibility -> application.conf ->
  * etmpLocks { PASTE OUTPUT HERE }
  */
case class EtmpLock(
  lockReason: String,
  disallowPAYE: Boolean,
  disallowVAT: Boolean,
  disallowSa: Boolean,
  disallowSimp: Boolean
)
case class LockTypeAndLock(lockType: String, etmpLock: EtmpLock)

object BuildEtmpLocksTTPEligibility extends App {

  println(filterAndEncodeList(CsvData.expectedItems))

  // If the business name (in the spreadsheet) or the config name for the locks change update them here
  class LockTypes(val businessLockName: String, val configName: String)
  case object Dunning extends LockTypes(businessLockName = "dunning", configName = "dunning")
  case object CalculateInterest
      extends LockTypes(businessLockName = "calculateinterest", configName = "calculateInterest")
  case object ClearingLocks extends LockTypes(businessLockName = "posting/clearing", configName = "clearingLocks")
  case object PaymentLocks extends LockTypes(businessLockName = "payments", configName = "paymentLocks")

  def filterAndEncodeList(csvLocks: List[LockTypeAndLock]): String = {

    def encodeLock(lock: EtmpLock): String = {
      val encodedReason =
        lock.copy(lockReason = new String(Base64.getEncoder.encodeToString(lock.lockReason.getBytes())))

      s"{ lockReason = \"${encodedReason.lockReason}\", " +
        s"disallowPaye = ${lock.disallowPAYE}, disallowVat = ${lock.disallowVAT}, " +
        s"disallowSa = ${lock.disallowSa}, disallowSimp = ${lock.disallowSimp} }," +
        s" \n # lock reason = ${lock.lockReason}\n"
    }

    def filteredList(lockType: LockTypes): List[EtmpLock] =
      csvLocks
        .filter(
          _.lockType.trim.toLowerCase.replaceAll(" ", "") == lockType.businessLockName.trim.toLowerCase
            .replaceAll(" ", "")
        )
        .map(_.etmpLock)
        .sortBy(_.lockReason)

    val encodedDunningLocks: String = filteredList(Dunning).map { lock =>
      encodeLock(lock)
    }.mkString
    val encodedCalculateInterestLocks: String = filteredList(CalculateInterest).map { lock =>
      encodeLock(lock)
    }.mkString
    val encodedClearingLocksLocks: String = filteredList(ClearingLocks).map { lock =>
      encodeLock(lock)
    }.mkString
    val encodedPaymentLocksLocks: String = filteredList(PaymentLocks).map { lock =>
      encodeLock(lock)
    }.mkString

    s"${Dunning.configName} = [\n $encodedDunningLocks]\n " +
      s"${CalculateInterest.configName} = [\n $encodedCalculateInterestLocks]\n " +
      s"${ClearingLocks.configName} = [\n $encodedClearingLocksLocks]\n " +
      s"${PaymentLocks.configName} = [\n $encodedPaymentLocksLocks]\n"
  }

}

object CsvData {
  // update the csv file name for the locks here
  private val maybeCsvData: Try[String] =
    Using(Source.fromFile("test/uk/gov/hmrc/debttransformationstub/utils/etmpLocks/etmpLocksV5.csv")) { source =>
      source.mkString
    }

  val unprocessedRows: List[List[String]] = maybeCsvData match {
    case Failure(exception) => throw new IllegalStateException(s"Exception: $exception")
    case Success(csvData) =>
      csvData
        .asCsvReader[List[String]](rfc.withHeader())
        .map {
          case Left(error)  => throw new IllegalStateException(s"ReadError: $error")
          case Right(value) => value
        }
        .toList
  }

  val expectedItems: List[LockTypeAndLock] =
    unprocessedRows.map(readCsvRowOrThrow)

  private def readCsvRowOrThrow(row: List[String]): LockTypeAndLock =
    row match {
      case lockType :: reasonCell :: preventPayeCell :: preventVatCell :: preventSaCell :: preventSimpCell :: _ =>
        def extractValue(uncheckedValue: String): Boolean =
          uncheckedValue.trim match {
            case "yes" => true
            case "no"  => false
            case invalid =>
              throw new IllegalStateException(
                s"""Illegal value provided: $invalid
                   |Row: $row""".stripMargin
              )
          }
        LockTypeAndLock(
          lockType,
          EtmpLock(
            reasonCell.trim,
            extractValue(preventPayeCell),
            extractValue(preventVatCell),
            extractValue(preventSaCell),
            extractValue(preventSimpCell)
          )
        )
      case other => throw new IllegalStateException(s"Row does not match pattern: $other")
    }
}
