\c :mosipdbname

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.otp_transaction
TO :dbuname;

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.resident_grievance_ticket
TO :dbuname;

ALTER TABLE resident.resident_session alter column machine_type type varchar(100);
-- Adding index to ida_token column
CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions (ida_token);
GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_user_actions
   TO :dbuname;