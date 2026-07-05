\c :mosipdbname

REVOKE SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.otp_transaction
TO :dbuname;

REVOKE SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.resident_grievance_ticket
TO :dbuname;

ALTER TABLE resident.resident_session alter column machine_type type varchar(30);

DROP INDEX IF EXISTS idx_resident_user_actions_ida_token;

REVOKE SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_user_actions
   TO :dbuname;