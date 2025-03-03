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
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.InterestForecastingRulesGeneratorSpec.Data
import uk.gov.hmrc.debttransformationstub.utils.ifsrulesmasterspreadsheet.impl.SafeLogger

import scala.util.Using

final class InterestForecastingRulesGeneratorSpec extends AnyFreeSpec {
  ".execute" - {
    implicit val logger: SafeLogger = obj => println(s"LOG: $obj")

    def brokenStandardInput(implicit pos: Position): Iterator[String] =
      Iterator.continually(fail("Test failure: standard input is disabled in this test."))

    def brokenReadFile(filename: String)(implicit pos: Position): IterableOnce[String] =
      fail(s"Test failure: cannot read any files here. Tried: ${Json.toJson(filename)}")

    "for toy examples" - {
      "when asked to read from file" - {
        "rejects unknown file extensions" in {

          val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

          val exception = the[IllegalArgumentException] thrownBy {
            runner.execute(
              args = Vector(s"""--input-file=/my/file/path.zip""", """--output-console-conf"""),
              stdin = brokenStandardInput
            )
          }

          exception.getMessage shouldBe """Unknown extension "zip" of file "/my/file/path.zip""""
        }
      }

      "when asked to read from stdin" - {
        "given empty sections" in {
          val input = Vector(
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End"
          )
          val stdin: Iterator[String] =
            input.iterator ++
              Vector("""END_INPUT""") ++
              Iterator.continually(fail(s"Tried to read line after input terminator."))

          val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

          val result: IterableOnce[String] = runner.execute(
            args = Vector("--input-console-tsv", "--output-console-conf"),
            stdin = stdin
          )

          result.iterator.mkString("\n") shouldBe ""
        }

        "given vanilla sections" - {
          val input = Vector(
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End\tRegime Usage",
            "1520\t1090\tN\tN/A\tN\tN/A\tperiodEnd\tCDCS",
            "1525\t1000\tY\t4\tN\tN/A\tsome description\tPAYE",
            "1045\t1090\tN\t\t\tCharge ref\tperiodEnd\tCDCS",
            "2000\t1000\tY\t\t\tASN\tperiodEnd\tCDCS",
            "4700\t1174\tY\t\t\tVRN\tperiodEnd\tCDCS",
            "4620\t1175\t\t\tY\tCharge ref\tperiodEnd\tVAT",
            "4920\t1553\tY\t\tN\tN/A\tperiodEnd\tCDCS",
            "6010\t1554\tN\t\tY\tN/A\tperiodEnd\tCDCS",
            "4910\t1005\tY\t\tN\tUTR\tperiodEnd\tCDCS",
            "4910\t1007\tY\t\tN\tN/A\tperiodEnd\tCDCS",
            "4910\t1007\tY\t\tN\tN/A\tperiodEnd\tCDCS",
            "4530\t1000\tN\t\tN\tCharge Ref\tperiodEnd\tSIA"
          )

          def newStdin(): Iterator[String] =
            input.iterator ++
              Vector("""END_INPUT""") ++
              Iterator.continually(fail(s"Tried to read line after input terminator."))

          "when asked to output application config" in {
            val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

            val result: IterableOnce[String] = runner.execute(
              args = Vector("--input-console-tsv", "--output-console-conf"),
              stdin = newStdin()
            )

            result.iterator.mkString("\n") shouldBe
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4530' AND subTrans == '1000' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
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
                |""".stripMargin.trim
          }

          "when asked to output production config" in {
            val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

            val result: IterableOnce[String] = runner.execute(
              args = Vector("--input-console-tsv", "--output-console-production-config"),
              stdin = newStdin()
            )

            result.iterator.mkString("\n") shouldBe
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.0: "SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |service-config.rules.1: "SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.2: "SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.3: "SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4530' AND subTrans == '1000' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.4: "SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
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
                |""".stripMargin.trim
          }

        }

        "given sections with duplicates" - {
          val input = Vector(
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End\tRegime Usage",
            "1520\t1090\tN\tN/A\tN\tN/A\tsome description\tCDCS",
            "1520\t1090\tN\tN/A\tN\tN/A\tsome description\tVAT",
            "1520\t1090\tN\tN/A\tN\tN/A\tsome description\tCDCS",
            "1525\t1000\tY\t4\tN\tN/A\tsome description\tCDCS",
            "1045\t1090\tN\t\t\tCharge ref\tsome description\tCDCS",
            "1045\t1090\tN\t\t\tCharge ref\tsome description\tCDCS",
            "2000\t1000\tY\t\t\tASN\tsome description\tVAT",
            "2000\t1000\tY\t\t\tASN\tsome description\tCDCS",
            "4700\t1174\tY\t\t\tVRN\tsome description\tPAYE",
            "4700\t1174\tY\t\t\tVRN\tsome description\tCDCS",
            "4620\t1175\t\t\tY\tCharge ref\tsome description\tCDCS",
            "4920\t1553\tY\t\tN\tN/A\tsome description\tPAYE",
            "6010\t1554\tN\t\tY\tN/A\tsome description\tCDCS",
            "6010\t1554\tN\t\tY\tN/A\tsome description\tPAYE",
            "6010\t1554\tN\t\tY\tN/A\tsome description\tCDCS",
            "4910\t1005\tY\t\tN\tN/A\tsome description\tCDCS",
            "4910\t1005\tY\t\tN\tN/A\tsome description\tVAT",
            "4910\t1007\tY\t\tN\tN/A\tsome description\tCDCS",
            "4530\t1000\tN\t\tN\tCharge Ref\tsome description\tSIA",
            "4530\t1000\tN\t\tN\tCharge Ref\tsome description\tVAT",
            "4530\t1000\tN\t\tN\tChArGe ReF\tsome description\tVAT"
          )

          def newStdin(): Iterator[String] =
            input.iterator ++
              Vector("""END_INPUT""") ++
              Iterator.continually(fail(s"Tried to read line after input terminator."))

          "when asked to output application config duplicates will be removed" in {
            val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

            val result: IterableOnce[String] = runner.execute(
              args = Vector("--input-console-tsv", "--output-console-conf"),
              stdin = newStdin()
            )

            result.iterator.mkString("\n") shouldBe
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4530' AND subTrans == '1000' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4910' AND subTrans == '1005' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '4910' AND subTrans == '1007' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDcnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '4920' AND subTrans == '1553' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICc0OTIwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '6010' AND subTrans == '1554' -> intRate = 0 AND interestOnlyDebt = true,
                |"SUYgbWFpblRyYW5zID09ICc2MDEwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
                |""".stripMargin.trim
          }

          "when asked to output production config  duplicates will be removed" in {
            val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

            val result: IterableOnce[String] = runner.execute(
              args = Vector("--input-console-tsv", "--output-console-production-config"),
              stdin = newStdin()
            )

            result.iterator.mkString("\n") shouldBe
              """# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.0: "SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |service-config.rules.1: "SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.2: "SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.3: "SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4530' AND subTrans == '1000' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.4: "SUYgbWFpblRyYW5zID09ICc0NTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |service-config.rules.5: "SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ=="
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.6: "SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4910' AND subTrans == '1005' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.7: "SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '4910' AND subTrans == '1007' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.8: "SUYgbWFpblRyYW5zID09ICc0OTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDcnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '4920' AND subTrans == '1553' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.9: "SUYgbWFpblRyYW5zID09ICc0OTIwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '6010' AND subTrans == '1554' -> intRate = 0 AND interestOnlyDebt = true,
                |service-config.rules.10: "SUYgbWFpblRyYW5zID09ICc2MDEwJyBBTkQgc3ViVHJhbnMgPT0gJzE1NTQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ=="
                |""".stripMargin.trim
          }

        }

        "given sections with duplicate main and sub trans with different values" - {
          val input = Vector(
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End",
            "1520\t1090\tN\tN/A\tN\tN/A",
            "1520\t1090\tN\tN/A\tN\tN/A",
            "1520\t1090\tN\tN/A\tN\tN/A",
            "1525\t1000\tY\t4\tN\tN/A\tsome description",
            "PAYE",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "1045\t1090\tN\t\t\tCharge ref",
            "1045\t1090\tN\t\t\tCharge ref",
            "2000\t1000\tY\t\t\tASN",
            "2000\t1000\tY\t\t\tASN",
            "VAT",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "4700\t1174\tY\t\t\tVRN",
            "4700\t1174\tY\t\t\tVRN",
            "4620\t1175\t\t\tY\tCharge ref",
            "SA DEBTS - not in production or code yet",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "4920\t1553\tY\t\tN\tN/A",
            "6010\t1554\tN\t\tY\tN/A",
            "6010\t1554\tN\t\tY\tN/A",
            "6010\t1554\tN\t\tY\tN/A",
            "SA SSTTP Debts",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "4910\t1005\tY\t\tN\tN/A",
            "4910\t1005\tY\t\tN\tN/A",
            "4910\t1007\tY\t\tN\tN/A",
            "SIA",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "4530\t1000\tN\t\tN\tCharge Ref",
            "4530\t1000\tN\t\tN\tCharge Ref"
          )

          def newStdin(): Iterator[String] =
            input.iterator ++
              Vector("""END_INPUT""") ++
              Iterator.continually(fail(s"Tried to read line after input terminator."))

          "when asked to output application config duplicates will be removed" in {}

          "when asked to output production config  duplicates will be removed" in {}

        }
      }
    }

    "for the use case on 2024-08-24 for DTD-2418" - {

      "as a TSV from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-august-2024.tsv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2024-08-24--DTD-2418`.tsvInput().split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2024-08-24--DTD-2418`.outputApplicationConf
      }

      "as a clean CSV from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-august-2024.csv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2024-08-24--DTD-2418`.csvInputWithCleanHeadings().split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2024-08-24--DTD-2418`.outputApplicationConf
      }

      "as a messy CSV export (from Excel) from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-august-2024.csv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2024-08-24--DTD-2418`.csvInputWithMessyHeadings().split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2024-08-24--DTD-2418`.outputApplicationConf
      }

      "as a TSV from stdin" in {
        val stdin: Iterator[String] =
          Iterator.from(Data.`Sample--2024-08-24--DTD-2418`.tsvInput().split("\n")) ++
            Iterator.single("END_INPUT") ++
            Iterator.continually(fail(s"Tried to read line after input terminator."))

        val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

        val result = runner.execute(
          args = Vector("--input-console-tsv", "--output-console-conf"),
          stdin = stdin
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2024-08-24--DTD-2418`.outputApplicationConf
      }

    }
  }
}

object InterestForecastingRulesGeneratorSpec {
  private object Data {
    object `Sample--2024-08-24--DTD-2418` {
      def tsvInput(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2024-08-24--DTD-2418/input-with-clean-headings.tsv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def csvInputWithCleanHeadings(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2024-08-24--DTD-2418/input-with-clean-headings.csv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def csvInputWithMessyHeadings(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2024-08-24--DTD-2418/input-with-messy-headings.csv"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }

      def outputApplicationConf(): String = {
        val path =
          "test/resources/InterestForecastingRulesGenerator/samples/2024-08-24--DTD-2418/output-application-conf-array.txt"
        Using(scala.io.Source.fromFile(path))(_.mkString).get
      }
    }
  }
}
