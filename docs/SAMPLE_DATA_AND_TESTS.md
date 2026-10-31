# Sample data & test cases (Backend)

Aligned with the **running** CARESETU API: Flyway seeds on MySQL, JWT OTP auth, `WorklistService` action matrix, and the FE pathway that creates a patient then advances CHO → MO → BOOKED.

**Local stack**

```bash
docker compose up -d                 # MySQL :3307
./mvnw spring-boot:run               # or jar --server.port=18080
./mvnw test
```

Demo OTP (default profile): **`123456`**. Disable in `prod`.

---

## 1. How sample data is loaded

| Migration | Contents |
|-----------|----------|
| `V1__init_rbac_auth.sql` | Roles, permissions, role_permissions, **10 demo staff users** + role links |
| `V2__clinical_domain.sql` | Patients, bookings, tests, events, alerts, messages schema |
| `V3__seed_demo_patients.sql` | **10 patients**, bookings, diagnostics, events, alerts, SMS/IVRS |
| `V4`–`V6` | Schemes, misses / field contacts, miss ladder |
| `V7__super_admin_user.sql` | Super Admin `9999999999` / ADMIN |

Reset demo DB: `docker compose down -v && docker compose up -d`, then restart the API (Flyway re-applies).

---

## 2. Demo users

| Mobile | Name | Role | Scope |
|--------|------|------|-------|
| `9999999999` | CARESETU Super Admin | ADMIN | Unscoped admin |
| `9876543210` | Meena Rathod | ASHA | `phc_code=ADL` |
| `9876543211` | Kiran Patel | CHO | `phc_code=ADL` |
| `9876543212` | Dr A. Shah | MO | `phc_code=ADL` |
| `9876543213` | Dr P. Trivedi | HOSP | `facility_code=DH` |
| `9876543214` | Dr R. Rao | PATH | `facility_code=DH` |
| `9876543215` | Dr S. Iyer | ONCO | `facility_code=RCC` |
| `9876543216` | Dr J. Raval | BLK | taluka Gandhinagar |
| `9876543217` | District Programme Officer | DIST | district Gandhinagar |
| `9876543218` | State Nodal Officer | SNO | state GJ |
| `9876543219` | National Programme Officer | NAT | national |

### Auth smoke (curl)

```bash
DEVICE=$(uuidgen)   # or any UUID string
API=http://localhost:18080/api/v1

curl -sS -X POST "$API/auth/otp/send" \
  -H 'Content-Type: application/json' -H "X-Device-Id: $DEVICE" \
  -d '{"mobile":"9876543210"}'

curl -sS -X POST "$API/auth/otp/verify" \
  -H 'Content-Type: application/json' -H "X-Device-Id: $DEVICE" \
  -d "{\"mobile\":\"9876543210\",\"otp\":\"123456\",\"deviceId\":\"$DEVICE\"}"
# → data.accessToken, data.refreshToken, data.user
```

Use `Authorization: Bearer <accessToken>` on subsequent calls. Refresh via `POST /auth/otp` path sibling `/auth/refresh` (see OpenAPI). Rate limits apply on auth endpoints — prefer API login in automation.

---

## 3. Demo patients (pathway coverage)

| UHID | Name | Stage | Risk | Cancer | PHC / area | Downstream fixtures |
|------|------|-------|------|--------|------------|---------------------|
| CS-GNR-0001 | Kamla Ben | CHO | HIGH | oral | ADL | Alert → CHO |
| CS-GNR-0002 | Savita Patel | REVIEW | HIGH | breast | ADL | Alert → MO book |
| CS-GNR-0003 | Ramesh Thakor | BOOKED | MEDIUM | oral | UVS | ACTIVE booking DH today; SMS SENT; HOSP alert |
| CS-GNR-0004 | Bharti Solanki | MONITOR | LOW | cervical | PTH | `monitor_until` +90d |
| CS-GNR-0005 | Hansaben Raval | TESTED | HIGH | cervical | SAJ | Pending `Colposcopy + biopsy`; `report_due` |
| CS-GNR-0006 | Prakash Zala | ONCO_REQ | HIGH | oral | RKH | Biopsy DONE POSITIVE |
| CS-GNR-0007 | Naina Joshi | SPECIALIST | HIGH | breast | CHR | ONCOLOGY booking RCC today |
| CS-GNR-0008 | Dinesh Dabhi | MISSED | MEDIUM | oral | BHY | **No mobile**; MISSED booking; CRIT alert |
| CS-GNR-0009 | Geeta Prajapati | BOOKED | MEDIUM | cervical | BLV | Booking MCH +2d; IVRS QUEUED |
| CS-GNR-0010 | Sumanben Makwana | CLOSED | LOW | breast | STJ | `closed_reason` set |

