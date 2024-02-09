-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Kamesh Shekhar Prasad
-- Created Date		: February-2024
-- 
-- Modified Date        Modified By             Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-----------------------------------------------------------------------------------------------------
\c mosip_resident sysadmin

DROP TABLE IF EXISTS resident.otp_transaction;
DROP TABLE IF EXISTS resident.resident_transaction;
DROP TABLE IF EXISTS resident.resident_session;
DROP TABLE IF EXISTS resident.resident_user_actions;

-----------------------------------------------------------------------------------------------------