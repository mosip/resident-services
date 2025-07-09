-- -------------------------------------------------------------------------------------------------
-- Database Name: :mosip_db_name
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	    Granting usage access to :resident_user_name scripts added.
-----------------------------------------------------------------------------------------------------
\c :mosip_db_name 

GRANT CONNECT
   ON DATABASE :mosip_db_name
   TO :resident_user_name;

GRANT USAGE
   ON SCHEMA :schema_name
   TO :resident_user_name;

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA :schema_name
   TO :resident_user_name;

ALTER DEFAULT PRIVILEGES IN SCHEMA :schema_name 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO :resident_user_name;
-----------------------------------------------------------------------------------------------------
