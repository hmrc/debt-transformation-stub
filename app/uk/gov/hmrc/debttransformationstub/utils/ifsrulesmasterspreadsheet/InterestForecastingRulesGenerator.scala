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

package uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet

import play.api.libs.json.{ JsString, Json }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.InterestForecastingRulesGenerator.ParsedArgs
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.InterestForecastingRulesGenerator.ParsedArgs.{ InputSettings, OutputSettings }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.{ IfsRulesMasterData, InterestForecastingConfigBuilder, SafeLogger }

import java.util.Locale
import scala.io.Source
import scala.util.Using

/** This command-line application takes in a master spreadsheet with IFS rules and transforms it. See the unit tests for
  * complete examples of how to use it.
  *
  * See `README.md` for instructions.
  */
object InterestForecastingRulesGenerator {
  def main(args: Array[String]): Unit = {
    implicit val logger: SafeLogger = SafeLogger.stderr

    val generator =
      new InterestForecastingRulesGenerator(
        readFile = (filename: String) => Using.resource(Source.fromFile(name = filename))(_.getLines().toVector)
      )

    val result = generator.execute(args.toVector, stdin = Source.stdin.getLines())
    System.out.println(result.iterator.mkString("\n"): String)
  }

  final case class ParsedArgs(inputSettings: ParsedArgs.InputSettings, outputSettings: ParsedArgs.OutputSettings)
  object ParsedArgs {
    def parse(args: Vector[String]): ParsedArgs =
      args.sorted match {
        case Vector(inputArgs @ s"--input-$_", outputArgs @ s"--output-$_") =>
          val inputSettings =
            inputArgs match {
              case "--input-console-tsv"     => InputSettings.ConsoleTsv(inputTerminator = "END_INPUT")
              case s"--input-file=$filePath" => InputSettings.FromFile(filePath = filePath)
              case wrongArgs => throw new IllegalArgumentException(s"Unknown input args: ${Json.toJson(wrongArgs)}")
            }

          val outputSettings =
            outputArgs match {
              case "--output-console-conf"              => OutputSettings.ConsoleApplicationConf
              case "--output-console-production-config" => OutputSettings.ConsoleProductionConfig
              case wrongArgs => throw new IllegalArgumentException(s"Unknown output args: ${Json.toJson(wrongArgs)}")
            }

          ParsedArgs(inputSettings, outputSettings)

        case wrongArgs => throw new IllegalArgumentException(s"Unknown args: ${Json.toJson(wrongArgs)}")
      }

    sealed trait InputSettings
    object InputSettings {
      final case class ConsoleTsv(inputTerminator: String) extends InputSettings
      final case class FromFile(filePath: String) extends InputSettings
    }

    sealed trait OutputSettings
    object OutputSettings {
      case object ConsoleApplicationConf extends OutputSettings
      case object ConsoleProductionConfig extends OutputSettings
    }
  }
}

final class InterestForecastingRulesGenerator(readFile: String => IterableOnce[String])(implicit logger: SafeLogger) {

  def execute(args: Vector[String], stdin: Iterator[String]): IterableOnce[String] = {
    val parsedArgs = ParsedArgs.parse(args)
    executeWithParsedArgs(parsedArgs, stdin)
  }

  private def executeWithParsedArgs(args: ParsedArgs, stdin: Iterator[String]): IterableOnce[String] = {
    val ParsedArgs(inputSettings, outputSettings) = args

    val parsedMasterData = readRulesMasterData(inputSettings, stdin)

    generateIfsRules(outputSettings, parsedMasterData)
  }

  private def readRulesMasterData(inputSettings: InputSettings, stdin: Iterator[String]): IfsRulesMasterData = {
    val result = inputSettings match {
      case InputSettings.ConsoleTsv(terminatorLine) =>
        logger.log(s"Paste the TSV and end the input with ${JsString(terminatorLine)} on one line.")
        val tsv: Seq[String] = stdin.takeWhile(_ != terminatorLine).toVector
        IfsRulesMasterData.fromMasterSpreadsheetTsv(tsv)

      case InputSettings.FromFile(filePath) =>
        lazy val fileLines: Vector[String] = readFile(filePath).iterator.toVector

        logger.log(s"Reading from CSV/TSV file: ${JsString(filePath)}")

        val filename = filePath.split("/").last
        val fileExtension = filename.split("\\.").drop(1).last
        fileExtension.toLowerCase(Locale.UK) match {
          case "csv" =>
            IfsRulesMasterData.fromMasterSpreadsheetCsv(csv = fileLines)
          case "tsv" =>
            IfsRulesMasterData.fromMasterSpreadsheetTsv(tsv = fileLines)
          case unknownExtension =>
            throw new IllegalArgumentException(
              s"Unknown extension ${JsString(unknownExtension)} of file ${JsString(filePath)}"
            )
        }
    }

    logger.log(s"Parsed master IFS data: $result")

    result
  }

  private def generateIfsRules(outputSettings: OutputSettings, data: IfsRulesMasterData): Seq[String] =
    outputSettings match {
      case OutputSettings.ConsoleApplicationConf =>
        InterestForecastingConfigBuilder.buildAppConfig(data)
      case OutputSettings.ConsoleProductionConfig =>
        InterestForecastingConfigBuilder.buildProductionConfig(data)
    }
}
