# Methodology and process for implementation

Companion to the frontend doc. Clinical pathway includes **HOSP (hospital doctor)**, **PATH (Diagnostics)**, and **ONCO (Specialist)** — not only booking.

See **[Caresetu-FE `docs/METHODOLOGY.md`](https://github.com/Yash111801/Caresetu-FE/blob/main/docs/METHODOLOGY.md)** for full Mermaid flowcharts, swimlanes, and slide-ready diagrams.

## Process summary

1. **Domain** — roles ASHA → CHO → MO → HOSP → PATH → ONCO; stages through CLOSED  
2. **API first** — JWT/OTP, RBAC/scope, pathway services, Flyway seed  
3. **SPA** — role-aware UI driven by `WorklistService` actions  
4. **Validate** — `./mvnw test` + FE pathway + seeded HOSP/PATH/ONCO cases  
5. **Docs** — README, architecture, sample data/tests, methodology, MIT  

## Clinical flow (API)

```
ASHA → CHO → REVIEW → MO book → BOOKED
  → HOSP attend → TESTED
  → PATH report → ONCO_REQ | CLOSED
  → MO oncology book → SPECIALIST
  → ONCO consult → CLOSED
```
