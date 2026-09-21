# Bruno API collection - Import guide
This guide provides instructions for importing and setting up the API collection on Bruno locally.
___

## Prerequisites
Before you begin, ensure you have:
* Bruno installed
* Repo cloned locally

## Open Collection in Bruno
* Launch the **Bruno** application
* Click **File → Open Collection**
* Navigate to the cloned project location
* Select `debtTransformationStubAPIBrunoCollection` folder from inside the `debtTransformationStubBrunoCollectionAndGlobal`
* Click **Open**

The collection will load automatically with all requests and folders visible in the left sidebar.

# How to run the services

The `debtTransformationStub` service can be run through the Service Manager via `DTD_ALL` profile:
```
sm2 --start DTD_ALL
```

Or locally with the testOnlyDoNotUseInAppConf.Routes specified: (Port is 10003)
```
sbt "run <PORT>"
```

## How to use the collection
```
Once the collection is loaded, set the Global in Environment on the top right corner importing Global.json from "debtTransformationStubBrunoCollectionAndGlobal" package.
To use enactStage requests you will first need to create an self-serve arrangement from TTP requests manually and it will store the correlationId/idValue from request body in the global.
You can now send the enactStage request manually.
```

## Endpoints can be tested manually in debt-transformation-stub
```
POST        /enactStage 
POST        /enactStageByIdValue
```