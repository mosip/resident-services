-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.       
-- Create By   		: Manoj SP
-- Created Date		: April-2022
-- 
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
-- April-2022			Manoj SP	    Creation of mosip_resident DB and resident schema.
-----------------------------------------------------------------------------------------------------
CREATE DATABASE mosip_resident
	ENCODING = 'UTF8'
	LC_COLLATE = 'en_US.UTF-8'
	LC_CTYPE = 'en_US.UTF-8'
	TABLESPACE = pg_default
	OWNER = postgres
	TEMPLATE  = template0;
	
COMMENT ON DATABASE mosip_resident IS 'Resident service database stores all the data related to transactions done in resident services';

\c mosip_resident

DROP SCHEMA IF EXISTS resident CASCADE;
CREATE SCHEMA resident;
ALTER SCHEMA resident OWNER TO postgres;
ALTER DATABASE mosip_resident SET search_path TO resident,pg_catalog,public;
-----------------------------------------------------------------------------------------------------