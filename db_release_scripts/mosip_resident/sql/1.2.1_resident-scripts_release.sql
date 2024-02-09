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

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.otp_transaction
TO residentuser;

GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
ON resident.resident_grievance_ticket
TO residentuser;

ALTER TABLE resident.resident_session alter column machine_type type varchar(100);
-- Adding index to ida_token column
CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions (ida_token);
GRANT SELECT, INSERT, REFERENCES, UPDATE, DELETE
   ON resident.resident_user_actions
   TO residentuser;
-----------------------------------------------------------------------------------------------------