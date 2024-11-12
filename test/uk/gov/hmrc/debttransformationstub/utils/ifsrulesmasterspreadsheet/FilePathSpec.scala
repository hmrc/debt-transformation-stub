/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import uk.gov.hmrc.debttransformationstub.utils.FilePath
import uk.gov.hmrc.debttransformationstub.utils.FilePath.findAndCreateFilePath

import java.io.File
import scala.List

class FilePathSpec extends AnyFreeSpec {
  "findAndCreateFilePath" - {
    val regimeType: String = "PAYE"
    "should return an empty list when file doesn't exist" in {
      val idValue: String = "nonExistingFile"
      val basePath = "conf/resources/data/etmp.eligibility"
      val directory: File = new File(basePath)
      val maybeFilePath: List[FilePath] = findAndCreateFilePath(directory, s"$basePath.$regimeType", idValue)

      maybeFilePath shouldBe List.empty
    }
    "should return a list with file path name" in {
      val idValue: String = "840PC00002261"
      val basePath = s"conf/resources/data/etmp.eligibility.$regimeType"
      val directory: File = new File(basePath)
      val maybeFilePath: List[FilePath] = findAndCreateFilePath(directory, basePath, idValue)

      maybeFilePath shouldBe List(FilePath("conf/resources/data/etmp.eligibility.PAYE/840PC00002261.json"))
    }

  }
}
