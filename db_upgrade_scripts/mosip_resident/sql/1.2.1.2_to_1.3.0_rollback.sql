-- ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES

DROP INDEX IF EXISTS resident.idx_resident_session_ida_token;

DROP INDEX IF EXISTS resident.idx_resident_transaction_ref_id;
DROP INDEX IF EXISTS resident.idx_resident_transaction_request_trn_id;
DROP INDEX IF EXISTS resident.idx_resident_transaction_token_id;

DROP INDEX IF EXISTS resident.idx_resident_user_actions_ida_token;

-- END ROLLBACK FOR PERFORMANCE OPTIMIZATION INDEXES
