-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE UNIQUE INDEX pk_otpt_id ON resident.otp_transaction USING btree (id);

CREATE UNIQUE INDEX pk_resgrev_id ON resident.resident_grievance_ticket USING btree (id);

CREATE INDEX idx_resident_session_ida_token ON resident.resident_session USING btree (ida_token);
CREATE UNIQUE INDEX pk_session_id ON resident.resident_session USING btree (session_id);

CREATE INDEX idx_resident_transaction_credential_request_id ON resident.resident_transaction USING btree (credential_request_id);
CREATE INDEX idx_resident_transaction_event_id ON resident.resident_transaction USING btree (event_id);
CREATE INDEX idx_resident_transaction_read_status ON resident.resident_transaction USING btree (read_status);
CREATE INDEX idx_resident_transaction_ref_id ON resident.resident_transaction USING btree (ref_id);
CREATE INDEX idx_resident_transaction_request_trn_id ON resident.resident_transaction USING btree (request_trn_id);
CREATE INDEX idx_resident_transaction_token_id ON resident.resident_transaction USING btree (token_id);

CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions USING btree (ida_token);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
