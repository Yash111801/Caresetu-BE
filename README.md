# CARESETU Backend

Open-source API for **Cancer Referral and Service Tracking Utility** · NP-NCD Gujarat prototype.

**Java 21 · Spring Boot 3.5 · MySQL 8 · JWT · RBAC · Flyway · OpenAPI**

| Requirement | Location |
|-------------|----------|
| Complete source | this repository (`src/`) |
| Setup instructions | [below](#setup) |
| Architecture | [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) |
| Sample data & tests | [`docs/SAMPLE_DATA_AND_TESTS.md`](docs/SAMPLE_DATA_AND_TESTS.md) |
| License | [`LICENSE`](LICENSE) (MIT) |

Companion UI: **Caresetu-Fe** (React / Vite).

---

## Setup

### Prerequisites

- JDK 21 (`JAVA_HOME` pointing at OpenJDK 21)
- Docker (for MySQL)
- Maven wrapper included (`./mvnw`)

### Database

```bash
docker compose up -d
# MySQL listens on localhost:3307 (user/pass/db: caresetu)
```

### Run API

```bash
git clone https://github.com/Yash111801/Caresetu-BE.git Caresetu-BE
cd Caresetu-BE
export JAVA_HOME=…/openjdk@21   # adjust for your machine
./mvnw spring-boot:run
# default port 8080
```

If port 8080 is taken (common on shared machines):

```bash
./mvnw -q -DskipTests package
java -jar target/caresetu-*.jar --server.port=18080
```

- Health: http://localhost:18080/actuator/health  
- Swagger (non-prod): http://localhost:18080/swagger-ui.html  

Point the FE `.env` at the same base: `VITE_API_BASE_URL=http://localhost:18080/api/v1`.

---

## Demo logins (OTP `123456`)

| Role | Mobile |
|------|--------|
| **ADMIN** | **9999999999** |
| ASHA | 9876543210 |
| CHO | 9876543211 |
| MO | 9876543212 |
| HOSP | 9876543213 |
| PATH | 9876543214 |
| ONCO | 9876543215 |
| BLK / DIST / SNO / NAT | 9876543216–19 |

Sample patients and pathway stages are loaded by Flyway (`V3__seed_demo_patients.sql`). Details: [`docs/SAMPLE_DATA_AND_TESTS.md`](docs/SAMPLE_DATA_AND_TESTS.md).

---

## API surface (summary)

| Area | Notes |
|------|--------|
| Auth | `/api/v1/auth/otp/*`, `/refresh`, `/logout`, `/me` |
| Clinical pathway | screenings, CHO review, refer, attend, report, oncology, consult, close, ack, field-visit, exit, treatment-start |
| Programme | dashboard K1–K8, intervals, monthly audit, schemes, map |
| Ops | `/actuator/health`, OpenAPI `/swagger-ui.html` (non-prod) |

---

## Tests

```bash
./mvnw test
```

---

## Production notes

```bash
export SPRING_PROFILES_ACTIVE=prod
export JWT_SECRET='…≥64 chars…'
export DB_URL='jdbc:mysql://…'
export DB_USERNAME=…
export DB_PASSWORD=…
export CORS_ORIGINS='https://caresetu.example.gov.in'
java -jar caresetu.jar
```

`application-prod.yml` disables demo OTP and Swagger. Wire real SMS/IVRS (TRAI DLT) before go-live.

---

## License

MIT — see [`LICENSE`](LICENSE).
