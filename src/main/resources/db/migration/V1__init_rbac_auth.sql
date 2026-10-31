-- CARESETU V1: users, RBAC, OTP sessions, refresh tokens, audit

CREATE TABLE roles (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(32)  NOT NULL UNIQUE,
    title         VARCHAR(100) NOT NULL,
    description   VARCHAR(255) NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE permissions (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    code          VARCHAR(80)  NOT NULL UNIQUE,
    description   VARCHAR(255) NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE role_permissions (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    public_id       VARCHAR(36)  NOT NULL UNIQUE,
    mobile          VARCHAR(15)  NOT NULL UNIQUE,
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NULL UNIQUE,
    password_hash   VARCHAR(255) NULL,
    facility_code   VARCHAR(32)  NULL,
    phc_code        VARCHAR(32)  NULL,
    taluka          VARCHAR(100) NULL,
    district        VARCHAR(100) NULL,
    state_code      VARCHAR(10)  NULL,
    active          TINYINT(1)   NOT NULL DEFAULT 1,
    locked_until    TIMESTAMP    NULL,
    failed_attempts INT          NOT NULL DEFAULT 0,
    last_login_at   TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_mobile (mobile),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE otp_challenges (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    mobile          VARCHAR(15) NOT NULL,
    otp_hash        VARCHAR(255) NOT NULL,
    expires_at      TIMESTAMP   NOT NULL,
    attempts        INT         NOT NULL DEFAULT 0,
    consumed        TINYINT(1)  NOT NULL DEFAULT 0,
    request_ip      VARCHAR(45) NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_mobile (mobile),
    INDEX idx_otp_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE refresh_tokens (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    device_id       VARCHAR(128) NULL,
    expires_at      TIMESTAMP    NOT NULL,
    revoked         TINYINT(1)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_rt_user (user_id),
    INDEX idx_rt_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_user_id   BIGINT       NULL,
    action          VARCHAR(80)  NOT NULL,
    resource        VARCHAR(120) NULL,
    resource_id     VARCHAR(64)  NULL,
    ip_address      VARCHAR(45)  NULL,
    user_agent      VARCHAR(255) NULL,
    success         TINYINT(1)   NOT NULL DEFAULT 1,
    detail          VARCHAR(500) NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_actor (actor_user_id),
    INDEX idx_audit_action (action),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed roles (prototype roles)
INSERT INTO roles (code, title, description) VALUES
('ASHA',  'ASHA worker', 'Accredited Social Health Activist'),
('CHO',   'Community Health Officer', 'Community Health Officer'),
('MO',    'PHC medical officer', 'PHC Medical Officer'),
('HOSP',  'Hospital doctor', 'Hospital doctor'),
('PATH',  'Diagnostic', 'Diagnostic / pathology'),
('ONCO',  'Cancer specialist', 'Oncology specialist'),
('BLK',   'Taluka Health Officer', 'Taluka / Block Health Officer'),
('DIST',  'District Programme Officer', 'District Programme Officer, NP-NCD'),
('SNO',   'State Nodal Officer', 'State Nodal Officer, NP-NCD'),
('NAT',   'National Officer', 'National Programme Officer, NP-NCD'),
('ADMIN', 'System Admin', 'System administrator');

-- Seed permissions
INSERT INTO permissions (code, description) VALUES
('AUTH_LOGIN', 'Authenticate'),
('PATIENT_READ', 'View patients in scope'),
('PATIENT_WRITE', 'Create/update patients'),
('SCREENING_WRITE', 'Submit screening'),
('REFERRAL_WRITE', 'Create referrals / bookings'),
('APPOINTMENT_READ', 'View appointments'),
('APPOINTMENT_WRITE', 'Manage appointments'),
('REPORT_WRITE', 'Enter diagnostic reports'),
('DASHBOARD_PHC', 'PHC dashboard'),
('DASHBOARD_BLOCK', 'Block dashboard'),
('DASHBOARD_DISTRICT', 'District dashboard'),
('DASHBOARD_STATE', 'State dashboard'),
('DASHBOARD_NATIONAL', 'National dashboard'),
('ALERT_READ', 'View alerts'),
('MAP_READ', 'View map / heat map'),
('AUDIT_READ', 'View audit logs'),
('USER_ADMIN', 'Manage users and roles');

-- Role → permission mapping
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ASHA' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','PATIENT_WRITE','SCREENING_WRITE','ALERT_READ','MAP_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'CHO' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','PATIENT_WRITE','SCREENING_WRITE','ALERT_READ','MAP_READ','DASHBOARD_PHC');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'MO' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','PATIENT_WRITE','SCREENING_WRITE','REFERRAL_WRITE','APPOINTMENT_READ','ALERT_READ','MAP_READ','DASHBOARD_PHC');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'HOSP' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','PATIENT_WRITE','REFERRAL_WRITE','APPOINTMENT_READ','APPOINTMENT_WRITE','ALERT_READ','MAP_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'PATH' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','REPORT_WRITE','ALERT_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ONCO' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','APPOINTMENT_READ','APPOINTMENT_WRITE','REFERRAL_WRITE','ALERT_READ','MAP_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'BLK' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','DASHBOARD_BLOCK','ALERT_READ','MAP_READ','APPOINTMENT_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DIST' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','DASHBOARD_DISTRICT','ALERT_READ','MAP_READ','APPOINTMENT_READ','AUDIT_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'SNO' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','DASHBOARD_STATE','ALERT_READ','MAP_READ','AUDIT_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'NAT' AND p.code IN ('AUTH_LOGIN','PATIENT_READ','DASHBOARD_NATIONAL','ALERT_READ','MAP_READ','AUDIT_READ');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'ADMIN';

-- Demo users (mobile OTP login). public_id = fixed UUIDs for reproducibility
INSERT INTO users (public_id, mobile, full_name, facility_code, phc_code, taluka, district, state_code) VALUES
('11111111-1111-1111-1111-111111111001', '9876543210', 'Meena Rathod', NULL, 'ADL', 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111002', '9876543211', 'Kiran Patel', NULL, 'ADL', 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111003', '9876543212', 'Dr A. Shah', NULL, 'ADL', 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111004', '9876543213', 'Dr P. Trivedi', 'DH', NULL, 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111005', '9876543214', 'Dr R. Rao', 'DH', NULL, 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111006', '9876543215', 'Dr S. Iyer', 'RCC', NULL, 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111007', '9876543216', 'Dr J. Raval', NULL, NULL, 'Gandhinagar', 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111008', '9876543217', 'District Programme Officer', NULL, NULL, NULL, 'Gandhinagar', 'GJ'),
('11111111-1111-1111-1111-111111111009', '9876543218', 'State Nodal Officer', NULL, NULL, NULL, NULL, 'GJ'),
('11111111-1111-1111-1111-111111111010', '9876543219', 'National Programme Officer', NULL, NULL, NULL, NULL, NULL);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ASHA' WHERE u.mobile = '9876543210';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'CHO' WHERE u.mobile = '9876543211';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'MO' WHERE u.mobile = '9876543212';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'HOSP' WHERE u.mobile = '9876543213';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'PATH' WHERE u.mobile = '9876543214';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'ONCO' WHERE u.mobile = '9876543215';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'BLK' WHERE u.mobile = '9876543216';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'DIST' WHERE u.mobile = '9876543217';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'SNO' WHERE u.mobile = '9876543218';
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u JOIN roles r ON r.code = 'NAT' WHERE u.mobile = '9876543219';
