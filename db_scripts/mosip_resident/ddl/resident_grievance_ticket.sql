-- -------------------------------------------------------------------------------------------------
-- Database Name: resident_grievance_ticket
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.
-- Create By   		: Kamesh Shekhar Prasad
-- Created Date		: December-2022
--
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
--
-----------------------------------------------------------------------------------------------------

-- This Table is used to save the  resident_grievance_ticket table values.
CREATE TABLE resident.resident_grievance_ticket(
    id VARCHAR(64) NOT NULL,
    eventId VARCHAR(64) NOT NULL,
    emailId VARCHAR(64),
    alternateEmailId VARCHAR(64),
    phoneNo VARCHAR(64),
    altrenatePhoneNo VARCHAR(64),
	message character varying(1024) NOT NULL,
	status character varying(36) NOT NULL,
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    is_deleted boolean NOT NULL DEFAULT false,
    del_dtimes timestamp,
    CONSTRAINT pk_resgrev_id PRIMARY KEY (id)
);

COMMENT ON TABLE resident.resident_grievance_ticket IS 'This Table is used to save the  resident_grievance_ticket table values.';
COMMENT ON COLUMN resident.resident_grievance_ticket.id IS 'Unique Id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.event_id IS 'Unique event id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.emailId IS 'Unique email id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.alternateEmailId IS 'Alternate email id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.phoneNo IS 'Phone number.';
COMMENT ON COLUMN resident.resident_grievance_ticket.altrenatePhoneNo IS 'Alternate Phone number.';
COMMENT ON COLUMN resident.resident_grievance_ticket.message IS 'Message.';
COMMENT ON COLUMN resident.resident_grievance_ticket.status IS 'status.';
COMMENT ON COLUMN resident.resident_grievance_ticket.cr_by IS 'created by.';
COMMENT ON COLUMN resident.resident_grievance_ticket.cr_dtimes IS 'created date and time.';
COMMENT ON COLUMN resident.resident_grievance_ticket.upd_by IS 'updated by.';
COMMENT ON COLUMN resident.resident_grievance_ticket.upd_dtimes IS 'updated date and time.';
COMMENT ON COLUMN resident.resident_grievance_ticket.is_deleted IS 'is deleted.';
COMMENT ON COLUMN resident.resident_grievance_ticket.del_dtimes IS 'Deleted time-stamp.';

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
