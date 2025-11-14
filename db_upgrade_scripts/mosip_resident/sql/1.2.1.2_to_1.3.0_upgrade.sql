-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE INDEX idx_resident_session_ida_token ON resident.resident_session USING btree (ida_token);

CREATE INDEX idx_resident_transaction_ref_id ON resident.resident_transaction USING btree (ref_id);
CREATE INDEX idx_resident_transaction_request_trn_id ON resident.resident_transaction USING btree (request_trn_id);
CREATE INDEX idx_resident_transaction_token_id ON resident.resident_transaction USING btree (token_id);

CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions USING btree (ida_token);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
