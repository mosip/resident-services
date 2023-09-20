package io.mosip.resident.util;

import io.mosip.resident.constant.RegistrationConstants;

public enum EventEnum {

	RID_STATUS_SUCCESS("RES-SER-200", RegistrationConstants.SYSTEM, "RID status: Success",
			"Request for checking RID status is success", "RES-SER", "Residence service", "RS-RID", "RID Status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_STATUS_FAILURE("RES-SER-302", RegistrationConstants.SYSTEM, "RID status: Failed",
			"Request for checking RID status failed- %s", "RES-SER", "Residence service", "RS-RID", "RID Status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_EUIN_SUCCESS("RES-SER-319", RegistrationConstants.SYSTEM, "Request EUIN: Success",
			"Requesting euin for transaction id %s is success", "RES-SER", "Residence service", "RS-UIN", "UIN",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_PRINTUIN_SUCCESS("RES-SER-201", RegistrationConstants.SYSTEM, "Request print UIN: Success",
			"Requesting print uin api for transaction id %s is success", "RES-SER", "Residence service", "RS-UIN",
			"UIN", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_LOCK_SUCCESS("RES-SER-202", RegistrationConstants.SYSTEM, "Request auth lock: Success",
			"Requesting auth lock api for transaction id %s is success", "RES-SER", "Residence service", "RS-AUTH_LOCK_UNLOCK",
			"Auth lock/unlock", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_UNLOCK_SUCCESS("RES-SER-203", RegistrationConstants.SYSTEM, "Request auth unlock: Success",
			"Requesting auth unlock api for transaction id %s is success", "RES-SER", "Residence service", "RS-AUTH_LOCK_UNLOCK",
			"Auth lock/unlock", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	REQ_AUTH_LOCK_UNLOCK_SUCCESS("RES-SER-203", RegistrationConstants.SYSTEM, "Request auth lock unlock: Success",
			"Requesting auth lock unlock api is success", "RES-SER", "Residence service", "RS-AUTH_LOCK_UNLOCK",
			"Auth lock/unlock", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_HISTORY_SUCCESS("RES-SER-204", RegistrationConstants.SYSTEM, "Request auth history: Success",
			"Requesting auth history api for transaction id %s is success", "RES-SER", "Residence service", "RS-AUTH_HIST",
			"Auth history", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	UPDATE_UIN_SUCCESS("RES-SER-205", RegistrationConstants.SYSTEM, "Request update uin: Success",
			"Requesting update uin api for transaction id %s is success", "RES-SER", "Residence service", "RS-UIN",
			"UIN", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UPDATE_UIN_FAILURE("RES-SER-305", RegistrationConstants.SYSTEM, "Request update uin: Failed",
			"Requesting update uin failed", "RES-SER", "Residence service", "RS-UIN", "UIN",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GENERATE_VID_SUCCESS("RES-SER-206", RegistrationConstants.SYSTEM, "Request for generating VID: Success",
			"Request for generating VID for transaction id %s is success", "RES-SER", "Residence service", "RS-GEN_REV_VID",
			"Generate/Revoke VID", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REVOKE_VID_SUCCESS("RES-SER-207", RegistrationConstants.SYSTEM, "Request for revoking VID: Success",
			"Request for revoking VID for transaction id %s is success", "RES-SER", "Residence service", "RS-GEN_REV_VID",
			"Generate/Revoke VID", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_REQUEST("RES-SER-110", RegistrationConstants.SYSTEM, "Validating input request",
			"Validating input request of %s", "RES-SER", "Residence service", "RS-VAL_REQ", "Validate request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_NOTIFICATION_SUCCESS("RES-SER-208", RegistrationConstants.SYSTEM, "Send notification: Success",
			"Sending notification for transaction id %s", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_OTP_SUCCESS("RES-SER-209", RegistrationConstants.SYSTEM, "Validate otp: Success",
			"Validating OTP for transaction id %s is success", "RES-SER", "Residence service", "RS-OTP", "Otp section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VALIDATE_OTP_FAILURE("RES-SER-301", RegistrationConstants.SYSTEM, "Validate otp: Failed",
			"OTP vaildation for transaction id %s is failed", "RES-SER", "Residence service", "RS-OTP", "Otp section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_NOTIFICATION_FAILURE("RES-SER-403", RegistrationConstants.SYSTEM, "Send notification: Failed",
			"Failure notification sent for transaction id %s", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OBTAINED_RID("RES-SER-114", RegistrationConstants.SYSTEM, "Request print UIN",
			"Obtained RID for transaction id %s while requesting for printing UIN", "RES-SER", "Residence service",
			"RS-RID", "RID section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OBTAINED_RID_UIN_UPDATE("RES-SER-115", RegistrationConstants.SYSTEM, "Request UIN Update",
			"Obtained RID for transaction id %s while requesting for update UIN", "RES-SER", "Residence service", "RS-RID",
			"RID section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VID_GENERATED("RES-SER-117", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID generated for transaction id %s", "RES-SER", "Residence service", "RS-VID_GEN", "VID generation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_ALREADY_EXISTS("RES-SER-405", RegistrationConstants.SYSTEM, "VID already exists",
			"VID already exists for transaction id %s", "RES-SER", "Residence service", "RS-VID_GEN", "VID generation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_GENERATION_FAILURE("RES-SER-406", RegistrationConstants.SYSTEM, "Request to generate VID: Failed",
			"VID generated failed for transaction id %s", "RES-SER", "Residence service", "RS-VID_GEN", "VID generation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_JSON_PARSING_EXCEPTION("RES-SER-404", RegistrationConstants.SYSTEM, "Json parsing exception",
			"JSON parsing exception for transaction id %s while generating VID", "RES-SER", "Residence service", "RS-VID",
			"VID section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DEACTIVATED_VID("RES-SER-320", RegistrationConstants.SYSTEM, "Request to revoke VID",
			"Deactivated VID for transaction id %s while generating VID", "RES-SER", "Residence service", "RS_VID_REV",
			"Revoke vid", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_REVOKE_EXCEPTION("RES-SER-407", RegistrationConstants.SYSTEM, "Request to revoke VID: Exception",
			"Revoking VID failed for transaction id %s", "RES-SER", "Residence service", "RS_VID_REV", "Revoke vid",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	RID_NOT_FOUND("RES-SER-408", RegistrationConstants.SYSTEM, "Checking RID status: Not found",
			"RID not found while checking for RID status", "RES-SER", "Residence service", "RS-RID", "RID section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TOKEN_GENERATION_FAILED("RES-SER-409", RegistrationConstants.SYSTEM, "Generating token: Failed", "Token generation failed",
			"RES-SER", "Residence service", "RS-TOK", "Token generation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INPUT_INVALID("RES-SER-410", RegistrationConstants.SYSTEM, "Invalid input", "Invalid input parameter %s", "RES-SER",
			"Residence service", "RS-VAL", "Validation section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	API_NOT_AVAILABLE("RES-SER-411", RegistrationConstants.SYSTEM, "API not available", "API not available for transaction id %s",
			"RES-SER", "Residence service", "RS-API", "API section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	API_RESOURCE_UNACCESS("RES-SER-412", RegistrationConstants.SYSTEM, "API resource unaccess",
			"Unable to access API resource for transaction id %s", "RES-SER", "Residence service", "RS-API",
			"API section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_INVALID("RES-SER-413", RegistrationConstants.SYSTEM, "Check RID: Invalid", "RID is invalid", "RES-SER",
			"Residence service", "RS-RID", "RID section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INPUT_DOESNT_EXISTS("RES-SER-414", RegistrationConstants.SYSTEM, "Validating request", "Request does not exists",
			"RES-SER", "Residence service", "RS-VAL", "Validation section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TEMPLATE_EXCEPTION("RES-SER-415", RegistrationConstants.SYSTEM, "Get template", "Template Exception", "RES-SER",
			"Residence service", "RS-TEMP", "Template section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TEMPLATE_SUBJECT_EXCEPTION("RES-SER-416", RegistrationConstants.SYSTEM, "Get template Subject",
			"Template subject exception", "RES-SER", "Residence service", "RS-TEMP", "Template section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	NOTIFICATION_FAILED("RES-SER-417", RegistrationConstants.SYSTEM, "Notification failed", "Notification failed for transaction id %s",
			"RES-SER", "Residence service", "RS-NOT", "Notification section", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	BAD_REQUEST("RES-SER-418", RegistrationConstants.SYSTEM, "Bad request", "Bad request", "RES-SER", "Residence service",
			"RS-REQ", "Bad request", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_API_RESPONSE("RES-SER-419", RegistrationConstants.SYSTEM, "Checking RID status: Invalid API response",
			"Invalid api response while checking RID status", "RES-SER", "Residence service", "RS-API", "API section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	IO_EXCEPTION("RES-SER-420", RegistrationConstants.SYSTEM, "IO Exception", "IO exception for transaction id %s", "RES-SER",
			"Residence service", "RS-EXCE", "Exception", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	JSON_PARSING_EXCEPTION("RES-SER-421", RegistrationConstants.SYSTEM, "Request for UIN update: Exception",
			"JSON parsing exception for transaction id %s", "RES-SER", "Residence service", "RS-EXCE", "Exception",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_VALIDATION_FAILED("RES-SER-422", RegistrationConstants.SYSTEM, "OTP validation: Failed",
			"OTP validation failed for user Id: %s", "RES-SER", "Residence service", "RS-OTP", "Otp section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	BASE_EXCEPTION("RES-SER-401", RegistrationConstants.SYSTEM, "Base Exception", "Base exception for transaction id %s", "RES-SER",
			"Residence service", "RS-EXCE", "Exception", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQUEST_FAILED("RES-SER-402", RegistrationConstants.SYSTEM, "Request failed", "Request failed for transaction id %s", "RES-SER",
			"Residence service", "RS-REQ", "Request", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREATE_PACKET("RES-SER-118", RegistrationConstants.SYSTEM, "Request to create packet", "Started packet creation",
			"RES-SER", "Residence service", "RS-PACK", "Packet creation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UNKNOWN_EXCEPTION("RES-SER-423", RegistrationConstants.SYSTEM, "Request to create packet: Exception",
			"Unknown exception occured", "RES-SER", "Residence service", "RS-EXCE", "Exception",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	PACKET_CREATED("RES-SER-119", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Uploading UIN packet",
			"RES-SER", "Residence service", "RS-PACK", "Packet creation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_CREATED_FAILURE("RES-SER-425", RegistrationConstants.SYSTEM, "Request to upload UIN packet: Failed",
			"Packet sync Failure", "RES-SER", "Residence service", "RS-PACK", "Packet creation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_CREATED_EXCEPTION("RES-SER-424", RegistrationConstants.SYSTEM, "Request to create packet: Exception",
			"Exception while creating packet", "RES-SER", "Residence service", "RS-PACK", "Packet creation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_SYNC("RES-SER-120", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Sync packet", "RES-SER",
			"Residence service", "RS-PACK", "Packet creation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OTP_GEN_SUCCESS("RES-SER-122", RegistrationConstants.SYSTEM, "generating otp: Success", "otp generation is success",
			"RES-SER", "Residence service", "RS-OTP-GEN", "Otp generation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_GEN_EXCEPTION("RES-SER-123", RegistrationConstants.SYSTEM, "generating otp: Exception", "otp generation is failed",
			"RES-SER", "Residence service", "RS-OTP-GEN", "Otp generation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_REQ_SUCCESS("RES-SER-125", RegistrationConstants.SYSTEM, "Sharing credential to partner: Success",
			"Sharing credential to partner is succeded", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_EXCEPTION("RES-SER-126", RegistrationConstants.SYSTEM, "Sharing credential to partner: Failed",
			"Sharing credential to partner is failed", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_REQ_STATUS_SUCCESS("RES-SER-128", RegistrationConstants.SYSTEM, "credential status: Success",
			"credential req status is success", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_STATUS_EXCEPTION("RES-SER-129", RegistrationConstants.SYSTEM, "credential status: Exception",
			"credential req status is failed", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_CANCEL_REQ_SUCCESS("RES-SER-131", RegistrationConstants.SYSTEM, "credential cancel request: Success",
			"credential cancel request success", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_CANCEL_REQ_EXCEPTION("RES-SER-132", RegistrationConstants.SYSTEM, "credential cancel request: Exception",
			"credential cancel request failed", "RES-SER", "Residence service", "RS-CRED_REQ", "Credential request",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_TYPES_SUCCESS("RES-SER-134", RegistrationConstants.SYSTEM, "credential types: Success",
			"fetch credential type success", "RES-SER", "Residence service", "RS-CRED_TYP", "Credential type",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_TYPES_EXCEPTION("RES-SER-135", RegistrationConstants.SYSTEM, "credential types: Exception",
			"fetch credential types failed", "RES-SER", "Residence service", "RS-CRED_TYP", "Credential type",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_CARD_SUCCESS("RES-SER-137", RegistrationConstants.SYSTEM, "request for card: Success", "request for card is success",
			"RES-SER", "Residence service", "RS-CARD", "Request card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CARD_EXCEPTION("RES-SER-138", RegistrationConstants.SYSTEM, "request for card: Exception", "request for card is failed",
			"RES-SER", "Residence service", "RS-CARD", "Request card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_POLICY_SUCCESS("RES-SER-140", RegistrationConstants.SYSTEM, "request for policy: Success",
			"request for policy is success", "RES-SER", "Residence service", "RS-POL", "Request policy",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_POLICY_EXCEPTION("RES-SER-141", RegistrationConstants.SYSTEM, "request for policy: Exception",
			"request for policy is failed", "RES-SER", "Residence service", "RS-POL", "Request policy",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATION_FAILED_EXCEPTION("RES-SER-142", RegistrationConstants.SYSTEM, "Validation failed",
			"Validation failed : %s", "RES-SER", "Residence service", "RS-VAL", "Validation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_VALID_DOCUMENT_SUCCESS("RES-SER-144", RegistrationConstants.SYSTEM, "get valid documents: Success",
			"get valid documents by lang code is succeed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VALID_DOCUMENT_EXCEPTION("RES-SER-145", RegistrationConstants.SYSTEM, "get valid documents: Exception",
			"get valid documents by lang code is failed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_LOCATION_HIERARCHY_LEVEL_SUCCESS("RES-SER-147", RegistrationConstants.SYSTEM, "get location hierarchy levels: Success",
			"get location hierarchy level by lang code is succeed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION("RES-SER-148", RegistrationConstants.SYSTEM, "get location hierarchy levels: Failed",
			"get location hierarchy level by lang code is failed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_LOCATION_HIERARCHY_LEVEL_ALL_LANG_SUCCESS("RES-SER-147", RegistrationConstants.SYSTEM, "get location hierarchy levels for all language: Success",
			"get location hierarchy level is succeed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_HIERARCHY_LEVEL_ALL_LANG_EXCEPTION("RES-SER-148", RegistrationConstants.SYSTEM, "get location hierarchy levels for all language: Failed",
			"get location hierarchy level is failed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IMMEDIATE_CHILDREN_SUCCESS("RES-SER-150", RegistrationConstants.SYSTEM, "get immediate children: Success",
			"get immediate children by location code and lang code is succeed", "RES-SER", "Residence service", "RS-CHILD",
			"Immediate children", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IMMEDIATE_CHILDREN_EXCEPTION("RES-SER-151", RegistrationConstants.SYSTEM, "get immediate children: Exception",
			"get immediate children by location code and lang code is failed", "RES-SER", "Residence service", "RS-CHILD",
			"Immediate children", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_LOCATION_DETAILS_SUCCESS("RES-SER-153", RegistrationConstants.SYSTEM, "get location details: Success",
			"get location details by location code and lang code is succeed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_DETAILS_EXCEPTION("RES-SER-154", RegistrationConstants.SYSTEM, "get location details: Exception",
			"get location details by location code and lang code is failed", "RES-SER", "Residence service", "RS-LOC",
			"Location", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_COORDINATE_SPECIFIC_REG_CENTERS_SUCCESS("RES-SER-156", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers: Success",
			"RES-SER", "Residence service", "RS-REG", "Registration center", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION("RES-SER-157", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers: Failed",
			"RES-SER", "Residence service", "RS-REG", "Registration center", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_APPLICANT_VALID_DOCUMENT_SUCCESS("RES-SER-159", RegistrationConstants.SYSTEM, "get applicant valid documents: Success",
			"get applicant valid documents is succeed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_APPLICANT_VALID_DOCUMENT_EXCEPTION("RES-SER-160", RegistrationConstants.SYSTEM, "get applicant valid documents: Exception",
			"get applicant valid documents is failed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_REG_CENTERS_FOR_LOCATION_CODE_SUCCESS("RES-SER-162", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code: Success",
			"RES-SER", "Residence service", "RS-REG_LOC", "Registration center location", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION("RES-SER-163", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code: Failed",
			"RES-SER", "Residence service", "RS-REG_LOC", "Registration center location", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_REG_CENTERS_PAGINATED_SUCCESS("RES-SER-165", RegistrationConstants.SYSTEM, "get registration centers paginated: Success",
			"get registration centers paginated is succeed", "RES-SER", "Residence service", "RS-REG", "Registration center",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_PAGINATED_EXCEPTION("RES-SER-166", RegistrationConstants.SYSTEM,
			"get registration centers paginated", "get registration centers paginated: Failed", "RES-SER",
			"Residence service", "RS-REG", "Registration center", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_CONFIGURATION_PROPERTIES_SUCCESS("RES-SER-168", RegistrationConstants.SYSTEM,
			"get resident configuration properties success", "get resident configuration properties: Success", "RES-SER",
			"Residence service", "RS-CONF", "Config properties", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_CONFIGURATION_PROPERTIES_EXCEPTION("RES-SER-169", RegistrationConstants.SYSTEM,
			"get resident configuration properties failure", "get resident configuration properties: Failed", "RES-SER",
			"Residence service", "RS-CONF", "Config properties", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_REG_CENTER_WORKING_DAYS_SUCCESS("RES-SER-171", RegistrationConstants.SYSTEM,
			"get registration center working days success", "get registration center working days: Success", "RES-SER",
			"Residence service", "RS-REG", "Registration center", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTER_WORKING_DAYS_EXCEPTION("RES-SER-172", RegistrationConstants.SYSTEM,
			"get registration center working days failure", "get registration center working days: Failed", "RES-SER",
			"Residence service", "RS-REG", "Registration center", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_LATEST_ID_SCHEMA_SUCCESS("RES-SER-174", RegistrationConstants.SYSTEM, "get latest id schema: Success",
			"get latest id schema is succeeded", "RES-SER", "Residence service", "RS-ID_SCH", "ID schema",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LATEST_ID_SCHEMA_EXCEPTION("RES-SER-175", RegistrationConstants.SYSTEM, "get latest id schema: Failed",
			"get latest id schema is failed", "RES-SER", "Residence service", "RS-ID_SCH", "ID schema",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	UPLOAD_DOCUMENT_SUCCESS("RES-SER-177", RegistrationConstants.SYSTEM, "Document upload: Success",
			"document upload success for transaction id %s", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UPLOAD_DOCUMENT_FAILED("RES-SER-178", RegistrationConstants.SYSTEM, "Document upload: Failed",
			"document upload failed for transaction id %s", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_DOCUMENTS_METADATA_SUCCESS("RES-SER-180", RegistrationConstants.SYSTEM, "Get documents: Success",
			"get documents success for transaction id %s", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENTS_METADATA_FAILED("RES-SER-181", RegistrationConstants.SYSTEM, "Get documents: Failed",
			"Get documents failed for transaction id %s", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	REQ_AUTH_LOCK_STATUS_SUCCESS("RES-SER-183", RegistrationConstants.SYSTEM, "Request auth lock status: Success",
			"Requesting auth lock status api is success", "RES-SER", "Residence service", "RS-AUTH_LOCK",
			"Auth lock", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_LOCK_STATUS_FAILED("RES-SER-184", RegistrationConstants.SYSTEM, "Request auth lock status: Failed",
			"Requesting auth lock status api is failed", "RES-SER", "Residence service", "RS-AUTH_LOCK",
			"Auth lock", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	AUTH_TYPE_CALL_BACK_SUCCESS("RES-SER-323", RegistrationConstants.SYSTEM, "Auth type call back: Success",
			"auth type call back success for transaction id %s", "RES-SER", "Residence service", "RS-AUTH_TYP", "Auth type",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AUTH_TYPE_CALL_BACK_FAILURE("RES-SER-324", RegistrationConstants.SYSTEM, "Auth type call back: Failed",
			"auth type call back failure for transaction id %s", "RES-SER", "Residence service", "RS-AUTH_TYP", "Auth type",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_PARTNERS_BY_PARTNER_TYPE_SUCCESS("RES-SER-187", RegistrationConstants.SYSTEM, "get partners by partner type: Success",
			"get partners by partner type is succeeded", "RES-SER", "Residence service", "RS-PARTN", "Partner section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_PARTNERS_BY_PARTNER_TYPE_EXCEPTION("RES-SER-188", RegistrationConstants.SYSTEM, "get partners by partner type: Failed",
			"get partners by partner type is failed", "RES-SER", "Residence service", "RS-PARTN", "Partner section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_TXN_DETAILS("RES-SER-189", RegistrationConstants.SYSTEM, "Request auth transaction details",
			"Requesting auth transaction details for individual id %s", "RES-SER", "Residence service", "RS-AUTH_TXN",
			"Auth transaction", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_TXN_DETAILS_FAILURE("RES-SER-190", RegistrationConstants.SYSTEM, "Request auth transaction details: Failed",
			"Requesting auth transaction details for individual id %s failed", "RES-SER", "Residence service", "RS-AUTH_TXN",
			"Auth transaction", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_VIDS_SUCCESS("RES-SER-192", RegistrationConstants.SYSTEM,
			"get vids success", "get vids: Success", "RES-SER",
			"Residence service", "RS-VID", "VID", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VIDS_EXCEPTION("RES-SER-193", RegistrationConstants.SYSTEM,
			"get vids failure", "get vids: Failed", "RES-SER",
			"Residence service", "RS-VID", "VID", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OTP_INDIVIDUALID_GEN_SUCCESS("RES-SER-195", RegistrationConstants.SYSTEM, "generating otp for Individual ID: Success", "otp generation for Individual ID is success",
			"RES-SER", "Residence service", "RS-OTP_GEN", "Otp generation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_AID_GEN_EXCEPTION("RES-SER-196", RegistrationConstants.SYSTEM, "generating otp for aid: Failed", "otp generation for aid is failed",
			"RES-SER", "Residence service", "RS-OTP_GEN", "Otp generation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_INPUT_ATTRIBUTES_SUCCESS("RES-SER-198", RegistrationConstants.SYSTEM,
			"get identity attributes success", "get identity attributes: Success", "RES-SER",
			"Residence service", "RS-INP_ATTR", "Input attribute", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_INPUT_ATTRIBUTES_EXCEPTION("RES-SER-199", RegistrationConstants.SYSTEM,
			"get identity attributes failure", "get identity attributes: Failed", "RES-SER",
			"Residence service", "RS-INP_ATTR", "Input attribute", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	INDIVIDUALID_STATUS("RES-SER-321", RegistrationConstants.SYSTEM, "Checking Individual ID status", "Request for checking Individual ID status",
			"RES-SER", "Residence service", "RS-AID", "AID status", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AID_STATUS_SUCCESS("RES-SER-211", RegistrationConstants.SYSTEM, "Checking AID status: Success",
			"Request for checking AID status is success", "RES-SER", "Residence service", "RS-AID", "AID status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AID_STATUS_FAILURE("RES-SER-212", RegistrationConstants.SYSTEM, "Checking AID status: Failed",
			"Request for checking AID status failed", "RES-SER", "Residence service", "RS-AID", "AID status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INDIVIDUALID_STATUS_SUCCESS("RES-SER-325", RegistrationConstants.SYSTEM, "Checking Individual ID status: Success",
			"Request for checking Individual ID status is success", "RES-SER", "Residence service", "RS-IND_ID", "Indiviudal id status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_SERVICE_HISTORY_SUCCESS("RES-SER-303", RegistrationConstants.SYSTEM, "get service history: Success",
			"get service history is success", "RES-SER", "Residence service", "RS-SERV_HIS", "Service history",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_SERVICE_HISTORY_FAILURE("RES-SER-304", RegistrationConstants.SYSTEM, "get service history: Failed",
			"get service history failed", "RES-SER", "Residence service", "RS-SERV_HIS", "Service history",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_SERVICE_REQUEST_UPDATE("RES-SER-215", RegistrationConstants.SYSTEM, "Request service request update",
			"Requesting service request update is success", "RES-SER", "Residence service", "RS-SER_HIS", "Service history",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_SERVICE_REQUEST_UPDATE_SUCCESS("RES-SER-216", RegistrationConstants.SYSTEM, "Request service request update: Success",
			"Requesting service request update is success", "RES-SER", "Residence service", "RS-SER_HIS", "Service history",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	IDA_TOKEN_NOT_FOUND("RES-SER-217", RegistrationConstants.SYSTEM, "IDA token not found",
			"IDA token not found", "RES-SER", "Residence service", "RS-IDA_TOK", "IDA token",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_CUSTOM_CREDENTIAL("RES-SER-218", RegistrationConstants.SYSTEM, "Custom Credential Request", "Custom Credential Request",
			"RES-SER", "Residence service", "RES-CUS_CRED", "Custom credential", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CUSTOM_CREDENTIAL_SUCCESS("RES-SER-219", RegistrationConstants.SYSTEM, "Custom Credential Request: Success",
			"Custom Credential Request is success", "RES-SER", "Residence service", "RES-CUS_CRED", "Custom credential",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CUSTOM_CREDENTIAL_FAILURE("RES-SER-220", RegistrationConstants.SYSTEM, "Custom Credential Request: Failed",
			"Custom Credential Request has failed", "RES-SER", "Residence service", "RES-CUS_CRED", "Custom credential",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CHANNEL_VERIFICATION_STATUS("RES-SER-221", RegistrationConstants.SYSTEM, "Check Channel Verification status Request", "Check Channel Verification status Request",
			"RES-SER", "Residence service", "RS-CHAN_VER", "Channel verification", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHANNEL_VERIFICATION_STATUS_SUCCESS("RES-SER-222", RegistrationConstants.SYSTEM, "Check Channel Verification status Request: Success",
			"Check Channel Verification status Request is success", "RES-SER", "Residence service", "RS-CHAN_VER", "Channel verification",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHANNEL_VERIFICATION_STATUS_FAILURE("RES-SER-223", RegistrationConstants.SYSTEM, "Custom Credential Request: Failed",
			"Custom Credential Request has failed", "RES-SER", "Residence service", "RS-CHAN_VER", "Channel verification",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_VID_POLICY("RES-SER-224", RegistrationConstants.SYSTEM, "Get VID Policy Request", "Get VID Policy Request",
			"RES-SER", "Residence service", "RS-VID", "VID", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VID_POLICY_SUCCESS("RES-SER-225", RegistrationConstants.SYSTEM, "Get VID Policy Request: Success",
			"Get VID Policy Request is success", "RES-SER", "Residence service", "RS-VID", "VID",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VID_POLICY_FAILURE("RES-SER-226", RegistrationConstants.SYSTEM, "Get VID Policy Request: Failed",
			"Get VID Policy Request has failed", "RES-SER", "Residence service", "RS-VID", "VID",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IDMAPPING("RES-SER-227", RegistrationConstants.SYSTEM, "get identity mapping json",
			"get identity mapping json", "RES-SER", "Residence service", "RS-ID_MAP", "ID mapping",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDMAPPING_SUCCESS("RES-SER-228", RegistrationConstants.SYSTEM,
			"get identity mapping json success", "get identity mapping json: Success", "RES-SER",
			"Residence service", "RS-ID_MAP", "ID mapping", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDMAPPING_EXCEPTION("RES-SER-229", RegistrationConstants.SYSTEM,
			"get identity mapping json failure", "get identity mapping json: Failed", "RES-SER",
			"Residence service", "RS-ID_MAP", "ID mapping", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	RID_DIGITAL_CARD_REQ_SUCCESS("RES-SER-231", RegistrationConstants.SYSTEM, "Download digital card request: Success",
			"Download digital card request is succeeded", "RES-SER", "Residence service", "RS-RID_CARD", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_DIGITAL_CARD_REQ_FAILURE("RES-SER-232", RegistrationConstants.SYSTEM, "Download digital card request: Failed",
			"Download digital card request is failed", "RES-SER", "Residence service", "RS-RID_CARD", "RID digital card",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	CHECK_AID_STATUS_REQUEST_SUCCESS("RES-SER-234", RegistrationConstants.SYSTEM, "Request AID status: Success",
			"Requesting AID status for eventId %s is succeeded", "RES-SER", "Residence service", "RS-AID", "AID status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHECK_AID_STATUS_REQUEST_FAILED("RES-SER-279", RegistrationConstants.SYSTEM, "Request AID status: Failed",
			"Requesting AID status  for eventId %s is failed", "RES-SER", "Residence service", "RS-AID", "AID status",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	SEND_PHYSICAL_CARD_SUCCESS("RES-SER-237", RegistrationConstants.SYSTEM, "send a physical card: Success",
			"send a physical card is succeeded", "RES-SER", "Residence service", "RS-PHYS_CARD", "Physical card",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_PHYSICAL_CARD_EXCEPTION("RES-SER-238", RegistrationConstants.SYSTEM, "send a physical card: Exception",
			"send a physical card is failed", "RES-SER", "Residence service", "RS-PHYS_CARD", "Physical card",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_BY_DOC_ID_SUCCESS("RES-SER-240", RegistrationConstants.SYSTEM, "get document by doc id: Success",
			"get document by doc id is succeeded", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_BY_DOC_ID_FAILED("RES-SER-241", RegistrationConstants.SYSTEM, "get document by doc id: Failed",
			"get document by doc id is failed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DELETE_DOCUMENT_SUCCESS("RES-SER-243", RegistrationConstants.SYSTEM, "delete document: Success",
			"delete document is succeeded", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DELETE_DOCUMENT_FAILED("RES-SER-244", RegistrationConstants.SYSTEM, "delete document: Failed",
			"delete document is failed", "RES-SER", "Residence service", "RS-DOC", "Document",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_TEMPLATES_SUCCESS("RES-SER-246", RegistrationConstants.SYSTEM, "get templates: Success",
			"get templates by langCode and templateTypeCode is succeeded", "RES-SER", "Residence service", "RS-TEMP",
			"Templates section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_TEMPLATES_EXCEPTION("RES-SER-247", RegistrationConstants.SYSTEM, "get templates: Failed",
			"get templates by langCode and templateTypeCode is failed", "RES-SER", "Residence service", "RS-TEMP",
			"Templates section", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_LANGUAGE_CODE("RES-SER-500", RegistrationConstants.SYSTEM, "Invalid language code",
			"invalid lang code", "RES-SER", "Residence service", "RS-VAL", "Validation section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IDENTITY_UPDATE_COUNT_SUCCESS("RES-SER-249", RegistrationConstants.SYSTEM, "get identity update count: Success",
			"retrieve remaining update counts for each id attributes for a UIN/VID is succeeded", "RES-SER", "Residence service", "RS-IDEN_COUN",
			"Identity count", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDENTITY_UPDATE_COUNT_EXCEPTION("RES-SER-250", RegistrationConstants.SYSTEM, "get identity update count: Failed",
			"retrieve remaining update counts for each id attributes for a UIN/VID is failed", "RES-SER", "Residence service", "RS-IDEN_COUN",
			"Identity count", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PIN_STATUS_SUCCESS("RES-SER-252", RegistrationConstants.SYSTEM, "pin status success", "pin status success based on event id: Success", "RES-SER",
			"Resident service", "RS-PIN", "Pin status", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PIN_STATUS_FAILURE("RES-SER-253", RegistrationConstants.SYSTEM, "pin status", "pin status failure based on event id: Failed", "RES-SER",
			"Resident service", "RS-PIN", "Pin status", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	UN_PIN_STATUS_SUCCESS("RES-SER-255", RegistrationConstants.SYSTEM, "un pin status: Success", "un pin status success based on event id", "RES-SER",
			"Resident service", "RS-PIN", "Pin status", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UN_PIN_STATUS_FAILURE("RES-SER-256", RegistrationConstants.SYSTEM, "un pin status: Failed", "un pin status failure based on event id", "RES-SER",
			"Resident service", "RS-PIN", "Pin statusE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS("RES-SER-258", RegistrationConstants.SYSTEM, "get acknowledgement download url: Success",
			"get acknowledgement download url is succeeded", "RES-SER", "Residence service", "RS-ACK_DOWN", "Acknowledgement download",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_FAILURE("RES-SER-327", RegistrationConstants.SYSTEM, "get acknowledgement download url: Failed",
			"get acknowledgement download url failed", "RES-SER", "Residence service", "RS-ACK_DOWN", "Acknowledgement download",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	SEND_OTP_SUCCESS("RES-SER-259", RegistrationConstants.SYSTEM, "send otp: Success",
			"send otp is success for userId: %s", "RES-SER", "Residence service", "RS-OTP", "Otp section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	SEND_OTP_FAILURE("RES-SER-290", RegistrationConstants.SYSTEM, "send otp: Failed",
			"send otp is failed for userId: %s", "RES-SER", "Residence service", "RS-OTP", "Otp section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_SERVICE_HISTORY_SUCCESS("RES-SER-266", RegistrationConstants.SYSTEM, "download service history: Success",
			"download service history success based on language code", "RES-SER", "Resident service", "RS-DOWN_SER",
			"Download service history", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DOWNLOAD_SERVICE_HISTORY_FAILURE("RES-SER-313", RegistrationConstants.SYSTEM, "download service history: Failed",
			"download service history failed", "RES-SER", "Resident service", "RS-DOWN_SER", "Download service history",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME_SUCCESS("RES-SER-262", RegistrationConstants.SYSTEM, "get dynamic field based on lang code and field name: Success",
			"get dynamic field based on langCode and field name is succeeded", "RES-SER", "Residence service", "RS-GEND",
			"Dynamic Field", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME_EXCEPTION("RES-SER-263", RegistrationConstants.SYSTEM, "get dynamic field based on lang code and field name: Failed",
			"get dynamic field based on langCode and field name is failed", "RES-SER", "Residence service", "RS-GEND",
			"Dynamic Field", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_REQUEST_TYPE_CODE("RES-SER-267", RegistrationConstants.SYSTEM, "Invalid request type code", "\"Invalid Request Type. Please input eventId only for VID_CARD_DOWNLOAD,\" +\n" +
			"\t\t\t\"DOWNLOAD_PERSONALIZED_CARD, UPDATE_MY_UIN", "RES-SER",
			"Resident service", "RS-VAL", "Validation", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DOWNLOAD_PERSONALIZED_CARD_FAILURE("RES-SER-291", RegistrationConstants.SYSTEM,
			"Download personalized card: Failed", "Download card Html to pdf failed", "RES-SER", "Resident service",
			"RS-DOWN_CARD", "Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DOWNLOAD_PERSONALIZED_CARD_SUCCESS("RES-SER-292", RegistrationConstants.SYSTEM,
			"Download personalized card: Success", "Download card Html to pdf success", "RES-SER", "Resident service",
			"RS-DOWN_CARD", "Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_REGISTRATION_CENTER_SUCCESS("RES-SER-270", RegistrationConstants.SYSTEM,
			"download registration center: Success",
			"download registration center success based on language code and hierarchy level", "RES-SER",
			"Resident service", "RS-DOWN_CARD", "Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_REGISTRATION_CENTER_FAILURE("RES-SER-295", RegistrationConstants.SYSTEM,
			"Download registration center: Failed", "Download registration center is failed", "RES-SER",
			"Resident service", "RS-DOWN_CARD", "Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_SUPPORTING_DOCS_SUCCESS("RES-SER-272", RegistrationConstants.SYSTEM, "Download supporting docs: Success",
			"Download supporting docs success based on language code", "RES-SER", "Resident service", "RS-DOWN_CARD",
			"Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_SUPPORTING_DOCS_FAILURE("RES-SER-296", RegistrationConstants.SYSTEM, "Download supporting docs: Failed",
			"Download supporting docs failed", "RES-SER", "Resident service", "RS-DOWN_CARD", "Download card",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_REGISTRATION_CENTER_NEAREST_SUCCESS("RES-SER-273", RegistrationConstants.SYSTEM,
			"Download nearest registration center: Success",
			"Download nearest registration center success based on language code,longitude,latitude and distance",
			"RES-SER", "Resident service", "RS-DOWN_CARD", "Download card",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_REGISTRATION_CENTER_NEAREST_FAILURE("RES-SER-298", RegistrationConstants.SYSTEM,
			"Download nearest registration center: Failed", "Download nearest registration center failed", "RES-SER",
			"Resident service", "RS-DOWN_CARD", "Download card", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GRIEVANCE_TICKET_REQUEST_SUCCESS("RES-SER-329", RegistrationConstants.SYSTEM,
			"Grievance ticket request: Success",
			"Grievance ticket request success", "RES-SER",
			"Resident service", "RS-GRIEV", "Grievance ticket", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GRIEVANCE_TICKET_REQUEST_FAILED("RES-SER-280", RegistrationConstants.SYSTEM,
			"Grievance ticket request: Failed",
			"Grievance ticket request failed", "RES-SER",
			"Resident service", "RS-GRIEV", "Grievance ticket", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_TYPES_SUCCESS("RES-SER-275", RegistrationConstants.SYSTEM, "get document types: Success",
			"get document types by documentCode and langCode is succeeded", "RES-SER", "Residence service", "RS-DOC",
			"Document", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_TYPES_EXCEPTION("RES-SER-276", RegistrationConstants.SYSTEM, "get document types: Failed",
			"get document types by documentCode and langCode is failed", "RES-SER", "Residence service", "RS-DOC",
			"Document", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_CODE_SUCCESS("RES-SER-277", RegistrationConstants.SYSTEM, "get gender code: Success",
			"get gender code by genderName and langCode is succeeded", "RES-SER", "Residence service", "RS-GEND",
			"Gender", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_CODE_EXCEPTION("RES-SER-278", RegistrationConstants.SYSTEM, "get gender code: Failed",
			"get gender code by genderName and langCode is failed", "RES-SER", "Residence service", "RS-GEND",
			"Gender", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	INVALID_PAGE_START_VALUE("RES-SER-446", RegistrationConstants.SYSTEM, "Invalid page start value",
			"Invalid page start value %s", "RES-SER", "Residence service", "RS-VAL", "Validation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	INVALID_PAGE_FETCH_VALUE("RES-SER-447", RegistrationConstants.SYSTEM, "Invalid page fetch value",
			"Invalid page fetch value %s", "RES-SER", "Residence service", "RS-VAL", "Validation",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ("RES-SER-281", RegistrationConstants.SYSTEM,
			"Login Request",
			"Login Request", "RES-SER",
			"Resident service", "RS-LOGN", "Login req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ_SUCCESS("RES-SER-282", RegistrationConstants.SYSTEM,
			"Login Request: Success",
			"Login request is success", "RES-SER",
			"Resident service", "RS-LOGN", "Login req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ_FAILURE("RES-SER-283", RegistrationConstants.SYSTEM,
			"Login Request: Failed",
			"Login request is failed", "RES-SER",
			"Resident service", "RS-LOGN", "Login req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ("RES-SER-284", RegistrationConstants.SYSTEM,
			"Logout Request",
			"Logout Request", "RES-SER",
			"Resident service", "RS-LOGN", "Logout req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ_SUCCESS("RES-SER-285", RegistrationConstants.SYSTEM,
			"Logout Request: Success",
			"Logout request is success", "RES-SER",
			"Resident service", "RS-LOGO", "Logout req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ_FAILURE("RES-SER-286", RegistrationConstants.SYSTEM,
			"Logout Request: Failed",
			"Logout request is failed", "RES-SER",
			"Resident service", "RS-LOGO", "Logout req", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_TOKEN_SUCCESS("RES-SER-287", RegistrationConstants.SYSTEM,
			"Validate Token: Success",
			"Validate token is success", "RES-SER",
			"Resident service", "RS-VAL", "Validate token", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_TOKEN_FAILURE("RES-SER-288", RegistrationConstants.SYSTEM,
			"Validate Token: Failed",
			"Validate token is failed", "RES-SER",
			"Resident service", "RS-VAL", "Validate token", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_STATUS_UPDATE_CALL_BACK_SUCCESS("RES-SER-299", RegistrationConstants.SYSTEM,
			"Credential status update call back: Success",
			"credential status update call back success for transaction id %s", "RES-SER", "Residence service",
			"RS-CRED_STAT_UPD", "Credential status update", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_STATUS_UPDATE_CALL_BACK_FAILURE("RES-SER-300", RegistrationConstants.SYSTEM,
			"Credential status update call back: Failed",
			"credential status update call back failure for transaction id %s", "RES-SER", "Residence service",
			"RS-CRED_STAT_UPD", "Credential status update", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	AID_STAGE_SUCCESS("RES-SER-293", RegistrationConstants.SYSTEM, "Check AID stage status: Success",
			"Check AID stage status is success", "RES-SER", "Residence service", "RS-AID_STAGE", "AID stage",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AID_STAGE_FAILURE("RES-SER-294", RegistrationConstants.SYSTEM, "Check AID stage status: Failed",
			"Check AID stage status is failed", "RES-SER", "Residence service", "RS-AID_STAGE", "AID stage",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UNREAD_NOTIF_COUNT_SUCCESS("RES-SER-306", RegistrationConstants.SYSTEM, "Unread notification count: Success",
			"Unread notification count is success", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UNREAD_NOTIF_COUNT_FAILURE("RES-SER-307", RegistrationConstants.SYSTEM, "Unread notification count: Failed",
			"Unread notification count failed", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_NOTIFICATION_SUCCESS("RES-SER-309", RegistrationConstants.SYSTEM, "Get notification: Success",
			"Get notification is Success", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_NOTIFICATION_FAILURE("RES-SER-310", RegistrationConstants.SYSTEM, "Get notification: Failed",
			"Get notification failed", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_NOTIF_CLICK_SUCCESS("RES-SER-311", RegistrationConstants.SYSTEM, "Get notification click: Success",
			"Get notification click is Success", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_NOTIF_CLICK_FAILURE("RES-SER-312", RegistrationConstants.SYSTEM, "Get notification click: Failed",
			"Get notification click failed", "RES-SER", "Residence service", "RS-NOT", "Notification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_PROFILE_SUCCESS("RES-SER-314", RegistrationConstants.SYSTEM, "Get profile: Success",
			"Get profile is Success", "RES-SER", "Residence service", "RS-PROF", "Profile section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_PROFILE_FAILURE("RES-SER-315", RegistrationConstants.SYSTEM, "Get profile: Failed",
			"Get profile failed", "RES-SER", "Residence service", "RS-PROF", "Profile section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TRANSLITERATION_FAILURE("RES-SER-316", RegistrationConstants.SYSTEM, "Transliteration: Failed",
			"Transliteration failed", "RES-SER", "Residence service", "RS-TRAN", "Transliteration section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VERIFICATION_STATUS_SUCCESS("RES-SER-317", RegistrationConstants.SYSTEM, "Verification status: Success",
			"Verification status is success", "RES-SER", "Residence service", "RS-VER", "Verification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VERIFICATION_STATUS_FAILURE("RES-SER-318", RegistrationConstants.SYSTEM, "Verification status: Failed",
			"Verification status is failed", "RES-SER", "Residence service", "RS-VER", "Verification section",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	;
	


	private final String eventId;

	private final String type;

	private String name;

	private String description;

	private String refId;

	private String refIdType;

	private String moduleId;

	private String moduleName;

	private String applicationId;

	private String applicationName;

	private EventEnum(String eventId, String type, String name, String description, String refId, String refIdType,
			String moduleId, String moduleName, String applicationId, String applicationName) {
		this.eventId = eventId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.refId = refId;
		this.refIdType = refIdType;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		this.applicationId = applicationId;
		this.applicationName = applicationName;
	}

	public String getEventId() {
		return eventId;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getModuleId() {
		return moduleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getRefId() {
		return refId;
	}

	public String getRefIdType() {
		return refIdType;
	}

	public void setDescription(String des) {
		this.description = des;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setId(String id) {
		this.refId = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicationName() {
		return applicationName;
	}

	/*
	 * Replace %s value in description and id with second parameter passed
	 */

	public static EventEnum getEventEnumWithValue(EventEnum e, String s) {
		e.setDescription(String.format(e.getDescription(), s));
		String id = e.getRefId();
		if (id!=null && id.equalsIgnoreCase("%s")){
			e.setId(s);
		}
		return e;
	}

	/*
	 * Replace %s value in description and id with second parameter passed and name
	 * property of enum with third parameter
	 */
	public static EventEnum getEventEnumWithValue(EventEnum e, String edescription, String ename) {
		e.setDescription(String.format(e.getDescription(), edescription));
		String id = e.getRefId();
		if (id!=null && id.equalsIgnoreCase("%s"))
			e.setId(edescription);
		e.setName(String.format(e.getName(), ename));
		return e;
	}

	/*
	 * Replace second parameter with %s in name property and in description property
	 */

	public static EventEnum getEventEnumWithDynamicName(EventEnum e, String s) {
		e.setName(Character.toUpperCase(s.charAt(0)) + s.substring(1));
		e.setDescription(String.format(e.getDescription(), s));
		return e;
	}

}
