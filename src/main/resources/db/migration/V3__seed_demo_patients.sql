-- Demo patients covering major pathway stages (Gandhinagar pilot)

INSERT INTO patients (
  public_id, uhid, full_name, age, sex, mobile, village, phc_code, taluka, district, state_code,
  cancer_type, presentation, stage, risk_level, screened_by_name, screened_by_role, screened_at,
  finding_notes, assigned_fac_code, visit_at, visit_window, counselled, mo_acked, latitude, longitude
) VALUES
('22222222-2222-2222-2222-222222222001', 'CS-GNR-0001', 'Kamla Ben', 46, 'F', '9988776601', 'Adalaj', 'ADL', 'Gandhinagar', 'Gandhinagar', 'GJ',
 'oral', 'symptomatic', 'CHO', 'HIGH', 'Meena Rathod', 'ASHA', TIMESTAMPADD(DAY, -2, CURRENT_TIMESTAMP),
 'White patch on left buccal mucosa · ASHA screening', NULL, NULL, NULL, 0, 0, 23.1685, 72.5772),
('22222222-2222-2222-2222-222222222002', 'CS-GNR-0002', 'Savita Patel', 52, 'F', '9988776602', 'Adalaj', 'ADL', 'Gandhinagar', 'Gandhinagar', 'GJ',
 'breast', 'symptomatic', 'REVIEW', 'HIGH', 'Kiran Patel', 'CHO', TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP),
 'Palpable lump upper outer quadrant · CHO confirmed', NULL, NULL, NULL, 1, 0, 23.1690, 72.5780),
('22222222-2222-2222-2222-222222222003', 'CS-GNR-0003', 'Ramesh Thakor', 58, 'M', '9988776603', 'Unvarsad', 'UVS', 'Gandhinagar', 'Gandhinagar', 'GJ',
 'oral', 'symptomatic', 'BOOKED', 'MEDIUM', 'Dr A. Shah', 'MO', TIMESTAMPADD(DAY, -5, CURRENT_TIMESTAMP),
 'Non-healing ulcer · booked District Hospital', 'DH', CURDATE(), '10:00–12:00', 1, 1, 23.1905, 72.6205),
('22222222-2222-2222-2222-222222222004', 'CS-GNR-0004', 'Bharti Solanki', 44, 'F', '9988776604', 'Pethapur', 'PTH', 'Gandhinagar', 'Gandhinagar', 'GJ',
 'cervical', 'asymptomatic', 'MONITOR', 'LOW', 'Meena Rathod', 'ASHA', TIMESTAMPADD(DAY, -10, CURRENT_TIMESTAMP),
 'VIA negative · counsel and monitor', NULL, NULL, NULL, 0, 0, 23.2102, 72.6401),
('22222222-2222-2222-2222-222222222005', 'CS-GNR-0005', 'Hansaben Raval', 49, 'F', '9988776605', 'Saij', 'SAJ', 'Kalol', 'Gandhinagar', 'GJ',
 'cervical', 'symptomatic', 'TESTED', 'HIGH', 'Dr A. Shah', 'MO', TIMESTAMPADD(DAY, -12, CURRENT_TIMESTAMP),
 'VIA positive · colposcopy samples at CHC Kalol', 'CHCK', DATE_SUB(CURDATE(), INTERVAL 2 DAY), '09:00–13:00', 1, 1, 23.2502, 72.5005),
('22222222-2222-2222-2222-222222222006', 'CS-GNR-0006', 'Prakash Zala', 61, 'M', '9988776606', 'Rakhiyal', 'RKH', 'Dehgam', 'Gandhinagar', 'GJ',
 'oral', 'symptomatic', 'ONCO_REQ', 'HIGH', 'Dr P. Trivedi', 'HOSP', TIMESTAMPADD(DAY, -20, CURRENT_TIMESTAMP),
 'Biopsy positive for squamous cell carcinoma', 'DH', DATE_SUB(CURDATE(), INTERVAL 8 DAY), '10:00–12:00', 1, 1, 23.1705, 72.8205),
