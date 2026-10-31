# CARESETU Backend — Architecture

## Purpose

REST API for **NP-NCD cancer referral and service tracking** (CARESETU): OTP auth, RBAC, geographic scope, patient pathway actions, programme KPIs, and audit-friendly responses.

## Stack

| Layer | Choice |
|-------|--------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5 |
| Persistence | MySQL 8 · Spring Data JPA · Flyway |
| Security | JWT (access + hashed refresh) · Spring Security · `@PreAuthorize` |
| Docs | springdoc OpenAPI / Swagger (non-prod) |
| Ops | Actuator `health` / `info` |

## Layered design

```
HTTP Controllers  →  Services  →  Repositories
        ↓               ↓
      DTOs         domain entities / enums
```

- Controllers accept/return DTOs only (no entity leakage).
- Services own pathway transitions, scope checks, and audit side-effects.
- Flyway owns schema; `ddl-auto: validate`.
- Correlation ID (`X-Request-Id`) in MDC for every request.

## Security model

- **Auth:** OTP send/verify → JWT access (~15m) + refresh (~7d).
- **RBAC:** roles × permissions; geographic filters (PHC / taluka / district / state).
- **VAPT posture:** validation, security headers, CORS allow-list, rate limits on auth endpoints, no stack traces to clients, demo OTP disableable in `prod`.
- **Demo OTP:** enabled in default profile (`123456`); disable via production profile + real SMS/DLT.

## Clinical pathway (API)

Typical happy path:

```
ASHA screening → stage CHO
CHO forward    → stage REVIEW
MO book        → stage BOOKED
Hospital attend / tests / oncology / close … (role actions)
```

Patient actions are exposed under `/api/v1/patients/{id}/…` (screen, forward, book, attend, report, oncology, consult, close, ack, field-visit, exit, treatment-start, rebook, etc.).

## Module map

| Package area | Responsibility |
|--------------|----------------|
| `controller` | Versioned REST (`/api/v1`) |
| `service` | Auth, patient pathway, worklist, dashboard, schemes |
| `repository` | JPA repositories |
| `domain` | Entities + enums (stage, risk, roles) |
| `dto` | Request/response contracts |
| `security` | JWT filters, OTP, CORS, rate limit |
| `config` | OpenAPI, security, Jackson |
| `exception` | `ApiException` + global handler (`ApiResponse` envelope) |

## Data & migrations

```
src/main/resources/db/migration/
  V1__init_rbac_auth.sql      # roles, users, demo staff mobiles
  V2__clinical_domain.sql     # patients, visits, tests, …
  V3__seed_demo_patients.sql  # Gandhinagar pilot sample patients
  V4__… V7__…                 # schemes, misses, super-admin, …
```

## Runtime topology (local)

```
FE :5174 ──► API :18080 (or :8080) ──► MySQL :3307 (docker compose)
```

## Companion frontend

SPA: **Caresetu-Fe** (React / Vite). See that repo’s `docs/ARCHITECTURE.md` and UI pathway scripts.
