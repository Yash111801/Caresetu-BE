-- Schemes, eligibility, ABHA, SMS language, consent, audit exceptions

ALTER TABLE patients
    ADD COLUMN abha_id VARCHAR(32) NULL AFTER mobile,
    ADD COLUMN sms_lang VARCHAR(8) NOT NULL DEFAULT 'gu' AFTER abha_id,
    ADD COLUMN income_band VARCHAR(40) NULL AFTER sms_lang,
    ADD COLUMN ration_card VARCHAR(40) NULL,
    ADD COLUMN has_pmjay_card TINYINT(1) NULL,
    ADD COLUMN is_esic TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN is_govt_employee TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN has_private_insurance TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN gps_consent TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN data_consent TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN who_class VARCHAR(80) NULL,
    ADD COLUMN entry_source VARCHAR(20) NOT NULL DEFAULT 'FIELD';

CREATE TABLE schemes (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(32)  NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    short_name      VARCHAR(80)  NOT NULL,
    level_label     VARCHAR(80)  NULL,
    cover_text      VARCHAR(255) NOT NULL,
    what_text       VARCHAR(255) NOT NULL,
    how_text        VARCHAR(255) NOT NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO schemes (code, name, short_name, level_label, cover_text, what_text, how_text) VALUES
('pmjay', 'PMJAY-MA (Ayushman Bharat + Mukhyamantri Amrutum)', 'PMJAY-MA', 'National + Gujarat',
 '₹10 lakh per family per year, cashless',
 'Cancer surgery, chemotherapy and radiotherapy at empanelled hospitals',
 'Show Ayushman / PMJAY-MA card or Aadhaar at the Arogya Mitra desk'),
('vay', 'Ayushman Vay Vandana (age 70+)', 'Vay Vandana', 'National',
 '₹5 lakh extra per year for the senior, any income',
 'Same hospitals and packages as PM-JAY',
 'Enrol with Aadhaar on the Ayushman app or at a CSC'),
('hmcpf', 'Health Minister’s Cancer Patient Fund (Rashtriya Arogya Nidhi)', 'HMCPF', 'National',
 'Up to ₹2 lakh (higher in emergency via Ministry)',
 'Cancer treatment at Regional Cancer Centres',
 'Apply through the Regional Cancer Centre with BPL proof'),
('esic', 'ESIC (insured workers)', 'ESIC', 'National',
 'Free treatment for the worker and family',
 'Treatment at ESIC hospitals and tie-up hospitals',
 'Show the ESIC e-Pehchan card'),
('govt', 'Government employee health cover (CGHS / state)', 'Govt employee', 'National / State',
 'As per service rules (cashless or reimbursement)',
 'Empanelled hospitals',
 'Show the employee health card'),
('free', 'Free screening & diagnosis at government hospitals (NP-NCD)', 'Govt free care', 'National',
 'Free or nominal-cost screening, tests and OPD',
 'PHC, CHC, district hospital, medical college, cancer centre',
 'No card needed'),
('insurance', 'Private health insurance', 'Insurance', '',
 'As per policy',
 'Network hospitals',
 'Show insurance card / policy');

CREATE TABLE audit_exceptions (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    area_code       VARCHAR(40)  NOT NULL,
    area_name       VARCHAR(120) NOT NULL,
    patient_id      BIGINT       NULL,
    issue_code      VARCHAR(40)  NOT NULL,
    issue_text      VARCHAR(500) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'Open',
    due_date        DATE         NULL,
    month_key       INT          NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_audit_month (month_key),
    INDEX idx_audit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed a few audit exceptions for prior month
INSERT INTO audit_exceptions (public_id, area_code, area_name, patient_id, issue_code, issue_text, status, due_date, month_key)
SELECT '66666666-6666-6666-6666-666666666001', p.phc_code, CONCAT('PHC ', p.phc_code), p.id,
       'MISSED_FOLLOWUP', 'Missed hospital visit without rebook within 7 days', 'Open',
       DATE_ADD(CURDATE(), INTERVAL 7 DAY),
       YEAR(CURDATE()) * 12 + MONTH(CURDATE()) - 2
FROM patients p WHERE p.uhid = 'CS-GNR-0008';

INSERT INTO audit_exceptions (public_id, area_code, area_name, patient_id, issue_code, issue_text, status, due_date, month_key)
SELECT '66666666-6666-6666-6666-666666666002', p.phc_code, CONCAT('PHC ', p.phc_code), p.id,
       'REPORT_DELAY', 'Diagnostic report overdue vs promised date', 'Reviewed',
       CURDATE(),
       YEAR(CURDATE()) * 12 + MONTH(CURDATE()) - 2
FROM patients p WHERE p.uhid = 'CS-GNR-0005';

INSERT INTO audit_exceptions (public_id, area_code, area_name, patient_id, issue_code, issue_text, status, due_date, month_key)
SELECT '66666666-6666-6666-6666-666666666003', 'DH', 'District Hospital', p.id,
       'POOL_OVERBOOK_RISK', 'Green-channel day near capacity with pending referrals', 'Corrected',
       CURDATE(),
       YEAR(CURDATE()) * 12 + MONTH(CURDATE()) - 2
FROM patients p WHERE p.uhid = 'CS-GNR-0003';
