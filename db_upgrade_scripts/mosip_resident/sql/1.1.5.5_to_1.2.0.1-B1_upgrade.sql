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

CREATE ROLE residentuser WITH 
	INHERIT
	LOGIN
	PASSWORD :dbuserpwd;

GRANT CONNECT
   ON DATABASE mosip_resident
   TO residentuser;

GRANT USAGE
   ON SCHEMA resident
   TO residentuser;

GRANT SELECT,INSERT,UPDATE,DELETE,REFERENCES
   ON ALL TABLES IN SCHEMA resident
   TO residentuser;

-- This Table is used to save the OTP for the user whenever user requests for one using the email id / phone number to log into the application.
CREATE TABLE resident.otp_transaction(
	id character varying(36) NOT NULL,
	ref_id character varying(1024) NOT NULL,
	otp_hash character varying(512) NOT NULL,
	generated_dtimes timestamp,
	expiry_dtimes timestamp,
	validation_retry_count smallint,
	status_code character varying(36),
	lang_code character varying(3),
	cr_by character varying(256) NOT NULL,
	cr_dtimes timestamp NOT NULL,
	upd_by character varying(256),
	upd_dtimes timestamp,
	is_deleted boolean,
	del_dtimes timestamp,
	CONSTRAINT pk_otpt_id PRIMARY KEY (id)
);

COMMENT ON TABLE resident.otp_transaction IS 'All OTP related data and validation details are maintained here for Pre Registration module.';
COMMENT ON COLUMN resident.otp_transaction.id IS 'OTP id is a unique identifier (UUID) used as an unique key to identify the OTP transaction';
COMMENT ON COLUMN resident.otp_transaction.ref_id IS 'Reference ID is a reference information received from OTP requester which can be used while validating the OTP. AM: please give examples of ref_id';
COMMENT ON COLUMN resident.otp_transaction.otp_hash IS 'Hash of id, ref_id and otp which is generated based on the configuration setup and sent to the requester application / module.';
COMMENT ON COLUMN resident.otp_transaction.generated_dtimes IS 'Date and Time when the OTP was generated';
COMMENT ON COLUMN resident.otp_transaction.expiry_dtimes IS 'Date Time when the OTP will be expired';
COMMENT ON COLUMN resident.otp_transaction.validation_retry_count IS 'Validation retry counts of this OTP request. If the validation retry crosses the threshold limit, then the OTP will be de-activated.';
COMMENT ON COLUMN resident.otp_transaction.status_code IS 'Current status of the transaction. Refers to code field of master.status_list table.';
COMMENT ON COLUMN resident.otp_transaction.lang_code IS 'For multilanguage implementation this attribute Refers master.language.code. The value of some of the attributes in current record is stored in this respective language.';
COMMENT ON COLUMN resident.otp_transaction.cr_by IS 'ID or name of the user who create / insert record.';
COMMENT ON COLUMN resident.otp_transaction.cr_dtimes IS 'Date and Timestamp when the record is created/inserted';
COMMENT ON COLUMN resident.otp_transaction.upd_by IS 'ID or name of the user who update the record with new values';
COMMENT ON COLUMN resident.otp_transaction.upd_dtimes IS 'Date and Timestamp when any of the fields in the record is updated with new values.';
COMMENT ON COLUMN resident.otp_transaction.is_deleted IS 'Flag to mark whether the record is Soft deleted.';
COMMENT ON COLUMN resident.otp_transaction.del_dtimes IS 'Date and Timestamp when the record is soft deleted with is_deleted=TRUE';
-----------------------------------------------------------------------------------------------------
GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE
   ON resident.otp_transaction
   TO residentuser;

