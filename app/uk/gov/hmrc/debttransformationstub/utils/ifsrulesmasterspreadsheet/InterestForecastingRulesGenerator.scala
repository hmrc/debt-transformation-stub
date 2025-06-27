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
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.InterestForecastingRulesGenerator.ParsedArgs.{ InputSettings, OutputFormat }
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.{ IfsRulesMasterData, InputOutput, InterestForecastingConfigBuilder, RealTerminalInputOutput }

import java.util.Locale

/** This command-line application takes in a master spreadsheet with IFS rules and transforms it. See the unit tests for
  * complete examples of how to use it.
  *
  * See `README.md` for instructions.
  */
object InterestForecastingRulesGenerator {
  def main(args: Array[String]): Unit = {
    implicit val inputOutput: RealTerminalInputOutput.type = RealTerminalInputOutput

    val generator = new InterestForecastingRulesGenerator

    generator.execute(args.toVector)
  }

  final case class ParsedArgs(
    inputSettings: ParsedArgs.InputSettings,
    outputFormat: ParsedArgs.OutputFormat,
    outputLocation: ParsedArgs.OutputLocation
  )
  object ParsedArgs {
    def parse(args: Vector[String]): ParsedArgs =
      args.sorted match {
        case Vector(inputArg @ s"--input-$_", formatArg @ s"--output-format=$_", outputLocArg @ s"--output=$_") =>
          val inputSettings =
            inputArg match {
              case "--input-console-tsv"     => InputSettings.ConsoleTsv(inputTerminator = "END_INPUT")
              case s"--input-file=$filePath" => InputSettings.FromFile(filePath = filePath)
              case wrongArgs => throw new IllegalArgumentException(s"Unknown input args: ${Json.toJson(wrongArgs)}")
            }

          val outputFormat =
            formatArg match {
              case "--output-format=ifs-scala-config"  => OutputFormat.IfsScalaConfig
              case "--output-format=application-conf"  => OutputFormat.ApplicationConf
              case "--output-format=production-config" => OutputFormat.ProductionConfig
              case wrongArgs =>
                throw new IllegalArgumentException(s"Unknown output format args: ${Json.toJson(wrongArgs)}")
            }

          val outputLocation =
            outputLocArg match {
              case "--output=console" => OutputLocation.Console
              case wrongArgs =>
                throw new IllegalArgumentException(s"Unknown output location args: ${Json.toJson(wrongArgs)}")
            }

          ParsedArgs(inputSettings, outputFormat, outputLocation)

        case wrongArgs => throw new IllegalArgumentException(s"Unknown args: ${Json.toJson(wrongArgs)}")
      }

    sealed trait InputSettings
    object InputSettings {
      final case class ConsoleTsv(inputTerminator: String) extends InputSettings
      final case class FromFile(filePath: String) extends InputSettings
    }

    sealed trait OutputFormat
    object OutputFormat {
      case object IfsScalaConfig extends OutputFormat
      case object ApplicationConf extends OutputFormat
      case object ProductionConfig extends OutputFormat
    }

    sealed trait OutputLocation
    object OutputLocation {
      case object Console extends OutputLocation
    }
  }
}

final class InterestForecastingRulesGenerator(implicit io: InputOutput) {

  def execute(args: Vector[String]): Unit = {
    val parsedArgs = ParsedArgs.parse(args)
    executeWithParsedArgs(parsedArgs)
  }

  private def executeWithParsedArgs(args: ParsedArgs): Unit = {
    val ParsedArgs(inputSettings, outputFormat, outputLocation) = args

    val parsedMasterData = readRulesMasterData(inputSettings)

    val result = generateIfsRules(outputFormat, parsedMasterData)

    outputLocation match {
      case ParsedArgs.OutputLocation.Console =>
        io.stdoutWriteln(result.iterator.mkString("\n"): String)
    }
  }

  private def readRulesMasterData(inputSettings: InputSettings): IfsRulesMasterData = {
    val result = inputSettings match {
      case InputSettings.ConsoleTsv(terminatorLine) =>
        io.debugWriteln(s"Paste the TSV and end the input with ${JsString(terminatorLine)} on one line.")
        val tsv: Seq[String] = io.stdin.takeWhile(_ != terminatorLine).toVector
        IfsRulesMasterData.fromMasterSpreadsheetTsv(tsv)

      case InputSettings.FromFile(filePath) =>
        lazy val fileLines: Vector[String] = io.readFile(filePath).iterator.toVector

        io.debugWriteln(s"Reading from CSV/TSV file: ${JsString(filePath)}")

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

    io.debugWriteln(s"Parsed master IFS data: $result")

    result
  }

  private def generateIfsRules(outputFormat: OutputFormat, data: IfsRulesMasterData): Seq[String] =
    outputFormat match {
      case OutputFormat.IfsScalaConfig =>
        InterestForecastingConfigBuilder.OutputGenerators.ifsScalaConfig(data)
      case OutputFormat.ApplicationConf =>
        InterestForecastingConfigBuilder.OutputGenerators.ifsApplicationConf(data)
      case OutputFormat.ProductionConfig =>
        InterestForecastingConfigBuilder.OutputGenerators.ifsProductionConfigOverride(data)
    }
}
