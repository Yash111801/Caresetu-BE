-- Missed-visit ladder, field tasks, treatment/exit extras

CREATE TABLE patient_misses (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id      BIGINT       NOT NULL,
    facility_code   VARCHAR(16)  NOT NULL,
    missed_at       DATE         NOT NULL,
    onco            TINYINT(1)   NOT NULL DEFAULT 0,
    escalate_at     DATE         NOT NULL,
    call1_answered  TINYINT(1)   NOT NULL DEFAULT 0,
    call2_answered  TINYINT(1)   NOT NULL DEFAULT 0,
    call3_answered  TINYINT(1)   NOT NULL DEFAULT 0,
    field_feedback  VARCHAR(500) NULL,
    field_at        TIMESTAMP    NULL,
    action_at       TIMESTAMP    NULL,
    resolved_at     TIMESTAMP    NULL,
    resolve_how     VARCHAR(80)  NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_miss_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_miss_patient (patient_id),
    INDEX idx_miss_open (resolved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE field_tasks (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    patient_id      BIGINT       NOT NULL,
    assignee_role   VARCHAR(32)  NOT NULL,
    kind            VARCHAR(32)  NOT NULL,
    task_text       VARCHAR(500) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    feedback        VARCHAR(500) NULL,
    completed_at    TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ft_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_ft_role (assignee_role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contact_attempts (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    patient_id      BIGINT       NOT NULL,
    channel         VARCHAR(16)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    attempt_no      INT          NOT NULL DEFAULT 1,
    notes           VARCHAR(255) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ca_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_ca_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE patients
    ADD COLUMN treatment_started TINYINT(1) NOT NULL DEFAULT 0 AFTER treatment_plan,
    ADD COLUMN exit_reason VARCHAR(120) NULL AFTER closed_reason,
    ADD COLUMN informed_at TIMESTAMP NULL AFTER exit_reason,
    ADD COLUMN assessed_at TIMESTAMP NULL AFTER informed_at,
    ADD COLUMN consult_at TIMESTAMP NULL AFTER assessed_at;

-- Seed miss for existing MISSED patient
INSERT INTO patient_misses (patient_id, facility_code, missed_at, onco, escalate_at, call1_answered, call2_answered, call3_answered)
SELECT p.id, 'DH', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 0, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 7 DAY), 0, 0, 0
FROM patients p WHERE p.uhid = 'CS-GNR-0008';

INSERT INTO field_tasks (public_id, patient_id, assignee_role, kind, task_text, status)
SELECT '77777777-7777-7777-7777-777777777001', p.id, 'ASHA', 'nophone',
       'No mobile number · tell the patient about the missed DH appointment', 'OPEN'
FROM patients p WHERE p.uhid = 'CS-GNR-0008';

INSERT INTO contact_attempts (patient_id, channel, status, attempt_no, notes)
SELECT p.id, 'IVRS', 'Unanswered', 1, 'Missed-visit ladder day 0'
FROM patients p WHERE p.uhid = 'CS-GNR-0008';
INSERT INTO contact_attempts (patient_id, channel, status, attempt_no, notes)
SELECT p.id, 'SMS', 'Delivered', 1, 'Missed-visit reminder SMS'
FROM patients p WHERE p.uhid = 'CS-GNR-0008';
