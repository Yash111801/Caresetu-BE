-- Put missed demo patient in Adalaj PHC so ASHA/MO/CHO can act; advance ladder to day 3
UPDATE patients
SET phc_code = 'ADL',
    village = 'Adalaj',
    taluka = 'Gandhinagar'
WHERE uhid = 'CS-GNR-0008';

UPDATE patient_misses m
JOIN patients p ON p.id = m.patient_id
SET m.missed_at = DATE_SUB(CURDATE(), INTERVAL 3 DAY),
    m.escalate_at = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 7 DAY)
WHERE p.uhid = 'CS-GNR-0008' AND m.resolved_at IS NULL;

INSERT INTO contact_attempts (patient_id, channel, status, attempt_no, notes)
SELECT p.id, 'IVRS', 'Unanswered', 2, 'Ladder day 1'
FROM patients p
WHERE p.uhid = 'CS-GNR-0008'
  AND NOT EXISTS (
      SELECT 1 FROM contact_attempts c
      WHERE c.patient_id = p.id AND c.channel = 'IVRS' AND c.attempt_no = 2
  );

INSERT INTO contact_attempts (patient_id, channel, status, attempt_no, notes)
SELECT p.id, 'IVRS', 'Unanswered', 3, 'Ladder day 3 · failed'
FROM patients p
WHERE p.uhid = 'CS-GNR-0008'
  AND NOT EXISTS (
      SELECT 1 FROM contact_attempts c
      WHERE c.patient_id = p.id AND c.channel = 'IVRS' AND c.attempt_no = 3
  );

INSERT INTO field_tasks (public_id, patient_id, assignee_role, kind, task_text, status)
SELECT '77777777-7777-7777-7777-777777777002', p.id, 'ASHA', 'miss',
       'Missed DH visit · 3 voice calls not answered · visit or call and record feedback', 'OPEN'
FROM patients p
WHERE p.uhid = 'CS-GNR-0008'
  AND NOT EXISTS (
      SELECT 1 FROM field_tasks t
      WHERE t.patient_id = p.id AND t.kind = 'miss' AND t.status = 'OPEN'
  );