-- This Table is used to save the  resident_grievance_ticket table values.
CREATE TABLE resident.resident_grievance_ticket(
    id VARCHAR(64) NOT NULL,
    eventId VARCHAR(64) NOT NULL,
    name VARCHAR(256) NOT NULL,
    emailId VARCHAR(128),
    alternateEmailId VARCHAR(128),
    phoneNo VARCHAR(64),
    alternatePhoneNo VARCHAR(64),
	message character varying(1024) NOT NULL,
	hasAttachment boolean NOT NULL DEFAULT false,
	status character varying(64) NOT NULL,
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
COMMENT ON COLUMN resident.resident_grievance_ticket.eventId IS 'Unique event id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.emailId IS 'Unique email id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.alternateEmailId IS 'Alternate email id.';
COMMENT ON COLUMN resident.resident_grievance_ticket.phoneNo IS 'Phone number.';
COMMENT ON COLUMN resident.resident_grievance_ticket.alternatePhoneNo IS 'Alternate Phone number.';
COMMENT ON COLUMN resident.resident_grievance_ticket.message IS 'Message.';
COMMENT ON COLUMN resident.resident_grievance_ticket.status IS 'status.';
COMMENT ON COLUMN resident.resident_grievance_ticket.cr_by IS 'created by.';
COMMENT ON COLUMN resident.resident_grievance_ticket.cr_dtimes IS 'created date and time.';
COMMENT ON COLUMN resident.resident_grievance_ticket.upd_by IS 'updated by.';
COMMENT ON COLUMN resident.resident_grievance_ticket.upd_dtimes IS 'updated date and time.';
COMMENT ON COLUMN resident.resident_grievance_ticket.is_deleted IS 'is deleted.';
COMMENT ON COLUMN resident.resident_grievance_ticket.del_dtimes IS 'Deleted time-stamp.';

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE
   ON resident.resident_grievance_ticket
   TO residentuser;

-- This Table is used to save the  user actions for the user actions table.

CREATE TABLE resident.resident_session(
	session_id character varying(128) NOT NULL,
    ida_token character varying(128) NOT NULL,
    login_dtimes timestamp,
	ip_address character varying(128),
	host character varying(128),
	machine_type character varying(30),
    CONSTRAINT pk_session_id PRIMARY KEY (session_id)
);

COMMENT ON TABLE resident_session IS 'This Table is used to save the  user sessions.';
COMMENT ON COLUMN resident_session.session_id IS 'The unique session identifier for each login';
COMMENT ON COLUMN resident_session.ida_token IS 'The unique identifier for each user';
COMMENT ON COLUMN resident_session.login_dtimes IS 'The time when the user last logged in';
COMMENT ON COLUMN resident_session.ip_address IS 'The ip_address of device from which the user logged in';
COMMENT ON COLUMN resident_session.host IS 'The host of the site';
COMMENT ON COLUMN resident_session.machine_type IS 'The OS of device used for accessing the portal/app';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE
   ON resident.resident_session
   TO residentuser;

-- This Table is used to save the  transaction related to residents.
CREATE TABLE resident.resident_transaction(
    event_id VARCHAR(64) NOT NULL,
	request_trn_id character varying(64) ,
	request_dtimes timestamp NOT NULL,
	response_dtime timestamp NOT NULL,
	request_type_code character varying(128) NOT NULL,
	request_summary character varying(1024) NOT NULL,
	status_code character varying(36) NOT NULL,
	status_comment character varying(1024),
	lang_code character varying(3),
	ref_id_type character varying(36),
    ref_id character varying(64),
    token_id character varying(128) NOT NULL,
    requested_entity_type character varying(64),
    requested_entity_id character varying(36),
    requested_entity_name character varying(128),
    cr_by character varying(256) NOT NULL,
    cr_dtimes timestamp NOT NULL,
    upd_by character varying(256),
    upd_dtimes timestamp,
    is_deleted boolean NOT NULL DEFAULT false,
    del_dtimes timestamp,
    auth_type_code character varying(128),
    static_tkn_id character varying(64),
    request_signature character varying,
    response_signature character varying,
    olv_partner_id character varying(36),
    aid character varying(64),
    reference_link character varying(1024),
    read_status boolean NOT NULL DEFAULT false,
    pinned_status boolean NOT NULL DEFAULT false,
    purpose character varying(1024),
    credential_request_id character varying(256),
    attribute_list character varying(255),
    individual_id character varying(1024),
    consent character varying(50),
    tracking_id character varying(50),
    CONSTRAINT pk_restrn_event_id PRIMARY KEY (event_id)
);

COMMENT ON TABLE resident_transaction IS 'This Table is used to save the  transaction related to residents.';
COMMENT ON COLUMN resident_transaction.event_id IS 'Unique Id of the transaction.';
COMMENT ON COLUMN resident.resident_transaction.aid IS 'The Application ID';
COMMENT ON COLUMN resident.resident_transaction.request_dtimes IS 'The time when the request is received by the service';
COMMENT ON COLUMN resident.resident_transaction.response_dtime IS 'The time when the response is received by the service';
COMMENT ON COLUMN resident.resident_transaction.request_trn_id IS 'The unique identifier for each transaction';
COMMENT ON COLUMN resident.resident_transaction.request_type_code IS 'The type of request';
COMMENT ON COLUMN resident.resident_transaction.request_summary IS 'The summary of the request';
COMMENT ON COLUMN resident.resident_transaction.status_code IS 'The current status of the request';
COMMENT ON COLUMN resident.resident_transaction.status_comment IS 'The comment for the status of the request';
COMMENT ON COLUMN resident.resident_transaction.lang_code IS 'The language code for the request for multi-language support';
COMMENT ON COLUMN resident.resident_transaction.ref_id_type IS 'The type of reference id';
COMMENT ON COLUMN resident.resident_transaction.ref_id IS 'The reference id';
COMMENT ON COLUMN resident.resident_transaction.token_id IS 'The token id';
COMMENT ON COLUMN resident.resident_transaction.requested_entity_type IS 'The type of the requested entity';
COMMENT ON COLUMN resident.resident_transaction.requested_entity_id IS 'The id of the requested entity';
COMMENT ON COLUMN resident.resident_transaction.requested_entity_name IS 'The name of the requested entity';
COMMENT ON COLUMN resident.resident_transaction.cr_by IS 'The user who created the record';
COMMENT ON COLUMN resident.resident_transaction.cr_dtimes IS 'The time when the record is created';
COMMENT ON COLUMN resident.resident_transaction.upd_by IS 'The user who updated the record';
COMMENT ON COLUMN resident.resident_transaction.upd_dtimes IS 'The time when the record is updated';
COMMENT ON COLUMN resident.resident_transaction.is_deleted IS 'The flag to identify if the record is deleted or not';
COMMENT ON COLUMN resident.resident_transaction.del_dtimes IS 'The time when the record is deleted';
COMMENT ON COLUMN resident.resident_transaction.auth_type_code IS 'The type of the authentication';
COMMENT ON COLUMN resident.resident_transaction.static_tkn_id IS 'The static token id';
COMMENT ON COLUMN resident.resident_transaction.request_signature IS 'The signature of the request';
COMMENT ON COLUMN resident.resident_transaction.response_signature IS 'The signature of the response';
COMMENT ON COLUMN resident.resident_transaction.olv_partner_id IS 'The partner id';
COMMENT ON COLUMN resident.resident_transaction.reference_link IS 'The reference link';
COMMENT ON COLUMN resident.resident_transaction.read_status IS 'The flag to identify if the request is read or not';
COMMENT ON COLUMN resident.resident_transaction.pinned_status IS 'The flag to identify if the request is pinned or not';
COMMENT ON COLUMN resident.resident_transaction.purpose IS 'The purpose of the request';
COMMENT ON COLUMN resident.resident_transaction.credential_request_id IS 'The credential request id';

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE
   ON resident.resident_transaction
   TO residentuser;

-- This Table is used to save the  user actions for the user actions table.

CREATE TABLE resident.resident_user_actions(
    ida_token character varying(128) NOT NULL,
    last_bell_notif_click_dtimes timestamp,
    CONSTRAINT pk_ida_token PRIMARY KEY (ida_token)
);

COMMENT ON TABLE resident_user_actions IS 'This Table is used to save the  user actions';
COMMENT ON COLUMN resident_user_actions.ida_token IS 'The unique identifier for each user';
COMMENT ON COLUMN resident_user_actions.last_bell_notif_click_dtimes IS 'The time when the user last clicked on the bell notification';

GRANT SELECT, INSERT, TRUNCATE, REFERENCES, UPDATE, DELETE
   ON resident.resident_user_actions
   TO residentuser;
