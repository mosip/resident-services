-- -------------------------------------------------------------------------------------------------
-- Database Name:    mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.
-- Create By   		: Kamesh Shekhar Prasad
-- Created Date		: Aug-2022
--
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
--
-----------------------------------------------------------------------------------------------------

-- This Table is used to save the  user actions for the user actions table.

CREATE TABLE resident.resident_user_actions(
    ida_token character varying(128) NOT NULL,
    last_bell_notif_click_dtimes timestamp NOT NULL,
    last_login_dtimes timestamp NOT NULL,
	ip_address character varying(128),
	host character varying(128),
	machine_type character varying(30),
    CONSTRAINT pk_ida_token PRIMARY KEY (ida_token)
);

COMMENT ON TABLE resident_user_actions IS 'This Table is used to save the  user actions for the user actions table.';
COMMENT ON COLUMN resident_user_actions.ida_token IS 'The unique identifier for each user';
COMMENT ON COLUMN resident_user_actions.last_bell_notif_click_dtimes IS 'The time when the user last clicked on the bell notification';
COMMENT ON COLUMN resident_user_actions.last_login_dtimes IS 'The time when the user last logged in';
COMMENT ON COLUMN resident_user_actions.ip_address IS 'The ip_address of device from which the user logged in';
COMMENT ON COLUMN resident_user_actions.host IS 'The host of the site';
COMMENT ON COLUMN resident_user_actions.machine_type IS 'The OS of device used for accessing the portal/app';
