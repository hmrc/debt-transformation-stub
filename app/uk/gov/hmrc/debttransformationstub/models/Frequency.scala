/*
 * Copyright 2021 HM Revenue & Customs
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

import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scala.collection.immutable

sealed abstract class Frequency(override val entryName: String) extends EnumEntry

object Frequency extends Enum[Frequency] with PlayJsonEnum[Frequency] {
  val values: immutable.IndexedSeq[Frequency] = findValues

  case object Single extends Frequency("single")
  case object Weekly extends Frequency("weekly")
  case object TwoWeekly extends Frequency("2Weekly")
  case object FourWeekly extends Frequency("4Weekly")
  case object Monthly extends Frequency("monthly")
  case object Quarterly extends Frequency("quarterly")
  case object SixMonthly extends Frequency("6Monthly")
  case object Annually extends Frequency("annually")
}
