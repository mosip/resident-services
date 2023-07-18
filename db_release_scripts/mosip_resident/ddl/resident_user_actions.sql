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
    last_bell_notif_click_dtimes timestamp,
    CONSTRAINT pk_ida_token PRIMARY KEY (ida_token)
);

COMMENT ON TABLE resident.resident_user_actions IS 'This Table is used to save the  user actions';
COMMENT ON COLUMN resident.resident_user_actions.ida_token IS 'The unique identifier for each user';
COMMENT ON COLUMN resident.resident_user_actions.last_bell_notif_click_dtimes IS 'The time when the user last clicked on the bell notification';

-- Adding index to ida_token column
CREATE INDEX idx_resident_user_actions_ida_token ON resident.resident_user_actions (ida_token);