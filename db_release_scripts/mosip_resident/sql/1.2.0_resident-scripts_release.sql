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
-- Dec-2022           Kamesh Shekhar Prasad     Added resident_grievance_ticket, resident_user_actions table creation scripts with comments.
-----------------------------------------------------------------------------------------------------
\c mosip_resident sysadmin

\ir ddl/otp_transaction.sql
\ir ddl/resident_transaction.sql
\ir ddl/resident_grievance_ticket.sql
\ir ddl/resident_user_actions.sql
-----------------------------------------------------------------------------------------------------