('22222222-2222-2222-2222-222222222007', 'CS-GNR-0007', 'Naina Joshi', 55, 'F', '9988776607', 'Charada', 'CHR', 'Mansa', 'Gandhinagar', 'GJ',
 'breast', 'symptomatic', 'SPECIALIST', 'HIGH', 'Dr A. Shah', 'MO', TIMESTAMPADD(DAY, -25, CURRENT_TIMESTAMP),
 'Confirmed malignancy · oncology consult due', 'RCC', NULL, NULL, 1, 1, 23.4302, 72.6602),
('22222222-2222-2222-2222-222222222008', 'CS-GNR-0008', 'Dinesh Dabhi', 50, 'M', NULL, 'Bahiyel', 'BHY', 'Dehgam', 'Gandhinagar', 'GJ',
 'oral', 'symptomatic', 'MISSED', 'MEDIUM', 'Dr A. Shah', 'MO', TIMESTAMPADD(DAY, -15, CURRENT_TIMESTAMP),
 'Missed hospital visit · no mobile · ASHA follow-up', 'DH', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00–12:00', 1, 1, 23.1505, 72.8502),
('22222222-2222-2222-2222-222222222009', 'CS-GNR-0009', 'Geeta Prajapati', 47, 'F', '9988776609', 'Balva', 'BLV', 'Mansa', 'Gandhinagar', 'GJ',
 'cervical', 'symptomatic', 'BOOKED', 'MEDIUM', 'Dr A. Shah', 'MO', TIMESTAMPADD(DAY, -4, CURRENT_TIMESTAMP),
 'Booked Medical College for further tests', 'MCH', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30–12:30', 1, 1, 23.4502, 72.6802),
('22222222-2222-2222-2222-222222222010', 'CS-GNR-0010', 'Sumanben Makwana', 53, 'F', '9988776610', 'Santej', 'STJ', 'Kalol', 'Gandhinagar', 'GJ',
 'breast', 'symptomatic', 'CLOSED', 'LOW', 'Dr S. Iyer', 'ONCO', TIMESTAMPADD(DAY, -60, CURRENT_TIMESTAMP),
 'Not cancer on specialist review · closed', 'RCC', DATE_SUB(CURDATE(), INTERVAL 40 DAY), '10:00–13:00', 1, 1, 23.2602, 72.4805);

UPDATE patients SET
  test_fac_code = 'CHCK',
  report_due = DATE_ADD(CURDATE(), INTERVAL 3 DAY),
  test_result = NULL
WHERE uhid = 'CS-GNR-0005';

UPDATE patients SET
  test_fac_code = 'DH',
  report_at = TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP),
  test_result = 'POSITIVE',
  report_due = DATE_SUB(CURDATE(), INTERVAL 3 DAY)
WHERE uhid = 'CS-GNR-0006';

UPDATE patients SET
  onco_visit_at = CURDATE(),
  visit_window = '10:00–13:00',
  assigned_fac_code = 'RCC',
  test_result = 'POSITIVE'
WHERE uhid = 'CS-GNR-0007';

UPDATE patients SET
  monitor_until = DATE_ADD(CURDATE(), INTERVAL 90 DAY),
  closed_reason = NULL
WHERE uhid = 'CS-GNR-0004';

UPDATE patients SET closed_reason = 'Not cancer on review' WHERE uhid = 'CS-GNR-0010';

INSERT INTO bookings (public_id, patient_id, facility_code, booking_date, window_label, place_no, status, booked_by_name, booking_kind)
SELECT '33333333-3333-3333-3333-333333333001', p.id, 'DH', CURDATE(), '10:00–12:00', 3, 'ACTIVE', 'Dr A. Shah', 'HOSPITAL'
FROM patients p WHERE p.uhid = 'CS-GNR-0003';

INSERT INTO bookings (public_id, patient_id, facility_code, booking_date, window_label, place_no, status, booked_by_name, booking_kind)
SELECT '33333333-3333-3333-3333-333333333002', p.id, 'MCH', DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30–12:30', 2, 'ACTIVE', 'Dr A. Shah', 'HOSPITAL'
FROM patients p WHERE p.uhid = 'CS-GNR-0009';

INSERT INTO bookings (public_id, patient_id, facility_code, booking_date, window_label, place_no, status, booked_by_name, booking_kind)
SELECT '33333333-3333-3333-3333-333333333003', p.id, 'DH', DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00–12:00', 5, 'MISSED', 'Dr A. Shah', 'HOSPITAL'
FROM patients p WHERE p.uhid = 'CS-GNR-0008';

INSERT INTO bookings (public_id, patient_id, facility_code, booking_date, window_label, place_no, status, booked_by_name, booking_kind)
SELECT '33333333-3333-3333-3333-333333333004', p.id, 'RCC', CURDATE(), '10:00–13:00', 1, 'ACTIVE', 'Dr A. Shah', 'ONCOLOGY'
FROM patients p WHERE p.uhid = 'CS-GNR-0007';

INSERT INTO diagnostic_tests (patient_id, test_name, status)
SELECT p.id, 'Colposcopy + biopsy', 'PENDING' FROM patients p WHERE p.uhid = 'CS-GNR-0005';
INSERT INTO diagnostic_tests (patient_id, test_name, status, result_code, result_notes, reported_at)
SELECT p.id, 'Biopsy of oral lesion', 'DONE', 'POSITIVE', 'Moderately differentiated SCC', TIMESTAMPADD(DAY, -3, CURRENT_TIMESTAMP)
FROM patients p WHERE p.uhid = 'CS-GNR-0006';

INSERT INTO patient_events (patient_id, event_type, message, actor_name, actor_role)
SELECT p.id, 'SCREENED', CONCAT('Screened for ', p.cancer_type, ' cancer'), p.screened_by_name, p.screened_by_role
FROM patients p;

INSERT INTO patient_events (patient_id, event_type, message, actor_name, actor_role)
SELECT p.id, 'BOOKED', 'Booked at District Hospital for today · place 3 of 10 · SMS and voice call sent', 'Dr A. Shah', 'MO'
FROM patients p WHERE p.uhid = 'CS-GNR-0003';

INSERT INTO alerts (public_id, title, body, severity, audience_role, audience_phc, patient_id)
SELECT '44444444-4444-4444-4444-444444444001', 'CHO review pending', 'Kamla Ben · oral screening awaiting CHO review', 'WARN', 'CHO', 'ADL', p.id
FROM patients p WHERE p.uhid = 'CS-GNR-0001';

INSERT INTO alerts (public_id, title, body, severity, audience_role, audience_phc, patient_id)
SELECT '44444444-4444-4444-4444-444444444002', 'MO booking pending', 'Savita Patel · breast case ready for approve & book', 'WARN', 'MO', 'ADL', p.id
FROM patients p WHERE p.uhid = 'CS-GNR-0002';

INSERT INTO alerts (public_id, title, body, severity, audience_role, audience_fac, patient_id)
SELECT '44444444-4444-4444-4444-444444444003', 'Hospital visit today', 'Ramesh Thakor · District Hospital green-channel place 3', 'INFO', 'HOSP', 'DH', p.id
FROM patients p WHERE p.uhid = 'CS-GNR-0003';

INSERT INTO alerts (public_id, title, body, severity, audience_role, audience_phc, patient_id)
SELECT '44444444-4444-4444-4444-444444444004', 'Missed visit', 'Dinesh Dabhi missed DH visit · rebook or close', 'CRIT', 'MO', 'BHY', p.id
FROM patients p WHERE p.uhid = 'CS-GNR-0008';

INSERT INTO messages (public_id, channel, direction, mobile, subject, body, language, patient_id, status)
SELECT '55555555-5555-5555-5555-555555555001', 'SMS', 'OUT', p.mobile, 'CARESETU booking',
 'CARESETU: Your hospital visit is booked for today 10:00–12:00 at District Hospital, Gandhinagar. Show this message.',
 'en', p.id, 'SENT'
FROM patients p WHERE p.uhid = 'CS-GNR-0003';

INSERT INTO messages (public_id, channel, direction, mobile, subject, body, language, patient_id, status)
SELECT '55555555-5555-5555-5555-555555555002', 'IVRS', 'OUT', p.mobile, 'CARESETU reminder',
 'Voice reminder queued for hospital visit tomorrow.',
 'gu', p.id, 'QUEUED'
FROM patients p WHERE p.uhid = 'CS-GNR-0009';
