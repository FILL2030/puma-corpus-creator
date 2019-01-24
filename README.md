# Puma Corpus Creator

## Overview

The Puma Corpus Creator (PCC) application provides the backbone to obtaining and storing document metadata and full-text files.

Documents are added to the corpus from different <em>Puma Importer</em> web services via <em>import</em> actions. Each importer produces document data from a specific source and is added and configured through a PCC API.  This metadata (such as title, abstract, author, journal, affiliations, etc) is stored in a PostgreSQL database. Each importer provides documents of different types: generally facility proposals and scientific articles (publications) - the aim of <em>Puma</em> being to match publications to proposals.

Full-text files are either obtained directly from the importer or, more often, from an external source. In such cases, to obtain the full-text a DOI or URL is passed to the <em>Puma URL Resolver</em> which will then locate and download the associated file. These are then stored on the file system. 

An analysis is performed on the raw full-text files to obtain plain text and image data (where needed). For PDF full-text there is, for example, an extraction of raw text and images using Abbyy Finereader.

Elasticsearch is then used to index the document metadata and plain full-text.

Nightly imports, analysis and indexing can be performed using the in-built Scheduler.

A REST API provides access to imports, analysis, indexing and general monitoring.

The document metadata and plain full-text is used by the <em>Puma Matcher</em> to provide candidate matches of publications to proposals.

Data analysis is performed on the document metadata and match results.

### Quick start

### Building
```bash
# mvn clean
# mvn package 
```
### Running
A number of environment variables are required to PCC to connect to the PostrgreSQL database, Elasticsearch server and for PCC to function correctly.  

| Environment variable | Function |
|---|---|
| PUMA_PCC_DATASOURCE_URL | PostgreSQL database URL|
| PUMA_PCC_DATASOURCE_USERNAME | PostgreSQL database username |
| PUMA_PCC_DATASOURCE_PASSWORD | PostgreSQL database password |
| PUMA_PCC_DATASOURCE_SCHEMA | PostgreSQL database schema |
| PUMA_PCC_ELASTICSEARCH_HOSTS | Elasticsearch hosts (separated by a comma) |
| PUMA_PCC_ELASTICSEARCH_PORT | Elasticsearch port |
| PUMA_PCC_ELASTICSEARCH_CLUSTERNAME | Elasticsearch cluster name |
| PUMA_PCC_ELASTICSEARCH_INDEX | Elasticsearch index |
| PUMA_PCC_RESOLVER_URL | URL of the Puma URL Resolver |
| PUMA_PCC_RESOLVER_RECOVER_ON_RESTART | Specify whether and interrupted resolvers should be restarted when starting PCC |
| PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART | Specify whether and interrupted downloads should be restarted when starting PCC |
| PUMA_PCC_IMPORTER_RECOVER_ON_RESTART | Specify whether and interrupted imports should be restarted when starting PCC |
| PUMA_PCC_FILES_PATH | Path where full-text files will be stored |
| PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE | Maximum number of threads to run parallel tasks |
| PUMA_PCC_ABBY_BATCH_PATH | Path to Abbyy batch folder |
| PUMA_PCC_ABBY_INPUT_PATH | Path to Abbyy input folder |
| PUMA_PCC_ABBY_OUTPUT_PATH | Path to Abbyy output folder |
| PUMA_PCC_VM_ARG | General VM arguments to be sent to PCC (such as proxy settings and ports) |


PCC is launched as follows:

```bash
export PUMA_PCC_DATASOURCE_PASSWORD='abc'
export PUMA_PCC_DATASOURCE_URL='jdbc:postgresql://db-server.inst.com'
export PUMA_PCC_DATASOURCE_USERNAME='puma-owner'
export PUMA_PCC_DATASOURCE_SCHEMA='puma'
export PUMA_PCC_ELASTICSEARCH_HOSTS='es-server-1.inst.com,es-server-2.inst.com'
export PUMA_PCC_ELASTICSEARCH_PORT=9300
export PUMA_PCC_ELASTICSEARCH_CLUSTERNAME='puma'
export PUMA_PCC_ELASTICSEARCH_INDEX='puma'
export PUMA_PCC_DOWNLOADER_RECOVER_ON_RESTART='false'
export PUMA_PCC_FILES_PATH='/data/puma/files'
export PUMA_PCC_IMPORTER_RECOVER_ON_RESTART='false'
export PUMA_PCC_RESOLVER_RECOVER_ON_RESTART='false'
export PUMA_PCC_RESOLVER_URL='http://localhost:8020/api/v1'
export PUMA_PCC_TASKMANAGER_THREAD_POOL_SIZE='32'
export PUMA_PCC_ABBY_BATCH_PATH='/data/abby/batch/'
export PUMA_PCC_ABBY_INPUT_PATH='/data/abby/input/'
export PUMA_PCC_ABBY_OUTPUT_PATH='/data/abby/output/'
export PUMA_PCC_VM_ARGS='-Dserver.address=127.0.0.1 -Dserver.port=8000 -Xms512M -Xmx3G -Dhttp.proxyHost=proxyhost.inst.com -Dhttp.proxyPort=port -Dhttps.proxyHost=proxyhost.inst.com -Dhttps.proxyPort=port'

./run.sh
```

