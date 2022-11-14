-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_resident
-- Release Version 	: 1.2.1
-- Purpose    		: Database scripts for Resident Service DB.
-- Create By   		: Kamesh Shekhar Prasad
-- Created Date		: April-2022
--
-- Modified Date        Modified By         Comments / Remarks
-- --------------------------------------------------------------------------------------------------
--
-----------------------------------------------------------------------------------------------------

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
    purpose character varying(64),
    credential_request_id character varying(256),
    attribute_list character varying(255),
    individual_id character varying(500),
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
