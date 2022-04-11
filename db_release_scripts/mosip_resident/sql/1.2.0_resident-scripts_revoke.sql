-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	    Added otp_transaction table creation scripts with comments.
-----------------------------------------------------------------------------------------------------
\c mosip_resident sysadmin

DROP TABLE IF EXISTS idrepo.otp_transaction;
-- DROP TABLE IF EXISTS idrepo.resident_transaction;
-----------------------------------------------------------------------------------------------------