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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.JsString

final class FileInputSpec extends AnyFreeSpec {
  "FileInput" - {
    ".readFileLines" - {
      def mkFileInput(filename: String, content: String): FileInput = {
        case `filename`       => content
        case unrecognisedFile => fail(s"Could not read file: ${JsString(unrecognisedFile)}")
      }

      "when the file is empty" in {
        val fileInput = mkFileInput(filename = "dummy.txt", content = "")
        fileInput.readFileLines("dummy.txt") shouldBe Vector("")
      }

      "when the file has one line" in {
        val fileInput = mkFileInput(filename = "dummy.txt", content = "Hello, World!")
        fileInput.readFileLines("dummy.txt") shouldBe Vector("Hello, World!")
      }

      "when the file has multiple simple lines" - {
        "separated by Unix newlines" in {
          val fileInput = mkFileInput(
            filename = "dummy.txt",
            content = "Line 1\nLine 2\nLine 3"
          )
          fileInput.readFileLines("dummy.txt") shouldBe Vector("Line 1", "Line 2", "Line 3")
        }

        "separated by Windows newlines" in {
          val fileInput = mkFileInput(
            filename = "dummy.txt",
            content = "Line 1\r\nLine 2\r\nLine 3"
          )
          fileInput.readFileLines("dummy.txt") shouldBe Vector("Line 1", "Line 2", "Line 3")
        }

        "mixed newlines" in {
          val fileInput = mkFileInput(
            filename = "dummy.txt",
            content = "Line 1\nLine 2\r\nLine 3"
          )
          fileInput.readFileLines("dummy.txt") shouldBe Vector("Line 1", "Line 2", "Line 3")
        }
      }

      "when the file has multiple empty lines" - {
        "and there are only empty lines" in {
          val fileInput = mkFileInput(
            filename = "dummy.txt",
            content = "\n\n\n"
          )
          fileInput.readFileLines("dummy.txt") shouldBe Vector("", "", "", "")
        }

        "empty lines with some non-empty lines" in {
          val fileInput = mkFileInput(
            filename = "dummy.txt",
            content = "\nLine 1\n\nLine 2\n"
          )
          fileInput.readFileLines("dummy.txt") shouldBe Vector("", "Line 1", "", "Line 2", "")
        }
      }
    }
  }
}
