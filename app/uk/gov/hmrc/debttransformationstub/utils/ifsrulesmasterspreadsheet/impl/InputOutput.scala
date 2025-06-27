/*
 * Copyright 2025 HM Revenue & Customs
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

import scala.io.Source
import scala.util.Using

trait FileInput {
  def readFile(filename: String): IterableOnce[String]
}

trait ConsoleInput {
  def stdin: Iterator[String]
}

trait ConsoleOutput {
  def stdoutWrite(text: String): Unit

  final def stdoutWriteln(text: String): Unit = stdoutWrite(text + "\n")
}

trait DebugOutput {
  def debugWriteln(line: String): Unit
}

trait ApplicationInput extends FileInput with ConsoleInput
object ApplicationInput {
  def apply(fileInput: FileInput, consoleInput: ConsoleInput): ApplicationInput =
    new ApplicationInput {
      def readFile(filename: String): IterableOnce[String] = fileInput.readFile(filename)
      def stdin: Iterator[String] = consoleInput.stdin
    }
}

trait InputOutput extends ApplicationInput with ConsoleOutput with DebugOutput

object InputOutput {
  def apply(
    fileInput: FileInput,
    consoleInput: ConsoleInput,
    consoleOutput: ConsoleOutput,
    debugOutput: DebugOutput
  ): InputOutput =
    new InputOutput {
      def readFile(filename: String): IterableOnce[String] = fileInput.readFile(filename)
      def stdin: Iterator[String] = consoleInput.stdin
      def stdoutWrite(text: String): Unit = consoleOutput.stdoutWrite(text)
      def debugWriteln(text: String): Unit = debugOutput.debugWriteln(text)
    }
}

/** For command-line tools following the POSIX standard of logging to stderr.
  * @see
  *   https://pubs.opengroup.org/onlinepubs/9699919799/functions/stderr.html
  */
object RealTerminalInputOutput extends InputOutput {
  def readFile(filename: String): IterableOnce[String] =
    Using(Source.fromFile(filename))(source => source.getLines()).get

  def stdin: Iterator[String] = scala.io.Source.stdin.getLines()

  def stdoutWrite(text: String): Unit = System.out.print(text)

  def debugWriteln(text: String): Unit = System.err.println(text)
}
