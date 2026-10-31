-- CARESETU V2: facilities, PHCs, patients, bookings, events, alerts

CREATE TABLE facilities (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(16)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    short_name      VARCHAR(80)  NOT NULL,
    facility_type   VARCHAR(80)  NOT NULL,
    address_hint    VARCHAR(255) NULL,
    hours           VARCHAR(120) NULL,
    latitude        DECIMAL(10,7) NULL,
    longitude       DECIMAL(10,7) NULL,
    daily_pool_cap  INT          NOT NULL DEFAULT 10,
    report_days     INT          NOT NULL DEFAULT 3,
    services_csv    VARCHAR(120) NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE phcs (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(16)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    taluka          VARCHAR(100) NULL,
    district        VARCHAR(100) NOT NULL DEFAULT 'Gandhinagar',
    state_code      VARCHAR(10)  NOT NULL DEFAULT 'GJ',
    latitude        DECIMAL(10,7) NULL,
    longitude       DECIMAL(10,7) NULL,
    backup_team     TINYINT(1)   NOT NULL DEFAULT 0,
    active          TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patients (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id           VARCHAR(36)  NOT NULL UNIQUE,
    uhid                VARCHAR(32)  NOT NULL UNIQUE,
    full_name           VARCHAR(150) NOT NULL,
    age                 INT          NOT NULL,
    sex                 VARCHAR(16)  NOT NULL,
    mobile              VARCHAR(15)  NULL,
    village             VARCHAR(120) NULL,
    phc_code            VARCHAR(16)  NOT NULL,
    taluka              VARCHAR(100) NULL,
    district            VARCHAR(100) NOT NULL DEFAULT 'Gandhinagar',
    state_code          VARCHAR(10)  NOT NULL DEFAULT 'GJ',
    cancer_type         VARCHAR(16)  NOT NULL,
    presentation        VARCHAR(40)  NOT NULL,
    stage               VARCHAR(32)  NOT NULL,
    risk_level          VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    screened_by_user_id BIGINT       NULL,
    screened_by_name    VARCHAR(150) NULL,
    screened_by_role    VARCHAR(32)  NULL,
    screened_at         TIMESTAMP    NULL,
    finding_notes       VARCHAR(1000) NULL,
    assigned_fac_code   VARCHAR(16)  NULL,
    visit_at            DATE         NULL,
    visit_window        VARCHAR(40)  NULL,
    test_fac_code       VARCHAR(16)  NULL,
    report_due          DATE         NULL,
    report_at           TIMESTAMP    NULL,
    test_result         VARCHAR(40)  NULL,
    onco_visit_at       DATE         NULL,
    treatment_plan      VARCHAR(120) NULL,
    closed_reason       VARCHAR(120) NULL,
    mo_acked            TINYINT(1)   NOT NULL DEFAULT 0,
    counselled          TINYINT(1)   NOT NULL DEFAULT 0,
    monitor_until       DATE         NULL,
    latitude            DECIMAL(10,7) NULL,
    longitude           DECIMAL(10,7) NULL,
    active              TINYINT(1)   NOT NULL DEFAULT 1,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pat_stage (stage),
    INDEX idx_pat_phc (phc_code),
    INDEX idx_pat_fac (assigned_fac_code),
    INDEX idx_pat_type (cancer_type),
    INDEX idx_pat_visit (visit_at),
    CONSTRAINT fk_pat_phc FOREIGN KEY (phc_code) REFERENCES phcs(code),
    CONSTRAINT fk_pat_screener FOREIGN KEY (screened_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE patient_events (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id      BIGINT       NOT NULL,
    event_type      VARCHAR(40)  NOT NULL,
    message         VARCHAR(500) NOT NULL,
    actor_user_id   BIGINT       NULL,
    actor_name      VARCHAR(150) NULL,
    actor_role      VARCHAR(32)  NULL,
    meta_json       JSON         NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pe_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_pe_patient (patient_id),
    INDEX idx_pe_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bookings (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    patient_id      BIGINT       NOT NULL,
    facility_code   VARCHAR(16)  NOT NULL,
    booking_date    DATE         NOT NULL,
    window_label    VARCHAR(40)  NOT NULL,
    place_no        INT          NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    booked_by_user_id BIGINT     NULL,
    booked_by_name  VARCHAR(150) NULL,
    booking_kind    VARCHAR(20)  NOT NULL DEFAULT 'HOSPITAL',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_book_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_book_fac FOREIGN KEY (facility_code) REFERENCES facilities(code),
    INDEX idx_book_fac_date (facility_code, booking_date, status),
    INDEX idx_book_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE diagnostic_tests (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id      BIGINT       NOT NULL,
    test_name       VARCHAR(120) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    result_code     VARCHAR(40)  NULL,
    result_notes    VARCHAR(500) NULL,
    reported_at     TIMESTAMP    NULL,
    reported_by     BIGINT       NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dt_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_dt_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE alerts (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    title           VARCHAR(200) NOT NULL,
    body            VARCHAR(500) NOT NULL,
    severity        VARCHAR(16)  NOT NULL DEFAULT 'INFO',
    audience_role   VARCHAR(32)  NULL,
    audience_phc    VARCHAR(16)  NULL,
    audience_fac    VARCHAR(16)  NULL,
    audience_district VARCHAR(100) NULL,
    patient_id      BIGINT       NULL,
    read_flag       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_alert_role (audience_role),
    INDEX idx_alert_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE messages (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    channel         VARCHAR(16)  NOT NULL,
    direction       VARCHAR(16)  NOT NULL DEFAULT 'OUT',
    mobile          VARCHAR(15)  NULL,
    subject         VARCHAR(200) NULL,
    body            VARCHAR(1000) NOT NULL,
    language        VARCHAR(8)   NOT NULL DEFAULT 'en',
    patient_id      BIGINT       NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'QUEUED',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_msg_patient (patient_id),
    INDEX idx_msg_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed facilities (Gandhinagar pilot)
INSERT INTO facilities (code, name, short_name, facility_type, address_hint, hours, latitude, longitude, daily_pool_cap, report_days, services_csv) VALUES
('DH',   'District Hospital, Gandhinagar', 'District Hospital', 'District hospital', 'Sector 12 · Gate 2, Block B, Room 12', 'Mon–Sat, 10:00–12:00', 23.2235000, 72.6500000, 10, 3, 'oral,breast,cervical'),
('MCH',  'Medical College Hospital, Gandhinagar', 'Medical College', 'Medical college', 'OPD Building, Room 7', 'Mon–Sat, 09:30–12:30', 23.2150000, 72.6360000, 10, 4, 'oral,breast,cervical'),
('CHCK', 'CHC Kalol', 'CHC Kalol', 'Community health centre', 'NCD clinic, ground floor', 'Mon–Sat, 09:00–13:00', 23.2460000, 72.4950000, 10, 5, 'cervical'),
('RCC',  'Regional Cancer Centre, Ahmedabad', 'Cancer Centre', 'Regional cancer centre', 'New OPD, Counter 4', 'Mon–Fri, 10:00–13:00', 23.0500000, 72.5700000, 10, 3, 'oncology');

INSERT INTO phcs (code, name, taluka, district, latitude, longitude, backup_team) VALUES
('ADL', 'PHC Adalaj', 'Gandhinagar', 'Gandhinagar', 23.1680000, 72.5770000, 0),
('UVS', 'PHC Unvarsad', 'Gandhinagar', 'Gandhinagar', 23.1900000, 72.6200000, 0),
('PTH', 'PHC Pethapur', 'Gandhinagar', 'Gandhinagar', 23.2100000, 72.6400000, 0),
('SAJ', 'PHC Saij', 'Kalol', 'Gandhinagar', 23.2500000, 72.5000000, 0),
('STJ', 'PHC Santej', 'Kalol', 'Gandhinagar', 23.2600000, 72.4800000, 0),
('RKH', 'PHC Rakhiyal', 'Dehgam', 'Gandhinagar', 23.1700000, 72.8200000, 0),
('BHY', 'PHC Bahiyel', 'Dehgam', 'Gandhinagar', 23.1500000, 72.8500000, 0),
('CHR', 'PHC Charada', 'Mansa', 'Gandhinagar', 23.4300000, 72.6600000, 0),
('BLV', 'PHC Balva', 'Mansa', 'Gandhinagar', 23.4500000, 72.6800000, 0),
('DBK', 'District backup team', NULL, 'Gandhinagar', 23.2235000, 72.6500000, 1);
