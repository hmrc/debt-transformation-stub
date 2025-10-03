
# debt-transformation-stub

This is a placeholder README.md for a new repository

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


## Example usage of a polling service

Modify application.conf to enable polling and increase the intervals for humans
```
isPollingEnv = true
pollingIntervals = 5000
```

### Running the service on a specific port
```sbt "run 10003"```

Open a new terminal (this will be a long lived request)
Call an endpoint defined in app.routes
This will trigger the polling service to start and also insert a request document into mongodb ttp-testonly

```
curl --location --request GET 'http://localhost:9111/individuals/subcontractor/idms/wmfid/blah' \
--header 'Content-Type: application/json' 
```

### The document looks like this.
```
{
    "_id" : ObjectId("621f6bb9ad942e03d6cf1990"),
    "requestId" : "986da206-27fd-4786-8eac-ff3a8a6a3fce",
    "content" : "{}",
    "uri" : "/individuals/subcontractor/idms/wmfid/blah",
    "isResponse" : false,
    "createdOn" : "2022-03-02T13:06:01.825459"
}
```

### Copy the requestId from this mongoDb document in your subsequent request
### The content attribute should be minified and escaped Json
### This content is what will be sent back to the original polling request in the response

```
curl --location --request POST 'http://localhost:9111/test-only/response' \
--header 'Content-Type: application/json' \
--data-raw '{
  "requestId" : "986da206-27fd-4786-8eac-ff3a8a6a3fce",
  "content" : "[{\"fileInd\":\"K\",\"wmfId\":\"00000000000036320200100009KP0001\",\"shortDescription\":\"RGD Retrn Chrg\",\"solDescription\":\"Interest on First Payment on Account\",\"latestDueDate\":\"2021-09-15\",\"accountingPeriodEndDate\":\"2021-09-15\",\"safeChargeRef\":\"XA188698553712\",\"customerRefType\":\"A\",\"outputRefType\":\"B\",\"additionalInfo\":[{\"infoType\":\"RCQ\",\"infoValue\":\"Reinstated Charge\"}],\"bsPeriod\":[{\"irIntAreaRef\":\"AX21231YE\"}],\"debtItemSignal\":[{\"signalType\":\"#TBD#\",\"signalValue\":\"C\"},{\"signalType\":\"#TBD#\",\"signalValue\":\"D\"}],\"debtItemCharge\":[{\"intBearingInd\":\"Y\",\"interestChargeInd\":\"Y\",\"currentCollectibleAmount\":34409.49,\"chargeAmount\":7899.45,\"interestStartDate\":\"2021-09-15\",\"interestSuppressionInd\":\"Y\",\"debtChargePayment\":[{\"postingAmount\":100.99,\"effectiveDateOfPayment\":\"2021-09-15\",\"adjustmentType\":\"CHG\"},{\"postingAmount\":110.99,\"effectiveDateOfPayment\":\"2021-10-15\",\"adjustmentType\":\"CHG\"}]}]},{\"fileInd\":\"L\",\"wmfId\":\"00000000000036320200100009KP0002\",\"shortDescription\":\"RGD Retrn Chrg\",\"solDescription\":\"Interest on First Payment on Account\",\"latestDueDate\":\"2021-10-15\",\"accountingPeriodEndDate\":\"2021-10-15\",\"safeChargeRef\":\"XA188698553712\",\"customerRefType\":\"A\",\"outputRefType\":\"B\",\"additionalInfo\":[{\"infoType\":\"RCQ\",\"infoValue\":\"Reinstated Charge\"}],\"bsPeriod\":[{\"irIntAreaRef\":\"AX21231YE\"}],\"debtItemSignal\":[{\"signalType\":\"#TBD#\",\"signalValue\":\"D\"},{\"signalType\":\"#TBD#\",\"signalValue\":\"E\"}],\"debtItemCharge\":[{\"intBearingInd\":\"Y\",\"interestChargeInd\":\"Y\",\"currentCollectibleAmount\":54409.49,\"chargeAmount\":8899.45,\"interestStartDate\":\"2021-10-15\",\"interestSuppressionInd\":\"Y\",\"debtChargePayment\":[{\"postingAmount\":200.99,\"effectiveDateOfPayment\":\"2021-10-15\",\"adjustmentType\":\"CHG\"},{\"postingAmount\":210.99,\"effectiveDateOfPayment\":\"2021-11-15\",\"adjustmentType\":\"CHG\"}]}]}]",
  "uri" : "/individuals/subcontractor/idms/wmfid/blah",
  "isResponse" : true
}'
```
### To apply formatting to this repository using the configured rules in .scalafmt.conf execute:

