-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By             Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	            Added otp_transaction table creation scripts with comments.
-- April-2022           Kamesh Shekhar Prasad   Added resident_transaction table creation scripts with comments.
-----------------------------------------------------------------------------------------------------
\c mosip_resident sysadmin

DROP INDEX IF EXISTS idx_resident_transaction_event_id;
DROP INDEX IF EXISTS idx_resident_transaction_token_id;
DROP INDEX IF EXISTS idx_resident_transaction_credential_request_id;
DROP INDEX IF EXISTS idx_resident_transaction_request_dtimes;
DROP INDEX IF EXISTS idx_resident_transaction_request_trn_id;
DROP INDEX IF EXISTS idx_resident_transaction_ref_id;
DROP INDEX IF EXISTS idx_resident_user_actions_ida_token;
DROP INDEX IF EXISTS idx_resident_session_ida_token;
DROP INDEX IF EXISTS idx_resident_transaction_read_status;

DROP TABLE IF EXISTS resident.otp_transaction;
DROP TABLE IF EXISTS resident.resident_transaction;
DROP TABLE IF EXISTS resident.resident_session;
DROP TABLE IF EXISTS resident.resident_user_actions;

-----------------------------------------------------------------------------------------------------