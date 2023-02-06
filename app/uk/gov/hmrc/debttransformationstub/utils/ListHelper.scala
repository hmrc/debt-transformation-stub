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

package uk.gov.hmrc.debttransformationstub.utils

import scala.io.Source

class ListHelper {
  private val fileHelper: FileHelper = new FileHelper()

  def getList(folderPath: String): String = {
    val files = fileHelper.getListOfFiles(folderPath)

    var result = "["
    for (file <- files)
      result = result + Source.fromFile(file).mkString + ","
    result = result.substring(0, result.length() - 1)
    result = result + "]"
    result.toString()
  }
}