### To run stub locally 
sbt "run 10003"

sbt scalafmtAll scalafmtSbt
### To check files have been formatted as expected execute:

sbt scalafmtCheckAll scalafmtSbtCheck

## Interest Forecasting rules generator

Run the `main` method in `InterestForecastingRulesGenerator` with the appropriate arguments.

When modifying the run configuration the arguments need to include both the input and the output.

### Input Arguments

One of the following input arguments must be provided to specify the source of the interest forecasting rules.
This is expected to be a variant of what we call the "master spreadsheet" of interest forecasting rules.

#### Input Argument `--input-file=/absolute/path/to/file.tsv`
Tells the tool to read a TSV or CSV file representing the "master spreadsheet" with the interest forecasting rules.

The file can be `*.tsv` or `*.csv` and must have one of the following formats:
```CSV
Main Trans,Sub Trans,Interest bearing,Interest key,Interest only Debt,Charge Ref,Regime Usage,Period End
5330,7006,N,N/A,N,N/A,CDCS,
5330,7010,N,N/A,N,N/A,CDCS,
1525,1000,Y,4,N,N/A,CDCS,charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
2030,1350,Y,,,ASN,PAYE,
2045,2000,N,,Y,Charge ref,PAYE,
2045,2100,N,,Y,Charge ref,PAYE,
```

```TSV
Main Trans	Sub Trans	Interest bearing	Interest key	Interest only Debt	Charge Ref	Regime Usage	Period End
5330	7006	N	N/A	N	N/A	CDCS
5330	7010	N	N/A	N	N/A	CDCS
1525	1000	Y	4	N	N/A	CDCS	charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
2030	1350	Y			ASN	PAYE
2045	2000	N		Y	Charge ref	PAYE
2045	2100	N		Y	Charge ref	PAYE
```

#### Input Argument `--input-console-tsv`

Specified instead of `--input-file=...` to allow users to copy the data straight from Excel and paste it
directly into the terminal.

You'll be given a string to add after your pasted content, to signal the end of the data.

The pasted content must have the following format:
```TSV
Main Trans	Sub Trans	Interest bearing	Interest key	Interest only Debt	Charge Ref	Regime Usage	Period End
5330	7006	N	N/A	N	N/A	CDCS
5330	7010	N	N/A	N	N/A	CDCS
1525	1000	Y	4	N	N/A	CDCS	charged per quarter. Info passed will be quarter + year. E.g. 01 2004 (01/01/yyyy - 31/03/yyyy)
2030	1350	Y			ASN	PAYE
2045	2000	N		Y	Charge ref	PAYE
2045	2100	N		Y	Charge ref	PAYE
```

### Output Format Arguments (`--output-format=*`)

You must provide one of the following output format arguemnts to specify the desired output format.

#### Output Format Argument `--output-format=ifs-scala-config`

Provided if the desired output is the Scala-based rules config of `interest-forecasting`. Supported after `DTD-3573`.

Generates the entire expected Scala file `BusinessRulesConfiguration.scala`. Look for it in `interest-forecasting`.

#### Output Format Argument `--output-format=application-conf`

Provided if the desired output is the `rules` section in the `application.conf` of `interest-forecasting`.

#### Output Format Argument `--output-format=production-config`

Provided if the desired output is the `service-config.rules` entries in the production config of `interest-forecasting`.

### Output Location Arguments (`--output=*`)

Determines where the output will be written.
Only one supported value exists at the moment, but we may one day allow the tool to write directly to the desired file.

#### Output Location Argument `--output=console`

Provided if the desired output is to be printed to the console.

## SA E2E Stub Data added as part of DTD-3877

| Microservice | UTR        | API Call                 | Response | Test Thread |
|--------------|------------|--------------------------|----------|-------------|
| Eligibility  | 2208274718 | SA Customer Data Service | 500      | DMBP-1408   |
