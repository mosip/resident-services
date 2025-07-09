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
-- Switch to the target DB
\c :mosip_db_name

-- Grant CONNECT on the DB to the user
GRANT CONNECT ON DATABASE :"mosip_db_name" TO :"resident_user_name";

-- Allow user to use the schema
GRANT USAGE ON SCHEMA :"schema_name" TO :"resident_user_name";

-- Grant privileges on all tables in the schema
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES
  ON ALL TABLES IN SCHEMA :"schema_name"
  TO :"resident_user_name";

-- Set default privileges for future tables created in this schema
ALTER DEFAULT PRIVILEGES IN SCHEMA :"schema_name"
  GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO :"resident_user_name";

-----------------------------------------------------------------------------------------------------
