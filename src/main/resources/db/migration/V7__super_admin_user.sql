-- Demo Super Admin (system administrator)
INSERT INTO users (public_id, mobile, full_name, facility_code, phc_code, taluka, district, state_code)
VALUES (
  '11111111-1111-1111-1111-111111111099',
  '9999999999',
  'CARESETU Super Admin',
  NULL, NULL, NULL, NULL, NULL
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.code = 'ADMIN'
WHERE u.mobile = '9999999999';
