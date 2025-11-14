-- UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES

CREATE INDEX idx_resident_session_ida_token ON resident.resident_session USING btree (ida_token);

CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions USING btree (ida_token);

---END UPGRADE FOR PERFORMANCE OPTIMIZATION INDEXES--
