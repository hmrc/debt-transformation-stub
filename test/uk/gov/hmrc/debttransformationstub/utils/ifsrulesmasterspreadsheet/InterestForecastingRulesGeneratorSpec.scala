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
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End",
            "PAYE",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "VAT",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref"
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
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref\tPeriod End",
            "1520\t1090\tN\tN/A\tN\tN/A",
            "1525\t1000\tY\t4\tN\tN/A\tsome description",
            "PAYE",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "1045\t1090\tN\t\t\tCharge ref",
            "2000\t1000\tY\t\t\tASN",
            "VAT",
            "Main Trans\tSub Trans\tInterest bearing\tInterest key\tInterest only Debt\tCharge Ref",
            "4700\t1174\tY\t\t\tVRN",
            "4620\t1175\t\t\tY\tCharge ref"
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
              """"SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
                |# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |"SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |"SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |""".stripMargin.trim
          }

          "when asked to output production config" in {
            val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

            val result: IterableOnce[String] = runner.execute(
              args = Vector("--input-console-tsv", "--output-console-production-config"),
              stdin = newStdin()
            )

            result.iterator.mkString("\n") shouldBe
              """service-config.rules.0: "SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU="
                |# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
                |service-config.rules.1: "SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
                |service-config.rules.2: "SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U="
                |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
                |service-config.rules.3: "SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |service-config.rules.4: "SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ=="
                |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
                |service-config.rules.5: "SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl"
                |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
                |""".stripMargin.trim
          }

        }
      }
    }

    "for the use case on 2023-11-24 for DTD-2025" - {

      "as a TSV from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-november-2023.tsv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2023-11-24--DTD-2025`.tsvInput.split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2023-11-24--DTD-2025`.outputApplicationConf
      }

      "as a clean CSV from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-november-2023.csv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2023-11-24--DTD-2025`.csvInputWithCleanHeadings.split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2023-11-24--DTD-2025`.outputApplicationConf
      }

      "as a messy CSV export (from Excel) from a file" in {
        val exampleFilename = "/some/file/path/master-ifs-data-november-2023.csv"

        def readFile(filename: String): IterableOnce[String] =
          filename match {
            case `exampleFilename` => Data.`Sample--2023-11-24--DTD-2025`.csvInputWithMessyHeadings.split("\n")
            case unknownFile       => fail(s"Attempted to read file that hasn't been prepared: $unknownFile")
          }

        val runner = new InterestForecastingRulesGenerator(readFile = readFile)

        val result = runner.execute(
          args = Vector(s"""--input-file=$exampleFilename""", """--output-console-conf"""),
          stdin = brokenStandardInput
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2023-11-24--DTD-2025`.outputApplicationConf
      }

      "as a TSV from stdin" in {
        val stdin: Iterator[String] = {
          Iterator.from(Data.`Sample--2023-11-24--DTD-2025`.tsvInput.split("\n")) ++
            Iterator.single("END_INPUT") ++
            Iterator.continually(fail(s"Tried to read line after input terminator."))
        }

        val runner = new InterestForecastingRulesGenerator(readFile = brokenReadFile)

        val result = runner.execute(
          args = Vector("--input-console-tsv", "--output-console-conf"),
          stdin = stdin
        )

        result.iterator.mkString("\n") shouldBe Data.`Sample--2023-11-24--DTD-2025`.outputApplicationConf
      }

    }
  }
}

object InterestForecastingRulesGeneratorSpec {
  private object Data {
    object `Sample--2023-11-24--DTD-2025` {
      val tsvInput: String =
        """Main Trans	Sub Trans	Interest bearing	Interest key	Interest only Debt	Charge Ref	Period End
          |5330	7006	N	N/A	N	N/A
          |5330	7010	N	N/A	N	N/A
          |5330	7011	N	N/A	N	N/A
          |5350	7012	N	N/A	N	N/A
          |5350	7014	N	N/A	N	N/A
          |5350	7013	N	N/A	N	N/A
          |1085	1000	N	N/A	N	N/A
          |1085	1020	N	N/A	N	N/A
          |1085	1025	N	N/A	N	N/A
          |1085	1180	N	N/A	N	N/A
          |1511	2000	N	N/A	Y	N/A
          |1515	1090	N	N/A	N	N/A
          |1520	1090	N	N/A	N	N/A
          |1525	1000	Y	4	N	N/A	charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1526	2000	N	N/A	Y	N/A
          |1530	1000	Y	4	N	N/A	Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1531	2000	N	N/A	Y	N/A
          |1535	1000	Y	4	N	N/A	Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1536	2000	N	N/A	Y	N/A
          |1540	1000	Y	4	N	N/A	charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1541	2000	N	N/A	Y	N/A
          |1545	1000	Y	4	N	N/A	Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545	1090	Y	4	N	N/A	Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545	2000	Y	4	N	N/A
          |1546	2000	N	N/A	Y	N/A
          |2421	1150	N	N/A	N	N/A
          |1441	1150	N	N/A	N	N/A
          |4618	1090	N	N/A	N	N/A
          |3996	1091	Y	4	N	N/A
          |3997	2091	N	N/A	Y	N/A
          |
          |PAYE
          |Main Trans	Sub Trans	Interest bearing	Interest key	Interest only Debt	Charge Ref
          |1025	1090	N			Charge ref
          |1030	1090	N			Charge ref
          |1035	1090	N			Charge ref
          |1040	1090	N			Charge ref
          |1045	1090	N			Charge ref
          |2000	1000	Y			ASN
          |2000	1020	Y			ASN
          |2000	1023	Y			ASN
          |2000	1026	Y			ASN
          |2000	1030	Y			ASN
          |2000	1100	Y			ASN
          |2005	2000	N		Y	Charge ref
          |2005	2020	N		Y	Charge ref
          |2005	2023	N		Y	Charge ref
          |2005	2026	N		Y	Charge ref
          |2005	2030	N		Y	Charge ref
          |2005	2100	N		Y	Charge ref
          |2006	1106	Y			ASN
          |2007	1107	N		Y	Charge ref
          |2030	1250	Y			ASN
          |2030	1260	Y			ASN
          |2030	1270	Y			ASN
          |2030	1280	Y			ASN
          |2030	1290	Y			ASN
          |2030	1300	Y			ASN
          |2030	1310	Y			ASN
          |2030	1320	Y			ASN
          |2030	1330	Y			ASN
          |2030	1340	Y			ASN
          |2030	1350	Y			ASN
          |2030	1390	Y			ASN
          |2030	1395	Y			ASN
          |2040	1000	Y			Charge ref
          |2045	2000	N		Y	Charge ref
          |2045	2100	N		Y	Charge ref
          |2060	1020	Y			Charge ref
          |2065	2020	N		Y	Charge ref
          |2090	1000	Y			ASN
          |2090	1020	Y			ASN
          |2090	1023	Y			ASN
          |2090	1026	Y			ASN
          |2090	1100	Y			ASN
          |2090	1250	Y			ASN
          |2090	1260	Y			ASN
          |2090	1270	Y			ASN
          |2090	1280	Y			ASN
          |2090	1290	Y			ASN
          |2090	1300	Y			ASN
          |2090	1310	Y			ASN
          |2090	1320	Y			ASN
          |2090	1330	Y			ASN
          |2090	1340	Y			ASN
          |2090	1350	Y			ASN
          |2095	2000	N		Y	Charge ref
          |2095	2020	N		Y	Charge ref
          |2095	2023	N		Y	Charge ref
          |2095	2026	N		Y	Charge ref
          |2095	2100	N		Y	Charge ref
          |2100	1000	Y			Charge ref
          |2100	1023	Y			Charge ref
          |2100	1026	Y			Charge ref
          |2100	1030	Y			Charge ref
          |2100	1100	Y			Charge ref
          |2105	2000	N		Y	Charge ref
          |2105	2023	N		Y	Charge ref
          |2105	2026	N		Y	Charge ref
          |2105	2030	N		Y	Charge ref
          |2105	2100	N		Y	Charge ref
          |2110	1090	N			Charge ref
          |2115	1090	N			Charge ref
          |2120	1090	N			Charge ref
          |2125	1090	N			Charge ref
          |2130	1355	Y			ASN
          |2135	2355	N		Y	Charge ref
          |2500	1090	Y			Charge ref
          |2505	2090	N		Y	Charge ref
          |2510	1090	Y			Charge ref
          |2515	2090	N		Y	Charge ref
          |2520	1090	Y			Charge ref
          |2525	2090	N		Y	Charge ref
          |2530	1090	Y			Charge ref
          |2535	2090	N		Y	Charge ref
          |2540	1090	Y			Charge ref
          |2545	2090	N		Y	Charge ref
          |2550	1090	Y			Charge ref
          |2555	2090	N		Y	Charge ref
          |2560	1090	Y			Charge ref
          |2565	2090	N		Y	Charge ref
          |2570	1090	Y			Charge ref
          |2575	2090	N		Y	Charge ref
          |2580	1090	Y			Charge ref
          |2585	2090	N		Y	Charge ref
          |2590	1090	Y			Charge ref
          |2595	2090	N		Y	Charge ref
          |
          |VAT
          |Main Trans	Sub Trans	Interest bearing	Interest key	Interest only Debt	Charge Ref
          |4700	1174	Y			VRN
          |4620	1175			Y	VRN
          |4703	1090	Y			VRN
          |4622	1175			Y	VRN
          |4704	1090	Y			VRN
          |4624	1175			Y	VRN
          |7700	1174				VRN
          |4748	1090	Y			VRN
          |4749	1175			Y	VRN
          |4735	1090	Y			VRN
          |4682	1175			Y	VRN
          |7735	1090				VRN
          |4760	1090	Y			VRN
          |4693	1175			Y	VRN
          |7760	1090				VRN
          |4763	1090				VRN
          |4766	1090	Y			VRN
          |4767	1175			Y	VRN
          |7766	1090				VRN
          |4745	1090	Y			VRN
          |4684	1175			Y	VRN
          |7745	1090				VRN
          |4770	1090	Y			VRN
          |4771	1175			Y	VRN
          |4773	1090	Y			VRN
          |4774	1175			Y	VRN
          |4776	1090	Y			VRN
          |4777	1175			Y	VRN
          |7776	1090				VRN
          |4755	1090	Y			VRN
          |4687	1175			Y	VRN
          |7755	1090				VRN
          |4783	1090	Y			VRN
          |4784	1175			Y	VRN
          |7783	1090				VRN
          |4786	1090				VRN
          |7786	1090				VRN
          |4765	1090	Y			VRN
          |4695	1175			Y	VRN
          |7765	1090				VRN
          |4775	1090	Y			VRN
          |4697	1175			Y	VRN
          |7775	1090				VRN
          |4790	1090	Y			VRN
          |4791	1175			Y	VRN
          |4793	1090	Y			VRN
          |4794	1175			Y	VRN
          |4796	1090				VRN
          |7796	1090				VRN
          |4799	1090				VRN
          |7799	1090				VRN
          |4747	1090				VRN
          |7747	1090				VRN
          |4711	1174				VRN""".stripMargin

      val csvInputWithCleanHeadings: String =
        """Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref,Period End
          |5330,7006,N,N/A,N,N/A
          |5330,7010,N,N/A,N,N/A
          |5330,7011,N,N/A,N,N/A
          |5350,7012,N,N/A,N,N/A
          |5350,7014,N,N/A,N,N/A
          |5350,7013,N,N/A,N,N/A
          |1085,1000,N,N/A,N,N/A
          |1085,1020,N,N/A,N,N/A
          |1085,1025,N,N/A,N,N/A
          |1085,1180,N,N/A,N,N/A
          |1511,2000,N,N/A,Y,N/A
          |1515,1090,N,N/A,N,N/A
          |1520,1090,N,N/A,N,N/A
          |1525,1000,Y,4,N,N/A,charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1526,2000,N,N/A,Y,N/A
          |1530,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1531,2000,N,N/A,Y,N/A
          |1535,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1536,2000,N,N/A,Y,N/A
          |1540,1000,Y,4,N,N/A,charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1541,2000,N,N/A,Y,N/A
          |1545,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545,1090,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545,2000,Y,4,N,N/A
          |1546,2000,N,N/A,Y,N/A
          |2421,1150,N,N/A,N,N/A
          |1441,1150,N,N/A,N,N/A
          |4618,1090,N,N/A,N,N/A
          |3996,1091,Y,4,N,N/A
          |3997,2091,N,N/A,Y,N/A
          |
          |PAYE
          |Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref
          |1025,1090,N,,,Charge ref
          |1030,1090,N,,,Charge ref
          |1035,1090,N,,,Charge ref
          |1040,1090,N,,,Charge ref
          |1045,1090,N,,,Charge ref
          |2000,1000,Y,,,ASN
          |2000,1020,Y,,,ASN
          |2000,1023,Y,,,ASN
          |2000,1026,Y,,,ASN
          |2000,1030,Y,,,ASN
          |2000,1100,Y,,,ASN
          |2005,2000,N,,Y,Charge ref
          |2005,2020,N,,Y,Charge ref
          |2005,2023,N,,Y,Charge ref
          |2005,2026,N,,Y,Charge ref
          |2005,2030,N,,Y,Charge ref
          |2005,2100,N,,Y,Charge ref
          |2006,1106,Y,,,ASN
          |2007,1107,N,,Y,Charge ref
          |2030,1250,Y,,,ASN
          |2030,1260,Y,,,ASN
          |2030,1270,Y,,,ASN
          |2030,1280,Y,,,ASN
          |2030,1290,Y,,,ASN
          |2030,1300,Y,,,ASN
          |2030,1310,Y,,,ASN
          |2030,1320,Y,,,ASN
          |2030,1330,Y,,,ASN
          |2030,1340,Y,,,ASN
          |2030,1350,Y,,,ASN
          |2030,1390,Y,,,ASN
          |2030,1395,Y,,,ASN
          |2040,1000,Y,,,Charge ref
          |2045,2000,N,,Y,Charge ref
          |2045,2100,N,,Y,Charge ref
          |2060,1020,Y,,,Charge ref
          |2065,2020,N,,Y,Charge ref
          |2090,1000,Y,,,ASN
          |2090,1020,Y,,,ASN
          |2090,1023,Y,,,ASN
          |2090,1026,Y,,,ASN
          |2090,1100,Y,,,ASN
          |2090,1250,Y,,,ASN
          |2090,1260,Y,,,ASN
          |2090,1270,Y,,,ASN
          |2090,1280,Y,,,ASN
          |2090,1290,Y,,,ASN
          |2090,1300,Y,,,ASN
          |2090,1310,Y,,,ASN
          |2090,1320,Y,,,ASN
          |2090,1330,Y,,,ASN
          |2090,1340,Y,,,ASN
          |2090,1350,Y,,,ASN
          |2095,2000,N,,Y,Charge ref
          |2095,2020,N,,Y,Charge ref
          |2095,2023,N,,Y,Charge ref
          |2095,2026,N,,Y,Charge ref
          |2095,2100,N,,Y,Charge ref
          |2100,1000,Y,,,Charge ref
          |2100,1023,Y,,,Charge ref
          |2100,1026,Y,,,Charge ref
          |2100,1030,Y,,,Charge ref
          |2100,1100,Y,,,Charge ref
          |2105,2000,N,,Y,Charge ref
          |2105,2023,N,,Y,Charge ref
          |2105,2026,N,,Y,Charge ref
          |2105,2030,N,,Y,Charge ref
          |2105,2100,N,,Y,Charge ref
          |2110,1090,N,,,Charge ref
          |2115,1090,N,,,Charge ref
          |2120,1090,N,,,Charge ref
          |2125,1090,N,,,Charge ref
          |2130,1355,Y,,,ASN
          |2135,2355,N,,Y,Charge ref
          |2500,1090,Y,,,Charge ref
          |2505,2090,N,,Y,Charge ref
          |2510,1090,Y,,,Charge ref
          |2515,2090,N,,Y,Charge ref
          |2520,1090,Y,,,Charge ref
          |2525,2090,N,,Y,Charge ref
          |2530,1090,Y,,,Charge ref
          |2535,2090,N,,Y,Charge ref
          |2540,1090,Y,,,Charge ref
          |2545,2090,N,,Y,Charge ref
          |2550,1090,Y,,,Charge ref
          |2555,2090,N,,Y,Charge ref
          |2560,1090,Y,,,Charge ref
          |2565,2090,N,,Y,Charge ref
          |2570,1090,Y,,,Charge ref
          |2575,2090,N,,Y,Charge ref
          |2580,1090,Y,,,Charge ref
          |2585,2090,N,,Y,Charge ref
          |2590,1090,Y,,,Charge ref
          |2595,2090,N,,Y,Charge ref
          |
          |VAT
          |Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref
          |4700,1174,Y,,,VRN
          |4620,1175,,,Y,VRN
          |4703,1090,Y,,,VRN
          |4622,1175,,,Y,VRN
          |4704,1090,Y,,,VRN
          |4624,1175,,,Y,VRN
          |7700,1174,,,,VRN
          |4748,1090,Y,,,VRN
          |4749,1175,,,Y,VRN
          |4735,1090,Y,,,VRN
          |4682,1175,,,Y,VRN
          |7735,1090,,,,VRN
          |4760,1090,Y,,,VRN
          |4693,1175,,,Y,VRN
          |7760,1090,,,,VRN
          |4763,1090,,,,VRN
          |4766,1090,Y,,,VRN
          |4767,1175,,,Y,VRN
          |7766,1090,,,,VRN
          |4745,1090,Y,,,VRN
          |4684,1175,,,Y,VRN
          |7745,1090,,,,VRN
          |4770,1090,Y,,,VRN
          |4771,1175,,,Y,VRN
          |4773,1090,Y,,,VRN
          |4774,1175,,,Y,VRN
          |4776,1090,Y,,,VRN
          |4777,1175,,,Y,VRN
          |7776,1090,,,,VRN
          |4755,1090,Y,,,VRN
          |4687,1175,,,Y,VRN
          |7755,1090,,,,VRN
          |4783,1090,Y,,,VRN
          |4784,1175,,,Y,VRN
          |7783,1090,,,,VRN
          |4786,1090,,,,VRN
          |7786,1090,,,,VRN
          |4765,1090,Y,,,VRN
          |4695,1175,,,Y,VRN
          |7765,1090,,,,VRN
          |4775,1090,Y,,,VRN
          |4697,1175,,,Y,VRN
          |7775,1090,,,,VRN
          |4790,1090,Y,,,VRN
          |4791,1175,,,Y,VRN
          |4793,1090,Y,,,VRN
          |4794,1175,,,Y,VRN
          |4796,1090,,,,VRN
          |7796,1090,,,,VRN
          |4799,1090,,,,VRN
          |7799,1090,,,,VRN
          |4747,1090,,,,VRN
          |7747,1090,,,,VRN
          |4711,1174,,,,VRN""".stripMargin

      val csvInputWithMessyHeadings: String =
        """Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref,Period End
          |5330,7006,N,N/A,N,N/A,
          |5330,7010,N,N/A,N,N/A,
          |5330,7011,N,N/A,N,N/A,
          |5350,7012,N,N/A,N,N/A,
          |5350,7014,N,N/A,N,N/A,
          |5350,7013,N,N/A,N,N/A,
          |1085,1000,N,N/A,N,N/A,
          |1085,1020,N,N/A,N,N/A,
          |1085,1025,N,N/A,N,N/A,
          |1085,1180,N,N/A,N,N/A,
          |1511,2000,N,N/A,Y,N/A,
          |1515,1090,N,N/A,N,N/A,
          |1520,1090,N,N/A,N,N/A,
          |1525,1000,Y,4,N,N/A,charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1526,2000,N,N/A,Y,N/A,
          |1530,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1531,2000,N,N/A,Y,N/A,
          |1535,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1536,2000,N,N/A,Y,N/A,
          |1540,1000,Y,4,N,N/A,charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
          |1541,2000,N,N/A,Y,N/A,
          |1545,1000,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545,1090,Y,4,N,N/A,Tax Yr (6/4/yyyy - 5/4/yyyy)
          |1545,2000,Y,4,N,N/A,
          |1546,2000,N,N/A,Y,N/A,
          |2421,1150,N,N/A,N,N/A,
          |1441,1150,N,N/A,N,N/A,
          |4618,1090,N,N/A,N,N/A,
          |3996,1091,Y,4,N,N/A,
          |3997,2091,N,N/A,Y,N/A,
          |,,,,,,
          |PAYE,,,,,,
          |Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref,
          |1025,1090,N,,,Charge ref,
          |1030,1090,N,,,Charge ref,
          |1035,1090,N,,,Charge ref,
          |1040,1090,N,,,Charge ref,
          |1045,1090,N,,,Charge ref,
          |2000,1000,Y,,,ASN,
          |2000,1020,Y,,,ASN,
          |2000,1023,Y,,,ASN,
          |2000,1026,Y,,,ASN,
          |2000,1030,Y,,,ASN,
          |2000,1100,Y,,,ASN,
          |2005,2000,N,,Y,Charge ref,
          |2005,2020,N,,Y,Charge ref,
          |2005,2023,N,,Y,Charge ref,
          |2005,2026,N,,Y,Charge ref,
          |2005,2030,N,,Y,Charge ref,
          |2005,2100,N,,Y,Charge ref,
          |2006,1106,Y,,,ASN,
          |2007,1107,N,,Y,Charge ref,
          |2030,1250,Y,,,ASN,
          |2030,1260,Y,,,ASN,
          |2030,1270,Y,,,ASN,
          |2030,1280,Y,,,ASN,
          |2030,1290,Y,,,ASN,
          |2030,1300,Y,,,ASN,
          |2030,1310,Y,,,ASN,
          |2030,1320,Y,,,ASN,
          |2030,1330,Y,,,ASN,
          |2030,1340,Y,,,ASN,
          |2030,1350,Y,,,ASN,
          |2030,1390,Y,,,ASN,
          |2030,1395,Y,,,ASN,
          |2040,1000,Y,,,Charge ref,
          |2045,2000,N,,Y,Charge ref,
          |2045,2100,N,,Y,Charge ref,
          |2060,1020,Y,,,Charge ref,
          |2065,2020,N,,Y,Charge ref,
          |2090,1000,Y,,,ASN,
          |2090,1020,Y,,,ASN,
          |2090,1023,Y,,,ASN,
          |2090,1026,Y,,,ASN,
          |2090,1100,Y,,,ASN,
          |2090,1250,Y,,,ASN,
          |2090,1260,Y,,,ASN,
          |2090,1270,Y,,,ASN,
          |2090,1280,Y,,,ASN,
          |2090,1290,Y,,,ASN,
          |2090,1300,Y,,,ASN,
          |2090,1310,Y,,,ASN,
          |2090,1320,Y,,,ASN,
          |2090,1330,Y,,,ASN,
          |2090,1340,Y,,,ASN,
          |2090,1350,Y,,,ASN,
          |2095,2000,N,,Y,Charge ref,
          |2095,2020,N,,Y,Charge ref,
          |2095,2023,N,,Y,Charge ref,
          |2095,2026,N,,Y,Charge ref,
          |2095,2100,N,,Y,Charge ref,
          |2100,1000,Y,,,Charge ref,
          |2100,1023,Y,,,Charge ref,
          |2100,1026,Y,,,Charge ref,
          |2100,1030,Y,,,Charge ref,
          |2100,1100,Y,,,Charge ref,
          |2105,2000,N,,Y,Charge ref,
          |2105,2023,N,,Y,Charge ref,
          |2105,2026,N,,Y,Charge ref,
          |2105,2030,N,,Y,Charge ref,
          |2105,2100,N,,Y,Charge ref,
          |2110,1090,N,,,Charge ref,
          |2115,1090,N,,,Charge ref,
          |2120,1090,N,,,Charge ref,
          |2125,1090,N,,,Charge ref,
          |2130,1355,Y,,,ASN,
          |2135,2355,N,,Y,Charge ref,
          |2500,1090,Y,,,Charge ref,
          |2505,2090,N,,Y,Charge ref,
          |2510,1090,Y,,,Charge ref,
          |2515,2090,N,,Y,Charge ref,
          |2520,1090,Y,,,Charge ref,
          |2525,2090,N,,Y,Charge ref,
          |2530,1090,Y,,,Charge ref,
          |2535,2090,N,,Y,Charge ref,
          |2540,1090,Y,,,Charge ref,
          |2545,2090,N,,Y,Charge ref,
          |2550,1090,Y,,,Charge ref,
          |2555,2090,N,,Y,Charge ref,
          |2560,1090,Y,,,Charge ref,
          |2565,2090,N,,Y,Charge ref,
          |2570,1090,Y,,,Charge ref,
          |2575,2090,N,,Y,Charge ref,
          |2580,1090,Y,,,Charge ref,
          |2585,2090,N,,Y,Charge ref,
          |2590,1090,Y,,,Charge ref,
          |2595,2090,N,,Y,Charge ref,
          |,,,,,,
          |VAT,,,,,,
          |Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref,
          |4700,1174,Y,,,VRN,
          |4620,1175,,,Y,VRN,
          |4703,1090,Y,,,VRN,
          |4622,1175,,,Y,VRN,
          |4704,1090,Y,,,VRN,
          |4624,1175,,,Y,VRN,
          |7700,1174,,,,VRN,
          |4748,1090,Y,,,VRN,
          |4749,1175,,,Y,VRN,
          |4735,1090,Y,,,VRN,
          |4682,1175,,,Y,VRN,
          |7735,1090,,,,VRN,
          |4760,1090,Y,,,VRN,
          |4693,1175,,,Y,VRN,
          |7760,1090,,,,VRN,
          |4763,1090,,,,VRN,
          |4766,1090,Y,,,VRN,
          |4767,1175,,,Y,VRN,
          |7766,1090,,,,VRN,
          |4745,1090,Y,,,VRN,
          |4684,1175,,,Y,VRN,
          |7745,1090,,,,VRN,
          |4770,1090,Y,,,VRN,
          |4771,1175,,,Y,VRN,
          |4773,1090,Y,,,VRN,
          |4774,1175,,,Y,VRN,
          |4776,1090,Y,,,VRN,
          |4777,1175,,,Y,VRN,
          |7776,1090,,,,VRN,
          |4755,1090,Y,,,VRN,
          |4687,1175,,,Y,VRN,
          |7755,1090,,,,VRN,
          |4783,1090,Y,,,VRN,
          |4784,1175,,,Y,VRN,
          |7783,1090,,,,VRN,
          |4786,1090,,,,VRN,
          |7786,1090,,,,VRN,
          |4765,1090,Y,,,VRN,
          |4695,1175,,,Y,VRN,
          |7765,1090,,,,VRN,
          |4775,1090,Y,,,VRN,
          |4697,1175,,,Y,VRN,
          |7775,1090,,,,VRN,
          |4790,1090,Y,,,VRN,
          |4791,1175,,,Y,VRN,
          |4793,1090,Y,,,VRN,
          |4794,1175,,,Y,VRN,
          |4796,1090,,,,VRN,
          |7796,1090,,,,VRN,
          |4799,1090,,,,VRN,
          |7799,1090,,,,VRN,
          |4747,1090,,,,VRN,
          |7747,1090,,,,VRN,
          |4711,1174,,,,VRN,""".stripMargin

      val outputApplicationConf: String =
        """
          "SUYgbWFpblRyYW5zID09ICcxMDI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '1025' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcxMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '1030' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcxMDM1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '1035' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcxMDQwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '1040' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcxMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '1045' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcxMDg1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1085' AND subTrans == '1000' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxMDg1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMjAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1085' AND subTrans == '1020' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxMDg1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMjUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1085' AND subTrans == '1025' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxMDg1JyBBTkQgc3ViVHJhbnMgPT0gJzExODAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1085' AND subTrans == '1180' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNDQxJyBBTkQgc3ViVHJhbnMgPT0gJzExNTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1441' AND subTrans == '1150' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTExJyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1511' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcxNTE1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1515' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1520' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1525' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTI2JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1526' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcxNTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1530' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTMxJyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1531' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcxNTM1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1535' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTM2JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1536' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcxNTQwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1540' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTQxJyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1541' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcxNTQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1545' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1545' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTQ1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '1545' AND subTrans == '2000' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcxNTQ2JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '1546' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1020' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1023' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjYnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1026' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1030' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDAwJyBBTkQgc3ViVHJhbnMgPT0gJzExMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2000' AND subTrans == '1100' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2020' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjMnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2023' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjYnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2026' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMzAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2030' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA1JyBBTkQgc3ViVHJhbnMgPT0gJzIxMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2005' AND subTrans == '2100' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDA2JyBBTkQgc3ViVHJhbnMgPT0gJzExMDYnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2006' AND subTrans == '1106' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDA3JyBBTkQgc3ViVHJhbnMgPT0gJzExMDcnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2007' AND subTrans == '1107' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1250' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1260' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1270' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEyODAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1280' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEyOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1290' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1300' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1310' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1320' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1330' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzNDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1340' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzNTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1350' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1390' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzOTUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2030' AND subTrans == '1395' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDQwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2040' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2045' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDQ1JyBBTkQgc3ViVHJhbnMgPT0gJzIxMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2045' AND subTrans == '2100' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDYwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2060' AND subTrans == '1020' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDY1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2065' AND subTrans == '2020' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1020' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1023' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjYnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1026' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzExMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1100' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1250' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1260' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEyNzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1270' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEyODAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1280' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEyOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1290' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1300' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1310' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMjAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1320' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzMzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1330' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzNDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1340' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDkwJyBBTkQgc3ViVHJhbnMgPT0gJzEzNTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2090' AND subTrans == '1350' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMDk1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2095' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDk1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2095' AND subTrans == '2020' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDk1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjMnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2095' AND subTrans == '2023' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDk1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjYnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2095' AND subTrans == '2026' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMDk1JyBBTkQgc3ViVHJhbnMgPT0gJzIxMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2095' AND subTrans == '2100' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2100' AND subTrans == '1000' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjMnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2100' AND subTrans == '1023' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMjYnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2100' AND subTrans == '1026' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwMzAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2100' AND subTrans == '1030' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTAwJyBBTkQgc3ViVHJhbnMgPT0gJzExMDAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2100' AND subTrans == '1100' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2105' AND subTrans == '2000' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjMnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2105' AND subTrans == '2023' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMjYnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2105' AND subTrans == '2026' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwMzAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2105' AND subTrans == '2030' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIxMDAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2105' AND subTrans == '2100' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2110' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTE1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2115' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2120' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTI1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2125' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyMTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEzNTUnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '2130' AND subTrans == '1355' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICcyMTM1JyBBTkQgc3ViVHJhbnMgPT0gJzIzNTUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2135' AND subTrans == '2355' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNDIxJyBBTkQgc3ViVHJhbnMgPT0gJzExNTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '2421' AND subTrans == '1150' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICcyNTAwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2500' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTA1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2505' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTEwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2510' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTE1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2515' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTIwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2520' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTI1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2525' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTMwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2530' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTM1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2535' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTQwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2540' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTQ1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2545' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTUwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2550' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTU1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2555' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTYwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2560' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTY1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2565' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTcwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2570' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTc1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2575' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTgwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2580' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTg1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2585' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IHRydWU=",
          |# IF mainTrans == '2590' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICcyNTk1JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gdHJ1ZQ==",
          |# IF mainTrans == '2595' AND subTrans == '2090' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = true,
          |"SUYgbWFpblRyYW5zID09ICczOTk2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTEnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '3996' AND subTrans == '1091' -> intRate = 4 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICczOTk3JyBBTkQgc3ViVHJhbnMgPT0gJzIwOTEnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZQ==",
          |# IF mainTrans == '3997' AND subTrans == '2091' -> intRate = 0 AND interestOnlyDebt = true,
          |"SUYgbWFpblRyYW5zID09ICc0NjE4JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '4618' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc0NjIwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4620' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NjIyJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4622' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NjI0JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4624' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NjgyJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4682' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Njg0JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4684' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Njg3JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4687' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NjkzJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4693' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Njk1JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4695' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Njk3JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4697' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4700' AND subTrans == '1174' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzAzJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4703' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzA0JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4704' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzExJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4711' AND subTrans == '1174' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzM1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4735' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4745' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzQ3JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4747' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzQ4JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4748' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzQ5JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4749' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzU1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4755' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzYwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4760' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzYzJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4763' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzY1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4765' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzY2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4766' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzY3JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4767' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzcwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4770' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzcxJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4771' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzczJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4773' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzc0JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4774' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzc1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4775' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzc2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4776' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzc3JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4777' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzgzJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4783' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzg0JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4784' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzg2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4786' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzkwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4790' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzkxJyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4791' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0NzkzJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSA0IEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4793' AND subTrans == '1090' -> intRate = 4 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzk0JyBBTkQgc3ViVHJhbnMgPT0gJzExNzUnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gdHJ1ZSBBTkQgdXNlQ2hhcmdlUmVmZXJlbmNlID0gZmFsc2U=",
          |# IF mainTrans == '4794' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzk2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4796' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc0Nzk5JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '4799' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzMwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMDYnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5330' AND subTrans == '7006' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzMwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5330' AND subTrans == '7010' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzMwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMTEnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5330' AND subTrans == '7011' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzUwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMTInIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5350' AND subTrans == '7012' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzUwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMTMnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5350' AND subTrans == '7013' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc1MzUwJyBBTkQgc3ViVHJhbnMgPT0gJzcwMTQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2U=",
          |# IF mainTrans == '5350' AND subTrans == '7014' -> intRate = 0 AND interestOnlyDebt = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzAwJyBBTkQgc3ViVHJhbnMgPT0gJzExNzQnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7700' AND subTrans == '1174' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzM1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7735' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzQ1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7745' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzQ3JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7747' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzU1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7755' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzYwJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7760' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzY1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7765' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzY2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7766' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3Nzc1JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7775' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3Nzc2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7776' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3NzgzJyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7783' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3Nzg2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7786' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3Nzk2JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7796' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |"SUYgbWFpblRyYW5zID09ICc3Nzk5JyBBTkQgc3ViVHJhbnMgPT0gJzEwOTAnIC0+IGludFJhdGUgPSAwIEFORCBpbnRlcmVzdE9ubHlEZWJ0ID0gZmFsc2UgQU5EIHVzZUNoYXJnZVJlZmVyZW5jZSA9IGZhbHNl",
          |# IF mainTrans == '7799' AND subTrans == '1090' -> intRate = 0 AND interestOnlyDebt = false AND useChargeReference = false,
          |""".stripMargin.trim
    }

  }
}
