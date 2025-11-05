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

import org.scalactic.source.Position
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.Json
import uk.gov.hmrc.debttransformationstub.testutils.StringAssertionOps._
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.InterestForecastingRulesGeneratorSpec.Data
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.{ ApplicationInput, ConsoleInput, ConsoleOutput, DebugOutput, FileInput, InputOutput }

import java.util.concurrent.atomic.AtomicReference
import scala.util.Using

final class InterestForecastingRulesGeneratorSpec extends AnyFreeSpec {
  ".execute" - {
    object FileInputs {
      def forbidden(implicit pos: Position): FileInput =
        (filename: String) => fail(s"Test does not allow reading files. Tried reading file: ${Json.toJson(filename)}")

      def fileReadOnlyOnce(filename: String, input: String)(implicit pos: Position): FileInput = {
        val wholeInputEachTimeMutable: Iterator[String] =
          Iterator(input) ++
            Iterator.continually(s"Test does not allow reading file more than once: ${Json.toJson(filename)}")

        (file: String) =>
          if (file == filename) wholeInputEachTimeMutable.next()
          else fail(s"Test does not allow reading from file ${Json.toJson(file)}. Expected ${Json.toJson(filename)}.")
      }
    }

    object ConsoleInputs {
      def forbidden(implicit pos: Position): ConsoleInput =
        new ConsoleInput {
          def stdin: Iterator[String] = fail("Test does not allow reading from stdin.")
        }

      def fromExactString(consoleInput: String)(implicit pos: Position): ConsoleInput =
        new ConsoleInput {
          def stdin: Iterator[String] =
            consoleInput.split("\n").iterator ++
              Iterator.continually(fail(s"Tried to read line after input terminator."))
        }
    }

    object ApplicationInputs {
      def consoleFromExactString(stdinContent: String)(implicit pos: Position): ApplicationInput =
        ApplicationInput(FileInputs.forbidden, ConsoleInputs.fromExactString(stdinContent))

      def oneFileAtMostOnce(filename: String, fileContent: String)(implicit pos: Position): ApplicationInput =
        ApplicationInput(FileInputs.fileReadOnlyOnce(filename, fileContent), ConsoleInputs.forbidden)
    }

    object ConsoleOutputs {
      def forbidden(implicit pos: Position): ConsoleOutput =
        (text: String) => fail(s"Test does not allow writing to stdout. Tried writing: ${Json.toJson(text)}")

      def atomicCollector(storage: AtomicReference[String]): ConsoleOutput =
        (text: String) => storage.updateAndGet(_ + text)
    }

    object DebugOutputs {
      def consoleWithCollector(storage: AtomicReference[Seq[String]]): DebugOutput =
        (line: String) => {
          println(s"LOG: $line")
          storage.updateAndGet(_ :+ line)
        }

      def console: DebugOutput = (line: String) => println(s"LOG: $line")
    }

    "for toy examples" - {
      "when asked to read from file" - {
        "rejects unknown file extensions" in {
          val debugStorage = new AtomicReference(Seq.empty[String])

          implicit val io: InputOutput = InputOutput(
            FileInputs.forbidden,
            ConsoleInputs.forbidden,
            ConsoleOutputs.forbidden,
            DebugOutputs.consoleWithCollector(debugStorage)
          )

          val runner = new InterestForecastingRulesGenerator

          val exception = the[IllegalArgumentException] thrownBy {
            runner.execute(
              args = Vector(
                "--input-file=/my/file/path.zip",
                "--output-format=ifs-scala-config",
                "--output=console"
              )
            )
          }

          exception.getMessage shouldBeThatString """Unknown extension "zip" of file "/my/file/path.zip""""

          debugStorage.get() shouldBe List(
            """Reading from CSV/TSV file: "/my/file/path.zip""""
          )
        }
      }

      "when asked to read from stdin" - {
        "for simple inputs (test debugging too)" - {
          "given an empty table" - {
            val input =
              s"""Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End
                 |END_INPUT""".stripMargin

            "when asked to output IFS Scala config" in {
              val debugStorage = new AtomicReference(Seq.empty[String])
              val output = new AtomicReference("")

              implicit val io: InputOutput = InputOutput(
                FileInputs.forbidden,
                ConsoleInputs.fromExactString(input),
                ConsoleOutputs.atomicCollector(output),
                DebugOutputs.consoleWithCollector(debugStorage)
              )

              val runner = new InterestForecastingRulesGenerator

              runner.execute(
                args = Vector(
                  "--input-console-tsv",
                  "--output-format=ifs-scala-config",
                  "--output=console"
                )
              )

              output.get() shouldBeThatString
                """/*
                  | * Copyright 2025 HM Revenue & Customs
                  | *
                  | */
                  |
                  |package uk.gov.hmrc.interestforecasting.rules
                  |
                  |import uk.gov.hmrc.interestforecasting.models.DebtIdentifier
                  |import uk.gov.hmrc.interestforecasting.models.chargeTypes.{ MainTransType, SubTransType }
                  |import uk.gov.hmrc.interestforecasting.rules.model.{ DebtRule, DebtRuleProperties, DebtRules, InterestRateRef }
                  |
                  |/** This entire file is auto-generated by `InterestForecastingRulesGenerator` in the `debt-transformation-stub`. */
                  |object BusinessRulesConfiguration {
                  |  // This is the business config.
                  |  // Declared as a val so that if it throws, it has every opportunity to be noticed.
                  |  val businessProvidedRules: DebtRules = DebtRules.createOrThrow(
                  |    distinctRules = Vector(
                  |      // No rules were provided.
                  |    )
                  |  )
                  |
                  |}
                  |""".stripMargin

              debugStorage.get() shouldBe List(
                """Paste the TSV and end the input with "END_INPUT" on one line.""",
                """Parsed master IFS data: {
                  |  "tableData" : {
                  |    "headings" : [ "Main Trans", "Sub Trans", "Interest bearing", "Interest key", "Interest only Debt", "Charge Ref", "Period End" ],
                  |    "rows" : [ ]
                  |  }
                  |}""".stripMargin
              )
            }

          }

          "given a very short table" - {
            val input =
              s"""Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End\tRegime Usage
                 |1520\t1090\tN\tN/A\tN\tN/A\tperiodEnd\tCDCS
                 |4530\t3190\tN\t\tN\tCharge Ref\tperiodEnd\tSIA
                 |END_INPUT""".stripMargin

            "when asked to output IFS Scala config" in {
              val debugStorage = new AtomicReference(Seq.empty[String])
              val output = new AtomicReference("")

              implicit val io: InputOutput = InputOutput(
                FileInputs.forbidden,
                ConsoleInputs.fromExactString(input),
                ConsoleOutputs.atomicCollector(output),
                DebugOutputs.consoleWithCollector(debugStorage)
              )

              val runner = new InterestForecastingRulesGenerator

              runner.execute(
                args = Vector(
                  "--input-console-tsv",
                  "--output-format=ifs-scala-config",
                  "--output=console"
                )
              )

              output.get() shouldBeThatString
                """/*
                  | * Copyright 2025 HM Revenue & Customs
                  | *
                  | */
                  |
                  |package uk.gov.hmrc.interestforecasting.rules
                  |
                  |import uk.gov.hmrc.interestforecasting.models.DebtIdentifier
                  |import uk.gov.hmrc.interestforecasting.models.chargeTypes.{ MainTransType, SubTransType }
                  |import uk.gov.hmrc.interestforecasting.rules.model.{ DebtRule, DebtRuleProperties, DebtRules, InterestRateRef }
                  |
                  |/** This entire file is auto-generated by `InterestForecastingRulesGenerator` in the `debt-transformation-stub`. */
                  |object BusinessRulesConfiguration {
                  |  // This is the business config.
                  |  // Declared as a val so that if it throws, it has every opportunity to be noticed.
                  |  val businessProvidedRules: DebtRules = DebtRules.createOrThrow(
                  |    distinctRules = Vector(
                  |      DebtRule(
                  |        DebtIdentifier(MainTransType("1520"), SubTransType("1090")),
                  |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = false)
                  |      ),
                  |      DebtRule(
                  |        DebtIdentifier(MainTransType("4530"), SubTransType("3190")),
                  |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = false, useChargeReference = true)
                  |      )
                  |    )
                  |  )
                  |
                  |}
                  |""".stripMargin

              debugStorage.get() shouldBe List(
                """Paste the TSV and end the input with "END_INPUT" on one line.""",
                """Parsed master IFS data: {
                  |  "tableData" : {
                  |    "headings" : [ "Main Trans", "Sub Trans", "Interest bearing", "Interest key", "Interest only Debt", "Charge Ref", "Period End", "Regime Usage" ],
                  |    "rows" : [ [ "1520", "1090", "N", "N/A", "N", "N/A", "periodEnd", "CDCS" ], [ "4530", "3190", "N", "", "N", "Charge Ref", "periodEnd", "SIA" ] ]
                  |  }
                  |}""".stripMargin
              )
            }
          }
        }

        "given vanilla inputs that aren't so simple" - {
          val input =
            s"""Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End\tRegime Usage
               |1520\t1090\tN\tN/A\tN\tN/A\tperiodEnd\tCDCS
               |1525\t1000\tY\t4\tN\tN/A\tsome description\tPAYE
               |1045\t1090\tN\t\t\tCharge ref\tperiodEnd\tCDCS
               |2000\t1000\tY\t\t\tASN\tperiodEnd\tCDCS
               |4700\t1174\tY\t\t\tVRN\tperiodEnd\tCDCS
               |4620\t1175\t\t\tY\tCharge ref\tperiodEnd\tVAT
               |4920\t1553\tY\t\tN\tN/A\tperiodEnd\tCDCS
               |6010\t1554\tN\t\tY\tN/A\tperiodEnd\tCDCS
               |4910\t1005\tY\t\tN\tUTR\tperiodEnd\tCDCS
               |4910\t1007\tY\t\tN\tN/A\tperiodEnd\tCDCS
               |4530\t3190\tN\t\tN\tCharge Ref\tperiodEnd\tSIA
               |END_INPUT""".stripMargin

          "when asked to output IFS Scala config" in {
            val output = new AtomicReference("")

            implicit val io: InputOutput = InputOutput(
              FileInputs.forbidden,
              ConsoleInputs.fromExactString(input),
              ConsoleOutputs.atomicCollector(output),
              DebugOutputs.console
            )

            val runner = new InterestForecastingRulesGenerator

            runner.execute(
              args = Vector(
                "--input-console-tsv",
                "--output-format=ifs-scala-config",
                "--output=console"
              )
            )

            output.get() shouldBeThatString
              """/*
                | * Copyright 2025 HM Revenue & Customs
                | *
                | */
                |
                |package uk.gov.hmrc.interestforecasting.rules
                |
                |import uk.gov.hmrc.interestforecasting.models.DebtIdentifier
                |import uk.gov.hmrc.interestforecasting.models.chargeTypes.{ MainTransType, SubTransType }
                |import uk.gov.hmrc.interestforecasting.rules.model.{ DebtRule, DebtRuleProperties, DebtRules, InterestRateRef }
                |
                |/** This entire file is auto-generated by `InterestForecastingRulesGenerator` in the `debt-transformation-stub`. */
                |object BusinessRulesConfiguration {
                |  // This is the business config.
                |  // Declared as a val so that if it throws, it has every opportunity to be noticed.
                |  val businessProvidedRules: DebtRules = DebtRules.createOrThrow(
                |    distinctRules = Vector(
                |      DebtRule(
                |        DebtIdentifier(MainTransType("1045"), SubTransType("1090")),
                |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = false, useChargeReference = true)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("1520"), SubTransType("1090")),
                |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("1525"), SubTransType("1000")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("2000"), SubTransType("1000")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false, useChargeReference = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4530"), SubTransType("3190")),
                |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = false, useChargeReference = true)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4620"), SubTransType("1175")),
                |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = true, useChargeReference = true)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4700"), SubTransType("1174")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false, useChargeReference = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4910"), SubTransType("1005")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false, useChargeReference = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4910"), SubTransType("1007")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("4920"), SubTransType("1553")),
                |        DebtRuleProperties(InterestRateRef.standard, interestOnlyDebt = false)
                |      ),
                |      DebtRule(
                |        DebtIdentifier(MainTransType("6010"), SubTransType("1554")),
                |        DebtRuleProperties(InterestRateRef.zero, interestOnlyDebt = true)
                |      )
                |    )
                |  )
                |
                |}
                |""".stripMargin

          }

          "when asked to output IFS application.conf" in {
            val output = new AtomicReference("")

            implicit val io: InputOutput = InputOutput(
              FileInputs.forbidden,
              ConsoleInputs.fromExactString(input),
              ConsoleOutputs.atomicCollector(output),
              DebugOutputs.console
            )

            val runner = new InterestForecastingRulesGenerator

            runner.execute(
              args = Vector(
                "--input-console-tsv",
                "--output-format=application-conf",
                "--output=console"
              )
            )

            output.get() shouldBeThatString
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4530' AND subTrans == '3190' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzMxOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4910' AND subTrans == '1005' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4910' AND subTrans == '1007' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDcnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '4920' AND subTrans == '1553' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTIwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '6010' AND subTrans == '1554' -> intRate = 0 AND interestOnlyDebt = true,
                |"SUYgbWFpblRyYW5zID09ICc2MDEwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
                |""".stripMargin

          }

          "when asked to output IFS production override config" in {
            val output = new AtomicReference("")

            implicit val io: InputOutput = InputOutput(
              FileInputs.forbidden,
              ConsoleInputs.fromExactString(input),
              ConsoleOutputs.atomicCollector(output),
              DebugOutputs.console
            )

            val runner = new InterestForecastingRulesGenerator

            runner.execute(
              args = Vector(
                "--input-console-tsv",
                "--output-format=production-config",
                "--output=console"
              )
            )

            output.get() shouldBeThatString
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.0: "SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |service-config.rules.1: "SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.2: "SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.3: "SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4530' AND subTrans == '3190' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.4: "SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzMxOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |service-config.rules.5: "SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ=="
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.6: "SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4910' AND subTrans == '1005' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.7: "SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4910' AND subTrans == '1007' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.8: "SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDcnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '4920' AND subTrans == '1553' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.9: "SUYgbWFpblRyYW5zID09ICc0OTIwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '6010' AND subTrans == '1554' -> intRate = 0 AND interestOnlyDebt = true,
                |service-config.rules.10: "SUYgbWFpblRyYW5zID09ICc2MDEwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ=="
                |""".stripMargin

          }

        }

        "given sections with duplicate main and sub trans with different values" - {
          val consoleInput =
            s"""Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End\tRegime Usage
               |1520\t1090\tN\tN/A\tN\tN/A\tsome description\tCDCS
               |1520\t1090\tN\tN/A\tN\tN/A\tsome description\tVAT
               |1525\t1000\tY\t4\tN\tN/A\tsome description\tCDCS
               |END_INPUT""".stripMargin

          "when asked to output Scala IFS config" in {
            implicit val io: InputOutput = InputOutput(
              FileInputs.forbidden,
              ConsoleInputs.fromExactString(consoleInput),
              ConsoleOutputs.forbidden,
              DebugOutputs.console
            )

            val runner = new InterestForecastingRulesGenerator

            val exception = the[IllegalArgumentException] thrownBy {
              runner.execute(
                args = Vector(
                  "--input-console-tsv",
                  "--output-format=ifs-scala-config",
                  "--output=console"
                )
              )
            }

            exception.getMessage shouldBeThatString """Duplicate mainTrans/subTrans combinations are not allowed."""
          }

        }
      }
    }

    "for the use case on 2025-11-05 for DTD-3398" - {
      final case class InputCase(name: String, inputArg: String, appInput: () => ApplicationInput)
      final case class OutputFormatCase(name: String, formatArg: String, expectedOutput: () => String)
      final case class TestCase(inputCase: InputCase, outputFormat: OutputFormatCase)(implicit val pos: Position)

      val inputCaseFromTsvFile = InputCase(
        name = "from a TSV file",
        inputArg = "--input-file=/some/file/path/master-ifs-data-august-2024.tsv",
        () =>
          ApplicationInputs.oneFileAtMostOnce(
            filename = "/some/file/path/master-ifs-data-august-2024.tsv",
            fileContent = Data.`Sample--2025-11-05--DTD-3398`.tsvInput()
          )
      )
      val inputCaseFromCleanCsvFile = InputCase(
        name = "from a clean CSV file",
        inputArg = "--input-file=/some/file/path/master-ifs-data-august-2024.csv",
        () =>
          ApplicationInputs.oneFileAtMostOnce(
            filename = "/some/file/path/master-ifs-data-august-2024.csv",
            fileContent = Data.`Sample--2025-11-05--DTD-3398`.csvInputWithCleanHeadings()
          )
      )
      val inputCaseFromMessyCsvFile = InputCase(
        name = "from a messy Excel CSV export file",
        inputArg = "--input-file=/some/file/path/master-ifs-data-august-2024.csv",
        () =>
          ApplicationInputs.oneFileAtMostOnce(
            filename = "/some/file/path/master-ifs-data-august-2024.csv",
            fileContent = Data.`Sample--2025-11-05--DTD-3398`.csvInputWithMessyHeadings()
          )
      )
      val inputCaseFromTsvConsole = InputCase(
        name = "from a stdin TSV",
        inputArg = "--input-console-tsv",
        () =>
          ApplicationInputs.consoleFromExactString(
            stdinContent = Data.`Sample--2025-11-05--DTD-3398`.tsvInput() + "\nEND_INPUT"
          )
      )

      val outputCaseAsIfsScala = OutputFormatCase(
        name = "outputted as an IFS Scala config",
        formatArg = "--output-format=ifs-scala-config",
        expectedOutput = () => Data.`Sample--2025-11-05--DTD-3398`.outputScalaIfsConf()
      )
      val outputCaseAsApplicationConf = OutputFormatCase(
        name = "outputted as an application.conf",
        formatArg = "--output-format=application-conf",
        expectedOutput = () => Data.`Sample--2025-11-05--DTD-3398`.outputApplicationConf()
      )

      val testCases: List[TestCase] = List(
        TestCase(inputCaseFromTsvFile, outputCaseAsIfsScala),
        TestCase(inputCaseFromCleanCsvFile, outputCaseAsIfsScala),
        TestCase(inputCaseFromMessyCsvFile, outputCaseAsIfsScala),
        TestCase(inputCaseFromTsvConsole, outputCaseAsIfsScala),
        TestCase(inputCaseFromTsvFile, outputCaseAsApplicationConf),
        TestCase(inputCaseFromCleanCsvFile, outputCaseAsApplicationConf),
        TestCase(inputCaseFromMessyCsvFile, outputCaseAsApplicationConf),
        TestCase(inputCaseFromTsvConsole, outputCaseAsApplicationConf)
      )

      for (testCase <- testCases) {
        import testCase.pos
        val TestCase(inputCase, outputFormatCase) = testCase

        s"""for input case <${inputCase.name}> and output format case <${outputFormatCase.name}>""" in {
          val expectedOutput: String = withClue("Misconfigured expected output.\n\n") {
            outputFormatCase.expectedOutput()
          }

          val output = new AtomicReference("")
          val applicationInput: ApplicationInput = inputCase.appInput()

          implicit val io: InputOutput = InputOutput(
            applicationInput,
            applicationInput,
            ConsoleOutputs.atomicCollector(output),
            DebugOutputs.console
          )

          val runner = new InterestForecastingRulesGenerator

          runner.execute(
            args = Vector(
              inputCase.inputArg,
              outputFormatCase.formatArg,
              "--output=console"
            )
          )

          output.get() shouldBeThatString expectedOutput
        }
      }

    }
  }
}

object InterestForecastingRulesGeneratorSpec {
  private object Data {
    object `Sample--2025-11-05--DTD-3398` {
      def tsvInput(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2025-11-05--DTD-3398/input-with-clean-headings.tsv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def csvInputWithCleanHeadings(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2025-11-05--DTD-3398/input-with-clean-headings.csv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def csvInputWithMessyHeadings(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2025-11-05--DTD-3398/input-with-messy-headings.csv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def outputApplicationConf(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2025-11-05--DTD-3398/output-application-conf-array.txt"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def outputScalaIfsConf(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2025-11-05--DTD-3398/output-ifs-scala-config.txt"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }
    }
  }
}
