-- resident_session table

-- Adding index to ida_token column
CREATE INDEX idx_resident_session_ida_token ON resident.resident_session (ida_token);

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_session
   TO residentuser;

-- resident_transaction table

-- Adding index to event_id column
CREATE INDEX idx_resident_transaction_event_id ON resident.resident_transaction (event_id);

-- Adding index to token_id column
CREATE INDEX idx_resident_transaction_token_id ON resident.resident_transaction (token_id);

-- Adding index to credential_request_id column
CREATE INDEX idx_resident_transaction_credential_request_id ON resident.resident_transaction (credential_request_id);

-- Adding index to request_dtimes column
CREATE INDEX idx_resident_transaction_request_dtimes ON resident.resident_transaction (request_dtimes);

-- Adding index to request_trn_id column
CREATE INDEX idx_resident_transaction_request_trn_id ON resident.resident_transaction (request_trn_id);

-- Adding index to ref_id column
CREATE INDEX idx_resident_transaction_ref_id ON resident.resident_transaction (ref_id);

--Adding index to read_status column
CREATE INDEX idx_resident_transaction_read_status ON resident.resident_transaction (read_status);

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_transaction
   TO residentuser;


ALTER TABLE resident.resident_transaction
ALTER COLUMN individual_id TYPE VARCHAR(1024);



-- resident_user_actions table

-- Adding index to ida_token column
CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions (ida_token);

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_user_actions
   TO residentuser;


-- otp_transaction table

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.otp_transaction
   TO residentuser;


-- resident_grievance_ticket table

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_grievance_ticket
   TO residentuser;