Fixed `public_id`s use `22222222-…000N` for reproducible deep links from FE.

**Patient stages enum:** `MONITOR | CHO | REVIEW | BOOKED | MISSED | TESTED | ONCO_REQ | ONCO_MISSED | SPECIALIST | CLOSED`.

---

## 4. Working pathway (API behaviour)

### Happy path (create → next)

Same flow the FE script validates:

| # | Actor | Operation | Resulting `stage` |
|---|-------|-----------|-------------------|
| 1 | ASHA | Create screening (patient create + screen) | **CHO** |
| 2 | CHO | Forward / approve action | **REVIEW** |
| 3 | MO | Refer + book slot | **BOOKED** (+ booking row, events, messages as configured) |

### Action matrix (`WorklistService`)

Exact `act` codes returned on patient detail / worklist:

| Role | Stage / condition | Actions (`act`) |
|------|-------------------|-----------------|
| ASHA | MONITOR | `monitor` |
| ASHA/CHO | Miss ladder failed, no field feedback | `fieldvisit` |
| ASHA/CHO | Blank mobile, not CLOSED, not informed | `fieldvisit` |
| CHO | CHO | `approve` |
| MO | REVIEW | `refer` |
| MO | ONCO_REQ | `obook` |
| MO | MISSED / ONCO_MISSED | `rebook` |
| MO | Hospital entry, `mo_acked=false` | `ack` |
| HOSP | BOOKED, `visit_at` ≤ today | `attend`, `cantserve` |
| PATH | TESTED | `report`, `inconclusive` |
| ONCO | SPECIALIST, `onco_visit_at` ≤ today | `consult` |
| ONCO/MO | CLOSED + POSITIVE + !treatmentStarted | `txstart` |
| MO/HOSP/ONCO/CHO | not CLOSED | `exit` |

Wrong role or stage → action omitted (403 / business error if forced). Geographic `ScopeService` further filters list visibility.

---

## 5. Suggested API integration cases

| ID | Steps | Expect |
|----|-------|--------|
| A1 | OTP send+verify ASHA | `success`, tokens, role ASHA |
| A2 | ASHA create screening | 201/200; patient `stage=CHO`; event SCREENED |
| A3 | CHO `approve` on that id | `stage=REVIEW` |
| A4 | MO book (`refer`) | `stage=BOOKED`; booking ACTIVE |
| A5 | CHO opens BOOKED patient actions | no `refer` / `approve` |
| A6 | HOSP `attend` on CS-GNR-0003 (visit today) | attendance recorded; stage advances per service rules |
| A7 | PATH `report` on CS-GNR-0005 | test DONE + result codes |
| A8 | MO `obook` on CS-GNR-0006 | oncology booking / SPECIALIST path |
| A9 | Refresh token | new access; rotated refresh |
| A10 | OTP hammer | rate limit / cooldown respected |
| A11 | Cross-scope read | ASHA outside PHC → denied / empty |

OpenAPI: `/swagger-ui.html` (non-prod) lists request bodies for each patient action.

---

## 6. Automated unit tests

```bash
./mvnw test
```

| Class | Case | Asserts |
|-------|------|---------|
| `CaresetuApplicationTests` | Context load | Spring context starts |
| `WorklistServiceTest.ashaSeesMonitorActionOnMonitorStage` | MONITOR + ASHA | actions contain `monitor` |
| `WorklistServiceTest.hospSeesAttendAndCantServeOnBookedDueVisit` | BOOKED, visit yesterday, HOSP | `attend` + `cantserve` |
| `ApiExceptionTest` | Error helpers | Envelope / status mapping |

These encode the same action rules the FE relies on for Available actions.

---

## 7. End-to-end with frontend

From **Caresetu-Fe** (API + FE up):

```bash
npm run test:pathway
# 01 ASHA home → 02 create (CHO) → 03 CHO forward (REVIEW) → 04 MO book (BOOKED) → 05 timeline/print
```

Full route walk: `npm run test:ui`. Detailed UI expectations: FE `docs/SAMPLE_DATA_AND_TESTS.md`.