### Monitoring

You can verify that PCC is running by going to the monitoring URL (assuming PCC is running on port 8000)

```bash
http://localhost:8000/api/v1/monitoring
```

This should return JSON data with information about the current status of the Puma Corpus Creator.

### Usage / REST API

The following tables outline the different functionalities accessible through the PCC API.

#### Importers

| HTTP Method | Endpoint |Function |
|---|---|---|
| GET | [/api/v1/importers](#list-all-importers) | List all importers  | 
| POST | [/api/v1/importer](#create-a-new-importer) | Create a new importer | 
| GET | /api/v1/importers/{importerId} | Get details of an importer | 
| PUT | /api/v1/importers/{importerId} | Edit an importer | 
| DELETE | /api/v1/importers/{importerId} | Delete an importer | 
| GET | [/api/v1/importers/{importerId}/operations](#list-all-details-of-current-operations-of-an-importer) | List all details of current operations of an importer | 
| GET | /api/v1/importers/{importerId}/operations/history | List all details of previous operations of an importer | 
| POST | [/api/v1/importers/{importerId}/operations](#create-a-new-operation-on-a-specific-importer) | Create a new operation on a specific importer | 
| GET | /api/v1/operations | List all details of current importer operations | 
| GET | /api/v1/operations/history | List all details of previous importer operations | 
| GET | /api/v1/operations/{operationId} | Get details/status of an importer operation | 
| DELETE | /api/v1/operations/{operationId} | Cancel an importer operation | 

##### List all importers

<b>URL : </b> /api/v1/importers

<b>Method : </b> GET

<b>Return data : </b>
```
[
  {
	"id": 1,
	"shortName": "wos",
	"name": "WOS Importer"
	"url": "http://...",
  },
  {
	"id": 2,
	"shortName": "ill",
	"name": "ILL Importer"
	"url": "http://...",
  }, ...
]
```

##### Create a new importer

<b>URL : </b> /api/v1/importers

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "shortName": "wos",
  "name": "WOS Importer"
  "url": "http://...",
}
```

<b>Return data : </b>
```
{
  "id": 1,
  "shortName": "wos",
  "name": "WOS Importer"
  "url": "http://...",  
}
```

##### List all details of current operations of an importer

<b>URL : </b> /api/v1/importers/{importerId}/operations

<b>Method : </b> GET

<b>Return data : </b>
```
[
  {
    "id": 1,
    "status": "RUNNING",
    "params": {
      "query": "TS=neutron ill acid diffraction"
    },
    "updateCitations": false,
    "downloadFiles": false,
    "reimportAll": false,
    "lastImportedDocumentVersionId": 100,
    "creationDate": 1506666193659,
    "totalDocumentCount": 11,
    "updateExisting": false,
    "documentsReceived": 5,
    "documentsIntegrated": 5,
    "runTime": 1822
  }
]
```

##### Create a new operation on a specific importer

<b>URL : </b> /api/v1/importers/{importerId}/operations

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "params": {
    "query": "TS=neutron ill acid diffraction"
  },
  "updateCitations": false,
  "downloadFiles": false,
  "reimportAll": false,
  "updateExisting": false,
}
```

<b>Return data : </b>
```
{
  "id": 1,
  "status": "RUNNING",
  "params": {
    "query": "TS=neutron ill acid diffraction"
  },
  "updateCitations": false,
  "downloadFiles": false,
  "reimportAll": false,
  "lastImportedDocumentVersionId": 100,
  "creationDate": 1506666193659,
  "totalDocumentCount": 11,
  "updateExisting": false,
  "documentsReceived": 5,
  "documentsIntegrated": 5,
  "runTime": 1822
}
```

#### Full-text analysis

| HTTP Method | Endpoint | Function |
|---|---|---|
| GET | [/api/v1/analysers](#get-full-list-of-documents-pending-analysis) | Get full list of documents pending analysis  | 
| GET | /api/v1/analysers/{number}  | Get list of {number} documents pending analysis  | 
| GET | /api/v1/analysers/active  | Get list of documents being analysed  | 
| GET | /api/v1/analysers/history/{number}  | Get list of {number} documents that have been analysed  | 
| POST | [/api/v1/analysers](#submit-an-analysis-command-on-all-documents) | Submit an analysis command on all documents  | 
| POST | /api/v1/analysers/{id} | Submit an analysis command on a specific document  | 
| POST | /api/v1/analysers/{ids} | Submit an analysis command on list of specific documents | 
| DELETE | /api/v1/analysers/{id} | Cancel the analysis of a specific document | 

##### Get full list of documents pending analysis

<b>URL : </b> /api/v1/analysers

<b>Method : </b> GET

<b>Return data : </b>
```
{
  "totalNumberToAnalyse": 1,
  "documents": [
    {
      "analysisState": {
      "reference": "TO_ANALYSE",
      "laboratory": "CLOSED",
      "person": "CLOSED",
      "journal": "CLOSED",
      "publisher": "CLOSED",
      "abstractText": "CLOSED",
      "doi": "CLOSED",
      "releaseDate": "CLOSED",
      "title": "CLOSED",
      "researchDomain": "CLOSED",
      "instrument": "TO_ANALYSE",
      "keyword": "TO_ANALYSE",
      "formula": "TO_ANALYSE",
      "citation": "TO_ANALYSE",
      "fullText": "TO_ANALYSE",
      "analysisSetup": null,
      "analysisDate": null,
      "id": 255570
    },
    "abstract": "...",
    "doi": "...",
    "title": "...",
    "id": 1
    }
  ]
}
```

##### Submit an analysis command on all documents

<b>URL : </b> /api/v1/analysers

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "maxNumber": 20
}
```

#### File downloads

| HTTP Method | Endpoint | Function |
|---|---|---|
| GET | [/api/v1/downloads](#get-full-list-of-downloads-that-are-either-pending-or-active) | Get full list of downloads that are either pending or active  | 
| POST | [/api/v1/downloads](#submit-command-to-activate-pending-downloads) | Submit command to activate pending downloads | 


##### Get full list of downloads that are either pending or active

<b>URL : </b> /api/v1/downloads

<b>Method : </b> GET

<b>Return data : </b>
```
{
  "numberOfDownloads": 99,
  "downloads": [
    {
      "status": "PENDING",
      "originUrl": "https://..."
    },
    {
      "status": "PENDING",
      "originUrl": "https://..."
    }, ...
  ]
}
```

##### Submit command to activate pending downloads

<b>URL : </b> /api/v1/downloads

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "maxNumber": 20
}
```


#### URL Resolvers

| HTTP Method | Endpoint | Function |
|---|---|---|
| GET | [/api/v1/resolvers](#get-full-list-of-resolvers-that-are-either-pending-or-active) | Get full list of resolvers that are either pending or active  | 
| GET | /api/v1/resolvers/active | Get full list of active resolvers | 
| GET | /api/v1/resolvers/pending | Get full list of pending resolvers | 
| GET | /api/v1/resolvers/failed | Get full list of failed resolvers | 
| GET | /api/v1/resolvers/history/{number} | Get list of {number} resolvers that have been run| 
| POST | [/api/v1/resolvers](#send-a-command-to-activate-pending-resolvers) | Send a command to activate pending resolvers | 
| GET | /api/v1/resolvers/{id} | Get a specific resolver | 
| POST | /api/v1/resolvers/{id} | Send a command to activate a specific resolver | 
| POST | /api/v1/resolvers/{ids} | Send a command to activate specific resolvers | 
| POST | /api/v1/resolvers/host/{host} | Send a command to activate resolvers for a specific journal, identified by the host address | 
| POST | /api/v1/resolvers/{id}/upload | Manually upload a full text file for a specific resolver | 
| GET | /api/v1/resolvers/next | Get the next resolver to be handled | 
| GET | /api/v1/resolvers/next/host/{host} | Get the next resolver to be handled for a specific journal host address | 
| GET | /api/v1/resolvers/{id}/next | Get the next resolver to be handled after the given one | 
| GET | /api/v1/resolvers/{id}/next/host/{host} | Get the next resolver to be handled after the given one for a particular journal host address | 


##### Get full list of resolvers that are either pending or active

<b>URL : </b> /api/v1/resolvers

<b>Method : </b> GET

<b>Return data : </b>
```
{
  "numberOfResolverInfos": 9201,
  "resolverInfos": [
    {
      "status": "PENDING",
      "resolverHost": "www.nrcresearchpress.com",
      "originUrl": "https://...",
      "documentVersionId": 35944,
      "id": 91507
    },
    {
      "status": "PENDING",
      "resolverHost": "aas.aanda.org",
      "originUrl": "https://...",
      "documentVersionId": 36045,
      "id": 91512
    }
  ]
}
```

##### Send a command to activate pending resolvers

<b>URL : </b> /api/v1/resolvers

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "maxNumber": 20
}
```

#### Document indexer

| HTTP Method | Endpoint | Function |
|---|---|---|
| POST | /api/v1/indexer/{id} | Index a specific document | 
| POST | /api/v1/indexer/{ids} | Index specific documents | 
| POST | /api/v1/indexer/reindex | Re-index all documents | 
| POST | /api/v1/indexer/remaining | Index remaining documents | 
| POST | /api/v1/indexer/info | Get information concerning the number of documents indexed, number remaining and current state | 
| POST | /api/v1/indexer/pause | Pause indexation | 
| POST | /api/v1/indexer/resume | Resume indexation | 
| POST | /api/v1/indexer/cancelAll | Cancel all active indexation | 

#### Job scheduler

| HTTP Method | Endpoint | Function |
|---|---|---|
| GET | [/api/v1/jobs](#get-list-of-all-scheduled-jobs) | Get list of all scheduled jobs | 
| GET | /api/v1/jobs/{jobId} | Get details of a specific job | 
| POST | [/api/v1/jobs](#add-a-new-job-to-the-scheduler) | Add a new job to the scheduler | 
| PUT | /api/v1/jobs | Update a job | 
| DELETE | /api/v1/jobs/{jobId} | Delete a specific job | 
| POST | /api/v1/jobs/{jobId}/disable | Disable a specific a job | 
| POST | /api/v1/jobs/{jobId}/enable | Enable a specific a job | 
| GET | [/api/v1/jobs/runners](#get-the-list-of-available-job-runners) | Get the list of available job runners | 

##### Get list of all scheduled jobs

<b>URL : </b> /api/v1/jobs

<b>Method : </b> GET

<b>Return data : </b>
```
[
  {
    "id": 18,
    "name": "WoS Updater Job",
    "jobRunnerName": "previous_months_importer_operation_job_runner",
    "scheduling": "0 0 0 * * *",
    "enabled": true,
    "jobData": "{\"importerId\":\"1\",\n\"numberOfMonths\":4,\n\"importerOperation\": {\n  \"updateExisting\": \"false\",\n  \"downloadFiles\": \"true\",\n  \"params\": {\n    \"query\": "neutron" \n    }\n  }\n}",
    "lastRunDate": 1548284400067
  },
  {
    "id": 63,
    "name": "Analyse all pending document",
    "jobRunnerName": "launch_all_analyser_job_runner",
    "scheduling": "0 0 2 * * *",
    "enabled": true,
    "jobData": "{}",
    "lastRunDate": 1548291600005
  },
  {
    "id": 64,
    "name": "Index all pending document",
    "jobRunnerName": "index_all_remaining_job_runner",
    "scheduling": "0 0 4 * * *",
    "enabled": true,
    "jobData": "{}",
    "lastRunDate": 1548298800183
  },
]
```

##### Add a new job to the scheduler

<b>URL : </b> /api/v1/jobs

<b>Method : </b> POST

<b>Post body data: </b>
```
{
  "name": "Analyse all pending document",
  "jobRunnerName": "launch_all_analyser_job_runner",
  "scheduling": "0 0 2 * * *",
  "enabled": true,
  "jobData": "{}",
}
```

<b>Return data: </b>
```
{
  "id": 63,
  "name": "Analyse all pending document",
  "jobRunnerName": "launch_all_analyser_job_runner",
  "scheduling": "0 0 2 * * *",
  "enabled": true,
  "jobData": "{}",
}
```

##### Get the list of available job runners

<b>URL : </b> /api/v1/jobs/runners

<b>Method : </b> GET

<b>Return data : </b>
```
[
  "index_all_remaining_job_runner",
  "last_id_importer_operation_job_runner",
  "launch_all_analyser_job_runner",
  "resolver_info_job_runner",
  "generic_importer_operation_job_runner",
  "previous_months_importer_operation_job_runner"
]
```

#### Monitoring

| HTTP Method | Endpoint | Function |
|---|---|---|
| GET | [/api/v1/monitoring](#get-status-of-puma-corpus-creator) | Get status of Puma Corpus Creator | 

##### Get status of Puma Corpus Creator


<b>URL : </b> /api/v1/jobs/monitoring

<b>Method : </b> GET

<b>Return data : </b>
```
{
  "numberOfPendingTasks": 0,
  "numberOfActiveThreads": 0,
  "numberOfThreads": 32,
  "numberOfImportTasks": 0,
  "numberOfDownloadTasks": 0,
  "numberOfResolverTasks": 0,
  "numberOfAnalysisTasks": 0,
  "numberOfDocumentsPendingAnalysis": 1,
  "numberOfJobRunner": 6,
  "numberOfJobs": 4,
  "numberOfRegisteredAnalyser": 2,
  "numberOfInstantiatedAnalyser": 8,
  "indexationState": "Pending",
  "numberOfPendingIndexation": 0
}
```
