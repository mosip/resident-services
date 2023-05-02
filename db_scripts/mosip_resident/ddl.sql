-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By             Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	            Creation of otp_transaction table.
-- April-2022           Kamesh Shekhar Prasad   Creation of resident_transaction table.
-----------------------------------------------------------------------------------------------------

\c mosip_resident

\ir ddl/otp_transaction.sql
\ir ddl/resident_transaction.sql
\ir ddl/resident_grievance_ticket.sql
\ir ddl/resident_user_actions.sql
\ir ddl/resident_session.sql
-----------------------------------------------------------------------------------------------------
