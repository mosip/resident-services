-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	    Granting usage access to residentuser scripts added.
-----------------------------------------------------------------------------------------------------
\c :mosipdbname 

GRANT CONNECT
   ON DATABASE :mosipdbname
   TO :defaultdbname;

GRANT USAGE
   ON SCHEMA resident
   TO :defaultdbname;

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA resident
   TO :defaultdbname;

ALTER DEFAULT PRIVILEGES IN SCHEMA resident 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO :defaultdbname;
-----------------------------------------------------------------------------------------------------
