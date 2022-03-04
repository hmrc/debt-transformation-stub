
# debt-transformation-stub

This is a placeholder README.md for a new repository

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


### Example usage of a polling service

Modify application.conf to enable polling and increase the intervals for humans
```
isPollingEnv = true
pollingIntervals = 5000
```

Run the service on a specific port
```sbt "run 9111"```

Open a new terminal (this will be a long lived request)
Call an endpoint defined in app.routes
This will trigger the polling service to start and also insert a request document into mongodb ttp-testonly

```
curl --location --request GET 'http://localhost:9111/individuals/subcontractor/idms/wmfid/blah' \
--header 'Content-Type: application/json' 
```

The document looks like this.
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

Copy the requestId from this mongoDb document in your subsequent request
The content attribute should be minified and escaped Json
This content is what will be sent back to the original polling request in the response

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

