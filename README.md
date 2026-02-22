# 🚭 CTA Smokers API

Crowdsourced smoking reports on Chicago CTA trains --- simple,
transparent, and public.

The **CTA Smokers API** powers https://ctasmokers.com and provides a
clean REST interface for reporting and retrieving smoking incidents on
CTA trains. Built to help riders make informed decisions in real time.

------------------------------------------------------------------------

## 🚇 Overview

The API provides structured access to community-submitted smoking
reports:

-   Submit reports with line, car number, run number, and direction
-   Retrieve recent reports by date
-   ISO-8601 timestamps and predictable JSON schema
-   Designed for dashboards, mobile apps, and data analysis

------------------------------------------------------------------------

## 🌐 Base URL

    https://api.ctasmokers.com

> All responses are JSON.\
> No authentication required for read endpoints.\
> Write endpoints are rate-limited to prevent abuse.

------------------------------------------------------------------------

## 📄 OpenAPI Specification

The full OpenAPI spec is available for developers who want to generate clients or explore endpoints interactively.

**Endpoints:**

* `GET /v3/api-docs` — JSON format (OpenAPI 3.1)

You can view or import it directly in tools like [Swagger Editor](https://editor.swagger.io/) or [Postman](https://www.postman.com/).

Example:

``` bash
curl -s https://api.ctasmokers.com/v3/api-docs | jq '.'
```

------------------------------------------------------------------------

## 🧩 Example Usage

### Submit a smoking report

``` bash
curl -X POST https://api.ctasmokers.com/api/cta/smoking/reports   -H "Content-Type: application/json"   -d '{
    "line": "RED",
    "destination": "HOWARD",
    "nextStop": "Fullerton",
    "carNumber": "2435",
    "runNumber": "902" // Optional, but helps identify the train and track patterns over time
}'
```

### Fetch today's reports

``` bash
curl -s https://api.ctasmokers.com/api/cta/smoking/reports/2026-02-21   | jq '[.reports[] | {line, destination, carNumber, reportedAt}]'
```

------------------------------------------------------------------------

## 📚 Endpoints

### Smoking Reports

-   `POST /api/cta/smoking/reports ` — Submit a new smoking report.
-   `GET /api/cta/smoking/reports/{date}` — List reports by date.
-   `GET /api/cta/smoking/reports/{date}/{reportId}` — Retrieve a specific report.

------------------------------------------------------------------------

## 🗂 Data Model

### Smoking Report

``` json
{
  "reportId": "1771710202399#7a8dcb0d-aea9-4c37-89d6-fc79fe3ba77f",
  "reportedAt": "2026-02-21T21:43:22.399660Z",
  "date": "2026-02-21",
  "line": "RED",
  "destination": "HOWARD",
  "nextStop": "Fullerton",
  "carNumber": "2435",
  "runNumber": "902" // Optional, but helps identify the train and track patterns over time
}
```

------------------------------------------------------------------------

## ⚙️ Conventions

-   **Timestamps**: ISO-8601 UTC (`Instant.toString()`)
-   **Dates**: `yyyy-MM-dd`
-   **Report IDs**: `epochMillis#UUID`
-   **Expiration**: Reports may include a TTL (`expiresAt`) for
    automatic cleanup
-   **Line**: Standard CTA identifiers (`RED`, `BLUE`, `BROWN`,
    etc.)

------------------------------------------------------------------------

## 🧠 Design Goals

-   **Public transparency**
-   **Community-powered**
-   **Low friction**
-   **Developer friendly**
-   **Lightweight infrastructure**

------------------------------------------------------------------------

## 🔒 Moderation & Integrity

-   Basic validation on all submissions
-   Server-generated timestamps
-   Rate limiting to prevent spam
-   No personally identifiable information stored

------------------------------------------------------------------------

## ⚠️ Disclaimer

This project is not affiliated with the Chicago Transit Authority.\
All data is user-submitted and provided as-is.

------------------------------------------------------------------------

## 🧾 License

Licensed under the Apache License 2.0.\
Copyright © 2026 Logan Bailey Kulinski.

------------------------------------------------------------------------

*Built with ❤️ by [Logan Kulinski](https://lbku.net)*
