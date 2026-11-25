# 🚭 CTA Smokers API

> [!IMPORTANT]
> **This API is no longer active.** As of November 2026,
> https://ctasmokers.com is not taking any more reports. To report an
> issue on the CTA, use
> [NITA Ride Safe](https://nita.illinois.gov/safety/app), the new
> reporting app.
>
> https://ctasmokers.com still shows an archive of the report counts and
> includes a link to download them as a zip file.
>
> `api.ctasmokers.com` no longer works. The documentation below is kept
> for reference.

Crowdsourced smoking reports on Chicago CTA trains --- simple,
transparent, and public.

The **CTA Smokers API** powered https://ctasmokers.com and provided a
clean REST interface for reporting and retrieving smoking incidents on
CTA trains. It was built to help riders make informed decisions in real
time.

------------------------------------------------------------------------

## 🚇 Overview

The API provides structured access to community-submitted smoking
reports:

- Submit reports with line, car number, run number, and direction
- Retrieve currently active reports, newest first
- ISO-8601 timestamps and predictable JSON schema
- Designed for dashboards, mobile apps, and data analysis

------------------------------------------------------------------------

## 📚 Endpoints

### Smoking Reports

- `POST /api/cta/reports/smoking` — Submit a new smoking report.
- `GET /api/cta/reports/smoking/active` — List active reports, newest first (`limit` 1 to 100, default 25, and `cursor`).
- `GET /api/cta/reports/smoking/{carNumber}/{line}` — Retrieve the active report for a car number and line.

To get the next page of active reports, pass the `nextCursor` value from the response back unchanged as the `cursor` query parameter, until `nextCursor` is `null`. When the last page holds exactly `limit` reports, `nextCursor` can still be non-null, and the next request returns an empty page with `nextCursor` set to `null`.

### Smoking Report Counts

- `GET /api/cta/reports/smoking/counts/{line}/day/{date}` — Daily report count (e.g. `2026-05-10`).
- `GET /api/cta/reports/smoking/counts/{line}/week/{yearWeek}` — Weekly report count (e.g. `2026-W13`).
- `GET /api/cta/reports/smoking/counts/{line}/month/{yearMonth}` — Monthly report count (e.g. `2026-03`).
- `GET /api/cta/reports/smoking/counts/{line}/month/{yearMonth}/days` — Daily report counts for a month (e.g. `2026-05`).
- `GET /api/cta/reports/smoking/counts/{line}/year/{year}` — Yearly report count (e.g. `2026`).
- `GET /api/cta/reports/smoking/counts/{line}/all-time` — All-time report count.

Days, months, and years use Chicago time (`America/Chicago`), and weeks are ISO weeks. Week parsing is lenient because it follows `YearWeek.parse` from the ThreeTen-Extra library: a lowercase `w` is accepted, and week 53 of a year with only 52 weeks is read as week 1 of the next year (`2025-W53` is read as `2026-W01`). A period with no reports returns a `reportCount` of `0` (or an empty `days` list), not `404 Not Found`.

------------------------------------------------------------------------

## 🗂 Data Model

### Smoking Report

``` json
{
  "reportedAt": "2026-02-21T21:00:22.399660Z",
  "expiresAt": "2026-02-21T21:30:22.399660Z",
  "line": "RED",
  "destinationId": "40900",
  "nextStationId": "41220",
  "carNumber": "2435",
  "runNumber": "902" // Optional, but helps identify the train and track patterns over time
}
```

### Smoking Report Page

``` json
{
  "reports": [ /* Smoking Reports */ ],
  "nextCursor": "c2FtcGxlLWN1cnNvcg" // null on the last page; treat as an opaque token
}
```

### Smoking Report Count

``` json
{
  "reportCount": 42
}
```

### Smoking Report Daily Counts

``` json
{
  "days": [
    { "date": "2026-02-21", "reportCount": 3 },
    { "date": "2026-02-28", "reportCount": 7 }
  ]
}
```

------------------------------------------------------------------------

## ⚙️ Conventions

- **Timestamps**: ISO-8601 UTC (`Instant.toString()`)
- **Dates**: `yyyy-MM-dd`
- **Expiration**: Reports expire at `expiresAt`, and expired reports are no longer returned
- **Line**: Standard CTA identifiers (`RED`, `BLUE`, `BROWN`, etc.)
- **Destination/Next Station IDs**: CTA's numeric codes for stations,
found [here](https://data.cityofchicago.org/resource/8pix-ypme.json)
  - The smoking report API makes use of the `map_id` field

------------------------------------------------------------------------

## 🧠 Design Goals

- **Public transparency**
- **Community-powered**
- **Low friction**
- **Developer friendly**
- **Lightweight infrastructure**

------------------------------------------------------------------------

## 🔒 Moderation & Integrity

- Basic validation on all submissions
- Duplicate active reports for the same car number and line are rejected
- Server-generated timestamps
- Rate limiting to prevent spam
- No personally identifiable information stored

------------------------------------------------------------------------

## ⚠️ Disclaimer

This project is not affiliated with the Chicago Transit Authority or
NITA.\
All data is user-submitted and provided as-is.

------------------------------------------------------------------------

## 🧾 License

Licensed under the Apache License 2.0.\
Copyright © 2026 Logan Bailey Kulinski.

------------------------------------------------------------------------

*Built with ❤️ by [Logan Kulinski](https://lbku.net)*
