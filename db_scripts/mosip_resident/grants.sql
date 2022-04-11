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
\c mosip_resident 

GRANT CONNECT
   ON DATABASE mosip_resident
   TO residentuser;

GRANT USAGE
   ON SCHEMA resident
   TO residentuser;

GRANT SELECT,INSERT,UPDATE,DELETE,TRUNCATE,REFERENCES
   ON ALL TABLES IN SCHEMA resident
   TO residentuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA resident 
	GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES ON TABLES TO residentuser;
-----------------------------------------------------------------------------------------------------
