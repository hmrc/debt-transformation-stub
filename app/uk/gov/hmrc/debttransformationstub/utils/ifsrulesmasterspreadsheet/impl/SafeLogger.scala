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

/** For command-line tools following the POSIX standard of logging to stderr.
  * @see
  *   https://pubs.opengroup.org/onlinepubs/9699919799/functions/stderr.html
  */
trait SafeLogger {
  protected def printString(chars: String): Unit

  final def log(obj: Any): Unit = printString(obj.toString + "\n")
}

object SafeLogger {
  def stderr: SafeLogger = System.err.print _
}
