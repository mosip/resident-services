package io.mosip.resident.util;

import io.mosip.resident.constant.RegistrationConstants;

public enum EventEnum {

	RID_STATUS("RES-SER-101", RegistrationConstants.SYSTEM, "Checking RID status", "Request for checking RID status",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	RID_STATUS_RESPONSE("RES-SER-111", RegistrationConstants.SYSTEM, "Checking RID status", "RID status is %s",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	RID_STATUS_SUCCESS("RES-SER-200", RegistrationConstants.SYSTEM, "Checking RID status",
			"Request for checking RID status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_EUIN("RES-SER-102", RegistrationConstants.SYSTEM, "Request EUIN", "Requesting euin for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQ_EUIN_SUCCESS("RES-SER-210", RegistrationConstants.SYSTEM, "Request EUIN",
			"Requesting euin for transaction id %s is success", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_PRINTUIN("RES-SER-103", RegistrationConstants.SYSTEM, "Request to print UIN",
			"Requesting print uin for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_PRINTUIN_SUCCESS("RES-SER-201", RegistrationConstants.SYSTEM, "Request to print UIN",
			"Requesting print uin api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_LOCK("RES-SER-104", RegistrationConstants.SYSTEM, "Request auth lock",
			"Requesting auth lock for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_LOCK_SUCCESS("RES-SER-202", RegistrationConstants.SYSTEM, "Request auth lock",
			"Requesting auth lock api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_UNLOCK("RES-SER-105", RegistrationConstants.SYSTEM, "Request auth unlock",
			"Requesting auth unlock for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_UNLOCK_SUCCESS("RES-SER-203", RegistrationConstants.SYSTEM, "Request auth unlock",
			"Requesting auth unlock api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_HISTORY("RES-SER-106", RegistrationConstants.SYSTEM, "Request auth history",
			"Requesting auth history for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_HISTORY_SUCCESS("RES-SER-204", RegistrationConstants.SYSTEM, "Request auth history",
			"Requesting auth history api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPDATE_UIN("RES-SER-107", RegistrationConstants.SYSTEM, "Request update uin",
			"Requesting update uin  api for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPDATE_UIN_SUCCESS("RES-SER-205", RegistrationConstants.SYSTEM, "Request update uin",
			"Requesting update uin api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GENERATE_VID("RES-SER-108", RegistrationConstants.SYSTEM, "Request for generating VID",
			"Request for generating VID for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GENERATE_VID_SUCCESS("RES-SER-206", RegistrationConstants.SYSTEM, "Request for generating VID",
			"Request for generating VID for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REVOKE_VID("RES-SER-109", RegistrationConstants.SYSTEM, "Request for revoking VID",
			"Request for revoking VID for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REVOKE_VID_SUCCESS("RES-SER-207", RegistrationConstants.SYSTEM, "Request for revoking VID",
			"Request for revoking VID for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VALIDATE_REQUEST("RES-SER-110", RegistrationConstants.SYSTEM, "Validating input request",
			"Validating input request of %s", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	SEND_NOTIFICATION_SUCCESS("RES-SER-208", RegistrationConstants.SYSTEM, "%s",
			"Sending notification for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VALIDATE_OTP("RES-SER-113", RegistrationConstants.SYSTEM, "%s", "Validate OTP for %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	VALIDATE_OTP_SUCCESS("RES-SER-209", RegistrationConstants.SYSTEM, "%s",
			"Validating OTP for transaction id %s is success", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GETTING_RID_STATUS("RES-SER-116", RegistrationConstants.SYSTEM, "Checking RID status",
			"Getting RID status based on individual id", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	SEND_NOTIFICATION_FAILURE("RES-SER-403", RegistrationConstants.SYSTEM, "%s",
			"Failure notification sent for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	OBTAINED_RID("RES-SER-114", RegistrationConstants.SYSTEM, "Request print UIN",
			"Obtained RID for transaction id %s while requesting for printing UIN", "RES-SER", "Residence service",
			"%s", "Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	OBTAINED_RID_UIN_UPDATE("RES-SER-115", RegistrationConstants.SYSTEM, "Request UIN Update",
			"Obtained RID for transaction id %s while requesting for update UIN", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VID_GENERATED("RES-SER-117", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID generated for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VID_ALREADY_EXISTS("RES-SER-405", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID already exists for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VID_GENERATION_FAILURE("RES-SER-406", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID generated failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VID_JSON_PARSING_EXCEPTION("RES-SER-404", RegistrationConstants.SYSTEM, "%s",
			"JSON parsing exception for transaction id %s while generating VID", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	DEACTIVATED_VID("RES-SER-210", RegistrationConstants.SYSTEM, "Request to revoke VID",
			"Deactivated VID for transaction id %s while generating VID", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VID_REVOKE_EXCEPTION("RES-SER-407", RegistrationConstants.SYSTEM, "Request to revoke VID",
			"Revoking VID failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),

	RID_NOT_FOUND("RES-SER-408", RegistrationConstants.SYSTEM, "Checking RID status",
			"RID not found while checking for RID status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	TOKEN_GENERATION_FAILED("RES-SER-409", RegistrationConstants.SYSTEM, "Generating token", "Token generation failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	INPUT_INVALID("RES-SER-410", RegistrationConstants.SYSTEM, "%s", "Invalid input parameter %s", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	API_NOT_AVAILABLE("RES-SER-411", RegistrationConstants.SYSTEM, "%s", "API not available for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	API_RESOURCE_UNACCESS("RES-SER-412", RegistrationConstants.SYSTEM, "%s",
			"Unable to access API resource for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	RID_INVALID("RES-SER-413", RegistrationConstants.SYSTEM, "Check RID", "RID is invalid", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	INPUT_DOESNT_EXISTS("RES-SER-414", RegistrationConstants.SYSTEM, "Validating request", "Request does not exists",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	TEMPLATE_EXCEPTION("RES-SER-415", RegistrationConstants.SYSTEM, "Get template", "Template Exception", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	TEMPLATE_SUBJECT_EXCEPTION("RES-SER-416", RegistrationConstants.SYSTEM, "Get template",
			"Template subject exception", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	NOTIFICATION_FAILED("RES-SER-417", RegistrationConstants.SYSTEM, "%s", "Notification failed for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	BAD_REQUEST("RES-SER-418", RegistrationConstants.SYSTEM, "%s", "Bad request", "RES-SER", "Residence service",
			"NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	INVALID_API_RESPONSE("RES-SER-419", RegistrationConstants.SYSTEM, "Checking RID status",
			"Invalid api response while checking RID status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	IO_EXCEPTION("RES-SER-420", RegistrationConstants.SYSTEM, "%s", "IO exception for transaction id %s", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	JSON_PARSING_EXCEPTION("RES-SER-421", RegistrationConstants.SYSTEM, "Request for UIN update",
			"JSON parsing exception for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	OTP_VALIDATION_FAILED("RES-SER-422", RegistrationConstants.SYSTEM, "%s",
			"OTP validation failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	BASE_EXCEPTION("RES-SER-401", RegistrationConstants.SYSTEM, "%s", "Base exception for transaction id %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQUEST_FAILED("RES-SER-402", RegistrationConstants.SYSTEM, "%s", "Request failed for transaction id %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	CREATE_PACKET("RES-SER-118", RegistrationConstants.SYSTEM, "Request to create packet", "Started packet creation",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	UNKNOWN_EXCEPTION("RES-SER-423", RegistrationConstants.SYSTEM, "Request to create packet",
			"Unknown exception occured", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	PACKET_CREATED("RES-SER-119", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Uploading UIN packet",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	PACKET_CREATED_FAILURE("RES-SER-425", RegistrationConstants.SYSTEM, "Request to upload UIN packet",
			"Packet sync failure", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	PACKET_CREATED_EXCEPTION("RES-SER-424", RegistrationConstants.SYSTEM, "Request to create packet",
			"Exception while creating packet", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	PACKET_SYNC("RES-SER-120", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Sync packet", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	OTP_GEN("RES-SER-121", RegistrationConstants.SYSTEM, "generating otp", "Request for generating otp", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	OTP_GEN_SUCCESS("RES-SER-122", RegistrationConstants.SYSTEM, "generating otp", "otp generation is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	OTP_GEN_EXCEPTION("RES-SER-123", RegistrationConstants.SYSTEM, "generating otp", "otp generation is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ("RES-SER-124", RegistrationConstants.SYSTEM, "credential request", "credential request", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ_SUCCESS("RES-SER-125", RegistrationConstants.SYSTEM, "credential request",
			"credential request success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ_EXCEPTION("RES-SER-126", RegistrationConstants.SYSTEM, "credential request",
			"credential request failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ_STATUS("RES-SER-127", RegistrationConstants.SYSTEM, "credential status",
			"request for credential status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ_STATUS_SUCCESS("RES-SER-128", RegistrationConstants.SYSTEM, "credential status",
			"credential req status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_REQ_STATUS_EXCEPTION("RES-SER-129", RegistrationConstants.SYSTEM, "credential status",
			"credential req status is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_CANCEL_REQ("RES-SER-130", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_CANCEL_REQ_SUCCESS("RES-SER-131", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_CANCEL_REQ_EXCEPTION("RES-SER-132", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_TYPES("RES-SER-133", RegistrationConstants.SYSTEM, "credential types", "credential types", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_TYPES_SUCCESS("RES-SER-134", RegistrationConstants.SYSTEM, "credential types",
			"fetch credential type success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	CREDENTIAL_TYPES_EXCEPTION("RES-SER-135", RegistrationConstants.SYSTEM, "credential request",
			"fetch credential types failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_CARD("RES-SER-136", RegistrationConstants.SYSTEM, "request for card", "request for card", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQ_CARD_SUCCESS("RES-SER-137", RegistrationConstants.SYSTEM, "request for card", "request for card is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQ_CARD_EXCEPTION("RES-SER-138", RegistrationConstants.SYSTEM, "request for card", "request for card is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQ_POLICY("RES-SER-139", RegistrationConstants.SYSTEM, "request for policy", "request for policy", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	REQ_POLICY_SUCCESS("RES-SER-140", RegistrationConstants.SYSTEM, "request for policy",
			"request for policy is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_POLICY_EXCEPTION("RES-SER-141", RegistrationConstants.SYSTEM, "request for policy",
			"request for policy is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	VALIDATION_FAILED_EXCEPTION("RES-SER-142", RegistrationConstants.SYSTEM, "Validation failed",
			"Validation failed : %s", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_VALID_DOCUMENT("RES-SER-143", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_VALID_DOCUMENT_SUCCESS("RES-SER-144", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_VALID_DOCUMENT_EXCEPTION("RES-SER-145", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_HIERARCHY_LEVEL("RES-SER-146", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_HIERARCHY_LEVEL_SUCCESS("RES-SER-147", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION("RES-SER-148", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_IMMEDIATE_CHILDREN("RES-SER-149", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_IMMEDIATE_CHILDREN_SUCCESS("RES-SER-150", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_IMMEDIATE_CHILDREN_EXCEPTION("RES-SER-151", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_DETAILS("RES-SER-152", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_DETAILS_SUCCESS("RES-SER-153", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LOCATION_DETAILS_EXCEPTION("RES-SER-154", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS("RES-SER-155", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS_SUCCESS("RES-SER-156", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers is succeed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION("RES-SER-157", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_APPLICANT_VALID_DOCUMENT("RES-SER-158", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_APPLICANT_VALID_DOCUMENT_SUCCESS("RES-SER-159", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_APPLICANT_VALID_DOCUMENT_EXCEPTION("RES-SER-160", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE("RES-SER-161", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE_SUCCESS("RES-SER-162", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code is succeed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION("RES-SER-163", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_PAGINATED("RES-SER-164", RegistrationConstants.SYSTEM, "get registration centers paginated",
			"get registration centers paginated", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_PAGINATED_SUCCESS("RES-SER-165", RegistrationConstants.SYSTEM, "get registration centers paginated",
			"get registration centers paginated is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTERS_PAGINATED_EXCEPTION("RES-SER-166", RegistrationConstants.SYSTEM,
			"get registration centers paginated", "get registration centers paginated is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_CONFIGURATION_PROPERTIES("RES-SER-167", RegistrationConstants.SYSTEM, "get resident configuration properties",
			"get resident configuration properties", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_CONFIGURATION_PROPERTIES_SUCCESS("RES-SER-168", RegistrationConstants.SYSTEM,
			"get resident configuration properties", "get resident configuration properties is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_CONFIGURATION_PROPERTIES_EXCEPTION("RES-SER-169", RegistrationConstants.SYSTEM,
			"get resident configuration properties", "get resident configuration properties is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_REG_CENTER_WORKING_DAYS("RES-SER-170", RegistrationConstants.SYSTEM, "get registration center working days",
			"get registration center working days", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTER_WORKING_DAYS_SUCCESS("RES-SER-171", RegistrationConstants.SYSTEM,
			"get registration center working days", "get registration center working days is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_REG_CENTER_WORKING_DAYS_EXCEPTION("RES-SER-172", RegistrationConstants.SYSTEM,
			"get registration center working days", "get registration center working days is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_LATEST_ID_SCHEMA("RES-SER-173", RegistrationConstants.SYSTEM, "get latest id schema", "get latest id schema",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	GET_LATEST_ID_SCHEMA_SUCCESS("RES-SER-174", RegistrationConstants.SYSTEM, "get latest id schema",
			"get latest id schema is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_LATEST_ID_SCHEMA_EXCEPTION("RES-SER-175", RegistrationConstants.SYSTEM, "get latest id schema",
			"get latest id schema is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPLOAD_DOCUMENT("RES-SER-176", RegistrationConstants.SYSTEM, "Request document upload",
			"Requesting document upload api for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPLOAD_DOCUMENT_SUCCESS("RES-SER-177", RegistrationConstants.SYSTEM, "Document upload success",
			"document upload success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPLOAD_DOCUMENT_FAILED("RES-SER-178", RegistrationConstants.SYSTEM, "Document upload failed",
			"document upload failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_DOCUMENTS_METADATA("RES-SER-179", RegistrationConstants.SYSTEM, "Request get documents",
			"Requesting get documents api for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_DOCUMENTS_METADATA_SUCCESS("RES-SER-180", RegistrationConstants.SYSTEM, "Get documents success",
			"get documents success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_DOCUMENTS_METADATA_FAILED("RES-SER-181", RegistrationConstants.SYSTEM, "Get documents failed",
			"Get documents failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),

	REQ_AUTH_LOCK_STATUS("RES-SER-182", RegistrationConstants.SYSTEM, "Request auth lock status",
			"Requesting auth lock status for individual id %s", "RES-SER", "Residence service", "%s", "Individual id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_LOCK_STATUS_SUCCESS("RES-SER-183", RegistrationConstants.SYSTEM, "Request auth lock status",
			"Requesting auth lock status api for individual id %s is success", "RES-SER", "Residence service", "%s",
			"Individual id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_LOCK_STATUS_FAILED("RES-SER-184", RegistrationConstants.SYSTEM, "Request auth lock status",
			"Requesting auth lock status api for individual id %s failed", "RES-SER", "Residence service", "%s",
			"Individual id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),

	AUTH_TYPE_CALL_BACK("RES-SER-182", RegistrationConstants.SYSTEM, "Request auth type call back url",
			"Requesting auth type call back url for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	AUTH_TYPE_CALL_BACK_SUCCESS("RES-SER-183", RegistrationConstants.SYSTEM, "Auth type call back success",
			"auth type call back success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	AUTH_TYPE_CALL_BACK_FAILURE("RES-SER-184", RegistrationConstants.SYSTEM, "Auth type call back failure",
			"auth type call back failure for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	UPDATE_AUTH_TYPE_STATUS("RES-SER-185", RegistrationConstants.SYSTEM, "Request update auth type status",
			"Requesting update auth type status for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),

	GET_PARTNERS_BY_PARTNER_TYPE("RES-SER-186", RegistrationConstants.SYSTEM, "get partners by partner type",
			"get partners by partner type", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_PARTNERS_BY_PARTNER_TYPE_SUCCESS("RES-SER-187", RegistrationConstants.SYSTEM, "get partners by partner type",
			"get partners by partner type is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_PARTNERS_BY_PARTNER_TYPE_EXCEPTION("RES-SER-188", RegistrationConstants.SYSTEM, "get partners by partner type",
			"get partners by partner type is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_TXN_DETAILS("RES-SER-189", RegistrationConstants.SYSTEM, "Request auth transaction details",
			"Requesting auth transaction details for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_TXN_DETAILS_FAILURE("RES-SER-190", RegistrationConstants.SYSTEM, "Request auth transaction details",
			"Requesting auth transaction details for transaction id %s failed", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),


	GET_VIDS("RES-SER-191", RegistrationConstants.SYSTEM, "get vids",
			"get vids", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_VIDS_SUCCESS("RES-SER-192", RegistrationConstants.SYSTEM,
			"get vids", "get vids is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_VIDS_EXCEPTION("RES-SER-193", RegistrationConstants.SYSTEM,
			"get vids", "get vids is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	
	OTP_AID_GEN("RES-SER-194", RegistrationConstants.SYSTEM, "generating otp for aid", "Request for generating otp for aid", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	OTP_AID_GEN_SUCCESS("RES-SER-195", RegistrationConstants.SYSTEM, "generating otpfor aid", "otp generation for aid is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	OTP_AID_GEN_EXCEPTION("RES-SER-196", RegistrationConstants.SYSTEM, "generating otp for aid", "otp generation for aid is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	
	GET_INPUT_ATTRIBUTES("RES-SER-197", RegistrationConstants.SYSTEM, "get input attributes",
			"get input attributes", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_INPUT_ATTRIBUTES_SUCCESS("RES-SER-198", RegistrationConstants.SYSTEM,
			"get input attributes", "get input attributes is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),

	GET_INPUT_ATTRIBUTES_EXCEPTION("RES-SER-199", RegistrationConstants.SYSTEM,
			"get input attributes", "get input attributes is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	
	AID_STATUS("RES-SER-210", RegistrationConstants.SYSTEM, "Checking RID status", "Request for checking RID status",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	AID_STATUS_RESPONSE("RES-SER-211", RegistrationConstants.SYSTEM, "Checking RID status", "RID status is %s",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.APPLICATIONID,
			RegistrationConstants.APPLICATIONNAME),
	AID_STATUS_SUCCESS("RES-SER-212", RegistrationConstants.SYSTEM, "Checking RID status",
			"Request for checking RID status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_AUTH_TYPE_LOCK("RES-SER-213", RegistrationConstants.SYSTEM, "Request auth type lock",
			"Requesting auth type lock is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	GET_SERVICE_HISTORY("RES-SER-214", RegistrationConstants.SYSTEM, "get service history",
			"get service history", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_SERVICE_REQUEST_UPDATE("RES-SER-215", RegistrationConstants.SYSTEM, "Request service request update",
			"Requesting service request update is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	REQ_SERVICE_REQUEST_UPDATE_SUCCESS("RES-SER-216", RegistrationConstants.SYSTEM, "Request service request update Success",
			"Requesting service request update is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	IDA_TOKEN_NOT_FOUND("RES-SER-217", RegistrationConstants.SYSTEM, "IDA token not found",
			"IDA token not found", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.APPLICATIONID, RegistrationConstants.APPLICATIONNAME),
	;

	private final String eventId;

	private final String type;

	private String name;

	private String description;

	private String moduleId;

	private String moduleName;

	private String id;

	private String idType;

	private String applicationId;

	private String applicationName;

	private EventEnum(String eventId, String type, String name, String description, String moduleId, String moduleName,
			String id, String idType, String applicationId, String applicationName) {
		this.eventId = eventId;
		this.type = type;
		this.name = name;
		this.description = description;
		this.moduleId = moduleId;
		this.moduleName = moduleName;
		this.id = id;
		this.idType = idType;
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

	public String getId() {
		return id;
	}

	public String getIdType() {
		return idType;
	}

	public void setDescription(String des) {
		this.description = des;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setId(String id) {
		this.id = id;
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
		if (e.getId().equalsIgnoreCase("%s"))
			e.setId(s);
		return e;
	}

	/*
	 * Replace %s value in description and id with second parameter passed and name
	 * property of enum with third parameter
	 */
	public static EventEnum getEventEnumWithValue(EventEnum e, String edescription, String ename) {
		e.setDescription(String.format(e.getDescription(), edescription));
		if (e.getId().equalsIgnoreCase("%s"))
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
