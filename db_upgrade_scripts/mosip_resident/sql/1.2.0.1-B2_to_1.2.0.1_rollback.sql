-- Dropping index

DROP INDEX IF EXISTS idx_resident_session_ida_token;
DROP INDEX IF EXISTS idx_resident_transaction_event_id;
DROP INDEX IF EXISTS idx_resident_transaction_token_id;
DROP INDEX IF EXISTS idx_resident_transaction_credential_request_id;
DROP INDEX IF EXISTS idx_resident_transaction_request_dtimes;
DROP INDEX IF EXISTS idx_resident_transaction_request_trn_id;
DROP INDEX IF EXISTS idx_resident_transaction_ref_id;
DROP INDEX IF EXISTS idx_resident_transaction_read_status;
DROP INDEX IF EXISTS idx_resident_user_actions_ida_token;


-- resident_transaction table
ALTER TABLE resident.resident_transaction
ALTER COLUMN individual_id TYPE VARCHAR(500);