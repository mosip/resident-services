package io.mosip.resident.util;

import io.mosip.resident.constant.RegistrationConstants;

public enum EventEnum {

	RID_STATUS("RES-SER-101", RegistrationConstants.SYSTEM, "Checking RID status", "Request for checking RID status",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_STATUS_RESPONSE("RES-SER-111", RegistrationConstants.SYSTEM, "Checking RID status", "RID status is %s",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_STATUS_SUCCESS("RES-SER-200", RegistrationConstants.SYSTEM, "Checking RID status",
			"Request for checking RID status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_EUIN("RES-SER-102", RegistrationConstants.SYSTEM, "Request EUIN", "Requesting euin for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_EUIN_SUCCESS("RES-SER-210", RegistrationConstants.SYSTEM, "Request EUIN",
			"Requesting euin for transaction id %s is success", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_PRINTUIN("RES-SER-103", RegistrationConstants.SYSTEM, "Request to print UIN",
			"Requesting print uin for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_PRINTUIN_SUCCESS("RES-SER-201", RegistrationConstants.SYSTEM, "Request to print UIN",
			"Requesting print uin api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_LOCK("RES-SER-104", RegistrationConstants.SYSTEM, "Request auth lock",
			"Requesting auth lock for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_LOCK_SUCCESS("RES-SER-202", RegistrationConstants.SYSTEM, "Request auth lock success",
			"Requesting auth lock api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_UNLOCK("RES-SER-105", RegistrationConstants.SYSTEM, "Request auth unlock",
			"Requesting auth unlock for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_UNLOCK_SUCCESS("RES-SER-203", RegistrationConstants.SYSTEM, "Request auth unlock",
			"Requesting auth unlock api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_HISTORY("RES-SER-106", RegistrationConstants.SYSTEM, "Request auth history",
			"Requesting auth history for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_HISTORY_SUCCESS("RES-SER-204", RegistrationConstants.SYSTEM, "Request auth history",
			"Requesting auth history api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	UPDATE_UIN("RES-SER-107", RegistrationConstants.SYSTEM, "Request update uin",
			"Requesting update uin  api for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UPDATE_UIN_SUCCESS("RES-SER-205", RegistrationConstants.SYSTEM, "Request update uin success",
			"Requesting update uin api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GENERATE_VID("RES-SER-108", RegistrationConstants.SYSTEM, "Request for generating VID",
			"Request for generating VID for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GENERATE_VID_SUCCESS("RES-SER-206", RegistrationConstants.SYSTEM, "Request for generating VID success",
			"Request for generating VID for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REVOKE_VID("RES-SER-109", RegistrationConstants.SYSTEM, "Request for revoking VID",
			"Request for revoking VID for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REVOKE_VID_SUCCESS("RES-SER-207", RegistrationConstants.SYSTEM, "Request for revoking VID success",
			"Request for revoking VID for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_REQUEST("RES-SER-110", RegistrationConstants.SYSTEM, "Validating input request",
			"Validating input request of %s", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_NOTIFICATION_SUCCESS("RES-SER-208", RegistrationConstants.SYSTEM, "%s",
			"Sending notification for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_OTP("RES-SER-113", RegistrationConstants.SYSTEM, "%s", "Validate OTP for %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VALIDATE_OTP_SUCCESS("RES-SER-209", RegistrationConstants.SYSTEM, "%s",
			"Validating OTP for transaction id %s is success", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GETTING_RID_STATUS("RES-SER-116", RegistrationConstants.SYSTEM, "Checking RID status",
			"Getting RID status based on individual id", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_NOTIFICATION_FAILURE("RES-SER-403", RegistrationConstants.SYSTEM, "%s",
			"Failure notification sent for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OBTAINED_RID("RES-SER-114", RegistrationConstants.SYSTEM, "Request print UIN",
			"Obtained RID for transaction id %s while requesting for printing UIN", "RES-SER", "Residence service",
			"%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OBTAINED_RID_UIN_UPDATE("RES-SER-115", RegistrationConstants.SYSTEM, "Request UIN Update",
			"Obtained RID for transaction id %s while requesting for update UIN", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VID_GENERATED("RES-SER-117", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID generated for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_ALREADY_EXISTS("RES-SER-405", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID already exists for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_GENERATION_FAILURE("RES-SER-406", RegistrationConstants.SYSTEM, "Request to generate VID",
			"VID generated failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_JSON_PARSING_EXCEPTION("RES-SER-404", RegistrationConstants.SYSTEM, "%s",
			"JSON parsing exception for transaction id %s while generating VID", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DEACTIVATED_VID("RES-SER-210", RegistrationConstants.SYSTEM, "Request to revoke VID",
			"Deactivated VID for transaction id %s while generating VID", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	VID_REVOKE_EXCEPTION("RES-SER-407", RegistrationConstants.SYSTEM, "Request to revoke VID",
			"Revoking VID failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	RID_NOT_FOUND("RES-SER-408", RegistrationConstants.SYSTEM, "Checking RID status",
			"RID not found while checking for RID status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TOKEN_GENERATION_FAILED("RES-SER-409", RegistrationConstants.SYSTEM, "Generating token", "Token generation failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INPUT_INVALID("RES-SER-410", RegistrationConstants.SYSTEM, "%s", "Invalid input parameter %s", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	API_NOT_AVAILABLE("RES-SER-411", RegistrationConstants.SYSTEM, "%s", "API not available for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	API_RESOURCE_UNACCESS("RES-SER-412", RegistrationConstants.SYSTEM, "%s",
			"Unable to access API resource for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_INVALID("RES-SER-413", RegistrationConstants.SYSTEM, "Check RID", "RID is invalid", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INPUT_DOESNT_EXISTS("RES-SER-414", RegistrationConstants.SYSTEM, "Validating request", "Request does not exists",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TEMPLATE_EXCEPTION("RES-SER-415", RegistrationConstants.SYSTEM, "Get template", "Template Exception", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	TEMPLATE_SUBJECT_EXCEPTION("RES-SER-416", RegistrationConstants.SYSTEM, "Get template",
			"Template subject exception", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	NOTIFICATION_FAILED("RES-SER-417", RegistrationConstants.SYSTEM, "%s", "Notification failed for transaction id %s",
			"RES-SER", "Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	BAD_REQUEST("RES-SER-418", RegistrationConstants.SYSTEM, "%s", "Bad request", "RES-SER", "Residence service",
			"NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_API_RESPONSE("RES-SER-419", RegistrationConstants.SYSTEM, "Checking RID status",
			"Invalid api response while checking RID status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	IO_EXCEPTION("RES-SER-420", RegistrationConstants.SYSTEM, "%s", "IO exception for transaction id %s", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	JSON_PARSING_EXCEPTION("RES-SER-421", RegistrationConstants.SYSTEM, "Request for UIN update",
			"JSON parsing exception for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_VALIDATION_FAILED("RES-SER-422", RegistrationConstants.SYSTEM, "%s",
			"OTP validation failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	BASE_EXCEPTION("RES-SER-401", RegistrationConstants.SYSTEM, "%s", "Base exception for transaction id %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQUEST_FAILED("RES-SER-402", RegistrationConstants.SYSTEM, "%s", "Request failed for transaction id %s", "RES-SER",
			"Residence service", "%s", "Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREATE_PACKET("RES-SER-118", RegistrationConstants.SYSTEM, "Request to create packet", "Started packet creation",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UNKNOWN_EXCEPTION("RES-SER-423", RegistrationConstants.SYSTEM, "Request to create packet",
			"Unknown exception occured", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	PACKET_CREATED("RES-SER-119", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Uploading UIN packet",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_CREATED_FAILURE("RES-SER-425", RegistrationConstants.SYSTEM, "Request to upload UIN packet",
			"Packet sync failure", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_CREATED_EXCEPTION("RES-SER-424", RegistrationConstants.SYSTEM, "Request to create packet",
			"Exception while creating packet", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PACKET_SYNC("RES-SER-120", RegistrationConstants.SYSTEM, "Request to upload UIN packet", "Sync packet", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OTP_GEN("RES-SER-121", RegistrationConstants.SYSTEM, "generating otp", "Request for generating otp", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_GEN_SUCCESS("RES-SER-122", RegistrationConstants.SYSTEM, "generating otp", "otp generation is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_GEN_EXCEPTION("RES-SER-123", RegistrationConstants.SYSTEM, "generating otp", "otp generation is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_REQ("RES-SER-124", RegistrationConstants.SYSTEM, "credential request", "credential request", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_SUCCESS("RES-SER-125", RegistrationConstants.SYSTEM, "credential request",
			"credential request success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_EXCEPTION("RES-SER-126", RegistrationConstants.SYSTEM, "credential request",
			"credential request failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_REQ_STATUS("RES-SER-127", RegistrationConstants.SYSTEM, "credential status",
			"request for credential status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_STATUS_SUCCESS("RES-SER-128", RegistrationConstants.SYSTEM, "credential status",
			"credential req status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_REQ_STATUS_EXCEPTION("RES-SER-129", RegistrationConstants.SYSTEM, "credential status",
			"credential req status is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_CANCEL_REQ("RES-SER-130", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_CANCEL_REQ_SUCCESS("RES-SER-131", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_CANCEL_REQ_EXCEPTION("RES-SER-132", RegistrationConstants.SYSTEM, "credential cancel request",
			"credential cancel request failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CREDENTIAL_TYPES("RES-SER-133", RegistrationConstants.SYSTEM, "credential types", "credential types", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_TYPES_SUCCESS("RES-SER-134", RegistrationConstants.SYSTEM, "credential types",
			"fetch credential type success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CREDENTIAL_TYPES_EXCEPTION("RES-SER-135", RegistrationConstants.SYSTEM, "credential request",
			"fetch credential types failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_CARD("RES-SER-136", RegistrationConstants.SYSTEM, "request for card", "request for card", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CARD_SUCCESS("RES-SER-137", RegistrationConstants.SYSTEM, "request for card", "request for card is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CARD_EXCEPTION("RES-SER-138", RegistrationConstants.SYSTEM, "request for card", "request for card is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_POLICY("RES-SER-139", RegistrationConstants.SYSTEM, "request for policy", "request for policy", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_POLICY_SUCCESS("RES-SER-140", RegistrationConstants.SYSTEM, "request for policy",
			"request for policy is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_POLICY_EXCEPTION("RES-SER-141", RegistrationConstants.SYSTEM, "request for policy",
			"request for policy is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATION_FAILED_EXCEPTION("RES-SER-142", RegistrationConstants.SYSTEM, "Validation failed",
			"Validation failed : %s", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_VALID_DOCUMENT("RES-SER-143", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VALID_DOCUMENT_SUCCESS("RES-SER-144", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VALID_DOCUMENT_EXCEPTION("RES-SER-145", RegistrationConstants.SYSTEM, "get valid documents",
			"get valid documents by lang code is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_LOCATION_HIERARCHY_LEVEL("RES-SER-146", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_HIERARCHY_LEVEL_SUCCESS("RES-SER-147", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_HIERARCHY_LEVEL_EXCEPTION("RES-SER-148", RegistrationConstants.SYSTEM, "get location hierarchy levels",
			"get location hierarchy level by lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IMMEDIATE_CHILDREN("RES-SER-149", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IMMEDIATE_CHILDREN_SUCCESS("RES-SER-150", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IMMEDIATE_CHILDREN_EXCEPTION("RES-SER-151", RegistrationConstants.SYSTEM, "get immediate children",
			"get immediate children by location code and lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_LOCATION_DETAILS("RES-SER-152", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_DETAILS_SUCCESS("RES-SER-153", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code is succeed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LOCATION_DETAILS_EXCEPTION("RES-SER-154", RegistrationConstants.SYSTEM, "get location details",
			"get location details by location code and lang code is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_COORDINATE_SPECIFIC_REG_CENTERS("RES-SER-155", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS_SUCCESS("RES-SER-156", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers is succeed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_COORDINATE_SPECIFIC_REG_CENTERS_EXCEPTION("RES-SER-157", RegistrationConstants.SYSTEM,
			"get coordinate specific registration centers", "get coordinate specific registration centers is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_APPLICANT_VALID_DOCUMENT("RES-SER-158", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_APPLICANT_VALID_DOCUMENT_SUCCESS("RES-SER-159", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_APPLICANT_VALID_DOCUMENT_EXCEPTION("RES-SER-160", RegistrationConstants.SYSTEM, "get applicant valid documents",
			"get applicant valid documents is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_REG_CENTERS_FOR_LOCATION_CODE("RES-SER-161", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE_SUCCESS("RES-SER-162", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code is succeed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_FOR_LOCATION_CODE_EXCEPTION("RES-SER-163", RegistrationConstants.SYSTEM,
			"get registration centers for location code", "get registration centers for location code is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_REG_CENTERS_PAGINATED("RES-SER-164", RegistrationConstants.SYSTEM, "get registration centers paginated",
			"get registration centers paginated", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_PAGINATED_SUCCESS("RES-SER-165", RegistrationConstants.SYSTEM, "get registration centers paginated",
			"get registration centers paginated is succeed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTERS_PAGINATED_EXCEPTION("RES-SER-166", RegistrationConstants.SYSTEM,
			"get registration centers paginated", "get registration centers paginated is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_CONFIGURATION_PROPERTIES("RES-SER-167", RegistrationConstants.SYSTEM, "get resident configuration properties",
			"get resident configuration properties", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_CONFIGURATION_PROPERTIES_SUCCESS("RES-SER-168", RegistrationConstants.SYSTEM,
			"get resident configuration properties success", "get resident configuration properties is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_CONFIGURATION_PROPERTIES_EXCEPTION("RES-SER-169", RegistrationConstants.SYSTEM,
			"get resident configuration properties failure", "get resident configuration properties is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_REG_CENTER_WORKING_DAYS("RES-SER-170", RegistrationConstants.SYSTEM, "get registration center working days",
			"get registration center working days", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTER_WORKING_DAYS_SUCCESS("RES-SER-171", RegistrationConstants.SYSTEM,
			"get registration center working days success", "get registration center working days is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_REG_CENTER_WORKING_DAYS_EXCEPTION("RES-SER-172", RegistrationConstants.SYSTEM,
			"get registration center working days failure", "get registration center working days is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_LATEST_ID_SCHEMA("RES-SER-173", RegistrationConstants.SYSTEM, "get latest id schema", "get latest id schema",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LATEST_ID_SCHEMA_SUCCESS("RES-SER-174", RegistrationConstants.SYSTEM, "get latest id schema success",
			"get latest id schema is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_LATEST_ID_SCHEMA_EXCEPTION("RES-SER-175", RegistrationConstants.SYSTEM, "get latest id schema failure",
			"get latest id schema is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	UPLOAD_DOCUMENT("RES-SER-176", RegistrationConstants.SYSTEM, "Request document upload",
			"Requesting document upload api for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UPLOAD_DOCUMENT_SUCCESS("RES-SER-177", RegistrationConstants.SYSTEM, "Document upload success",
			"document upload success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UPLOAD_DOCUMENT_FAILED("RES-SER-178", RegistrationConstants.SYSTEM, "Document upload failed",
			"document upload failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_DOCUMENTS_METADATA("RES-SER-179", RegistrationConstants.SYSTEM, "Request get documents",
			"Requesting get documents api for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENTS_METADATA_SUCCESS("RES-SER-180", RegistrationConstants.SYSTEM, "Get documents success",
			"get documents success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENTS_METADATA_FAILED("RES-SER-181", RegistrationConstants.SYSTEM, "Get documents failed",
			"Get documents failed for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	REQ_AUTH_LOCK_STATUS("RES-SER-182", RegistrationConstants.SYSTEM, "Request auth lock status",
			"Requesting auth lock status for transaction id %s", "RES-SER", "Residence service", "%s", "Individual id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_LOCK_STATUS_SUCCESS("RES-SER-183", RegistrationConstants.SYSTEM, "Request auth lock status success",
			"Requesting auth lock status api for transaction id %s is success", "RES-SER", "Residence service", "%s",
			"Individual id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_LOCK_STATUS_FAILED("RES-SER-184", RegistrationConstants.SYSTEM, "Request auth lock status failure",
			"Requesting auth lock status api for transaction id %s failed", "RES-SER", "Residence service", "%s",
			"Individual id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	AUTH_TYPE_CALL_BACK("RES-SER-182", RegistrationConstants.SYSTEM, "Request auth type call back url",
			"Requesting auth type call back url for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AUTH_TYPE_CALL_BACK_SUCCESS("RES-SER-183", RegistrationConstants.SYSTEM, "Auth type call back success",
			"auth type call back success for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AUTH_TYPE_CALL_BACK_FAILURE("RES-SER-184", RegistrationConstants.SYSTEM, "Auth type call back failure",
			"auth type call back failure for transaction id %s", "RES-SER", "Residence service", "%s", "Transaction id",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	UPDATE_AUTH_TYPE_STATUS("RES-SER-185", RegistrationConstants.SYSTEM, "Request update auth type status",
			"Requesting update auth type status for transaction id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_PARTNERS_BY_PARTNER_TYPE("RES-SER-186", RegistrationConstants.SYSTEM, "get partners by partner type",
			"get partners by partner type", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_PARTNERS_BY_PARTNER_TYPE_SUCCESS("RES-SER-187", RegistrationConstants.SYSTEM, "get partners by partner type success",
			"get partners by partner type is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_PARTNERS_BY_PARTNER_TYPE_EXCEPTION("RES-SER-188", RegistrationConstants.SYSTEM, "get partners by partner type failure",
			"get partners by partner type is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_AUTH_TXN_DETAILS("RES-SER-189", RegistrationConstants.SYSTEM, "Request auth transaction details",
			"Requesting auth transaction details for individual id %s", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_TXN_DETAILS_FAILURE("RES-SER-190", RegistrationConstants.SYSTEM, "Request auth transaction details failure",
			"Requesting auth transaction details for individual id %s failed", "RES-SER", "Residence service", "%s",
			"Transaction id", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_VIDS("RES-SER-191", RegistrationConstants.SYSTEM, "get vids",
			"get vids", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VIDS_SUCCESS("RES-SER-192", RegistrationConstants.SYSTEM,
			"get vids success", "get vids is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VIDS_EXCEPTION("RES-SER-193", RegistrationConstants.SYSTEM,
			"get vids failure", "get vids is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	OTP_INDIVIDUALID_GEN("RES-SER-194", RegistrationConstants.SYSTEM, "generating otp for Individual ID", "Request for generating otp for Individual ID", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_INDIVIDUALID_GEN_SUCCESS("RES-SER-195", RegistrationConstants.SYSTEM, "generating otp for Individual ID success", "otp generation for Individual ID is success",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	OTP_AID_GEN_EXCEPTION("RES-SER-196", RegistrationConstants.SYSTEM, "generating otp for aid failure", "otp generation for aid is failed",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_INPUT_ATTRIBUTES("RES-SER-197", RegistrationConstants.SYSTEM, "get identity attributes",
			"get identity attributes invoked", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_INPUT_ATTRIBUTES_SUCCESS("RES-SER-198", RegistrationConstants.SYSTEM,
			"get identity attributes success", "get identity attributes is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_INPUT_ATTRIBUTES_EXCEPTION("RES-SER-199", RegistrationConstants.SYSTEM,
			"get identity attributes failure", "get identity attributes has failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	AID_STATUS("RES-SER-210", RegistrationConstants.SYSTEM, "Checking AID status", "Request for checking AID status",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INDIVIDUALID_STATUS("RES-SER-210", RegistrationConstants.SYSTEM, "Checking Individual ID status", "Request for checking Individual ID status",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AID_STATUS_RESPONSE("RES-SER-211", RegistrationConstants.SYSTEM, "Checking AID status Response", "AID status is %s",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	AID_STATUS_SUCCESS("RES-SER-212", RegistrationConstants.SYSTEM, "Checking AID status Success",
			"Request for checking AID status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INDIVIDUALID_STATUS_SUCCESS("RES-SER-212", RegistrationConstants.SYSTEM, "Checking Individual ID status Success",
			"Request for checking Individual ID status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_AUTH_TYPE_LOCK("RES-SER-213", RegistrationConstants.SYSTEM, "Request auth type lock",
			"Requesting auth type lock is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_SERVICE_HISTORY("RES-SER-214", RegistrationConstants.SYSTEM, "get service history",
			"get service history", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_SERVICE_REQUEST_UPDATE("RES-SER-215", RegistrationConstants.SYSTEM, "Request service request update",
			"Requesting service request update is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_SERVICE_REQUEST_UPDATE_SUCCESS("RES-SER-216", RegistrationConstants.SYSTEM, "Request service request update Success",
			"Requesting service request update is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	IDA_TOKEN_NOT_FOUND("RES-SER-217", RegistrationConstants.SYSTEM, "IDA token not found",
			"IDA token not found", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	REQ_CUSTOM_CREDENTIAL("RES-SER-218", RegistrationConstants.SYSTEM, "Custom Credential Request", "Custom Credential Request",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CUSTOM_CREDENTIAL_SUCCESS("RES-SER-219", RegistrationConstants.SYSTEM, "Custom Credential Request Success",
			"Custom Credential Request is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	REQ_CUSTOM_CREDENTIAL_FAILURE("RES-SER-220", RegistrationConstants.SYSTEM, "Custom Credential Request Failure",
			"Custom Credential Request has failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CHANNEL_VERIFICATION_STATUS("RES-SER-221", RegistrationConstants.SYSTEM, "Check Channel Verification status Request", "Check Channel Verification status Request",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHANNEL_VERIFICATION_STATUS_SUCCESS("RES-SER-222", RegistrationConstants.SYSTEM, "Check Channel Verification status Request Success",
			"Check Channel Verification status Request is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHANNEL_VERIFICATION_STATUS_FAILURE("RES-SER-223", RegistrationConstants.SYSTEM, "Custom Credential Request Failure",
			"Custom Credential Request has failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_VID_POLICY("RES-SER-224", RegistrationConstants.SYSTEM, "Get VID Policy Request", "Get VID Policy Request",
			"RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VID_POLICY_SUCCESS("RES-SER-225", RegistrationConstants.SYSTEM, "Get VID Policy Request Success",
			"Get VID Policy Request is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_VID_POLICY_FAILURE("RES-SER-226", RegistrationConstants.SYSTEM, "Get VID Policy Request Failure",
			"Get VID Policy Request has failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IDMAPPING("RES-SER-227", RegistrationConstants.SYSTEM, "get identity mapping json",
			"get identity mapping json", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDMAPPING_SUCCESS("RES-SER-228", RegistrationConstants.SYSTEM,
			"get identity mapping json success", "get identity mapping json is succeeded", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDMAPPING_EXCEPTION("RES-SER-229", RegistrationConstants.SYSTEM,
			"get identity mapping json failure", "get identity mapping json is failed", "RES-SER",
			"Residence service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	RID_DIGITAL_CARD_REQ("RES-SER-230", RegistrationConstants.SYSTEM, "RID digital card request",
			"Request for downloading digital card based on RID", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_DIGITAL_CARD_REQ_SUCCESS("RES-SER-231", RegistrationConstants.SYSTEM, "RID digital card request",
			"Downloading digital card based on RID success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_DIGITAL_CARD_REQ_FAILURE("RES-SER-231", RegistrationConstants.SYSTEM, "RID digital card request",
			"Downloading digital card based on RID failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	RID_DIGITAL_CARD_REQ_EXCEPTION("RES-SER-232", RegistrationConstants.SYSTEM, "RID digital card request",
			"Downloading digital card based on RID failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CHECK_AID_STATUS_REQUEST("RES-SER-233", RegistrationConstants.SYSTEM, "Request Application status",
			"Requesting application status", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHECK_AID_STATUS_REQUEST_SUCCESS("RES-SER-234", RegistrationConstants.SYSTEM, "Request credential request status success",
			"Requesting credential request status is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	CHECK_AID_STATUS_REQUEST_FAILED("RES-SER-279", RegistrationConstants.SYSTEM, "Request credential request status failed",
			"Requesting credential request status failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	CHECK_ORDER_STATUS_EXCEPTION("RES-SER-235", RegistrationConstants.SYSTEM, "check order status",
			"check order status is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_PHYSICAL_CARD("RES-SER-236", RegistrationConstants.SYSTEM, "send a physical card",
			"send a physical card", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_PHYSICAL_CARD_SUCCESS("RES-SER-237", RegistrationConstants.SYSTEM, "send a physical card",
			"send a physical card is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	SEND_PHYSICAL_CARD_EXCEPTION("RES-SER-238", RegistrationConstants.SYSTEM, "send a physical card",
			"send a physical card is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_BY_DOC_ID("RES-SER-239", RegistrationConstants.SYSTEM, "get document by doc id",
			"get document by doc id", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_BY_DOC_ID_SUCCESS("RES-SER-240", RegistrationConstants.SYSTEM, "get document by doc id",
			"get document by doc id is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_BY_DOC_ID_FAILED("RES-SER-241", RegistrationConstants.SYSTEM, "get document by doc id",
			"get document by doc id is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DELETE_DOCUMENT("RES-SER-242", RegistrationConstants.SYSTEM, "delete document",
			"delete document", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DELETE_DOCUMENT_SUCCESS("RES-SER-243", RegistrationConstants.SYSTEM, "delete document Success",
			"delete document is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DELETE_DOCUMENT_FAILED("RES-SER-244", RegistrationConstants.SYSTEM, "delete document Failed",
			"delete document is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_TEMPLATES("RES-SER-245", RegistrationConstants.SYSTEM, "get templates",
			"get templates by langCode and templateTypeCode", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_TEMPLATES_SUCCESS("RES-SER-246", RegistrationConstants.SYSTEM, "get templates success",
			"get templates by langCode and templateTypeCode is succeeded", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_TEMPLATES_EXCEPTION("RES-SER-247", RegistrationConstants.SYSTEM, "get templates failure",
			"get templates by langCode and templateTypeCode is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_LANGUAGE_CODE("RES-SER-500", RegistrationConstants.SYSTEM, "Invalid language code",
			"invalid lang code", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	GET_IDENTITY_UPDATE_COUNT("RES-SER-248", RegistrationConstants.SYSTEM, "get identity update count",
			"retrieve remaining update counts for each id attributes for a UIN/VID", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDENTITY_UPDATE_COUNT_SUCCESS("RES-SER-249", RegistrationConstants.SYSTEM, "get identity update count success",
			"retrieve remaining update counts for each id attributes for a UIN/VID is succeeded", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_IDENTITY_UPDATE_COUNT_EXCEPTION("RES-SER-250", RegistrationConstants.SYSTEM, "get identity update count failure",
			"retrieve remaining update counts for each id attributes for a UIN/VID is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PIN_STATUS("RES-SER-251", RegistrationConstants.SYSTEM, "pin status", "pin status based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PIN_STATUS_SUCCESS("RES-SER-252", RegistrationConstants.SYSTEM, "pin status success", "pin status success based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	PIN_STATUS_FAILURE("RES-SER-253", RegistrationConstants.SYSTEM, "pin status", "pin status failure based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	UN_PIN_STATUS("RES-SER-254", RegistrationConstants.SYSTEM, "un pin status", "un pin status based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UN_PIN_STATUS_SUCCESS("RES-SER-255", RegistrationConstants.SYSTEM, "un pin status success", "un pin status success based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	UN_PIN_STATUS_FAILURE("RES-SER-256", RegistrationConstants.SYSTEM, "un pin status failure", "un pin status failure based on event id", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_ACKNOWLEDGEMENT_DOWNLOAD_URL("RES-SER-257", RegistrationConstants.SYSTEM, "get acknowledgement download url",
			"get acknowledgement download url", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_SUCCESS("RES-SER-258", RegistrationConstants.SYSTEM, "get acknowledgement download url success",
			"get acknowledgement download url is succeeded", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_ACKNOWLEDGEMENT_DOWNLOAD_URL_FAILURE("RES-SER-258", RegistrationConstants.SYSTEM, "get acknowledgement download url failed",
			"get acknowledgement download url failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	SEND_OTP_FAILURE("RES-SER-259", RegistrationConstants.SYSTEM, "send otp failure",
			"send otp is failed", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	SEND_OTP_SUCCESS("RES-SER-259", RegistrationConstants.SYSTEM, "send otp success",
			"send otp is success", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_SERVICE_HISTORY("RES-SER-260", RegistrationConstants.SYSTEM, "download service histor",
			"get service history pdf", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_SERVICE_HISTORY_SUCCESS("RES-SER-266", RegistrationConstants.SYSTEM, "down load service history success", "download service history success based on language code", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GET_GENDER_TYPES("RES-SER-261", RegistrationConstants.SYSTEM, "get gender types",
			"get gender types by langCode", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_TYPES_SUCCESS("RES-SER-262", RegistrationConstants.SYSTEM, "get gender types success",
			"get gender types by langCode is succeeded", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_TYPES_EXCEPTION("RES-SER-263", RegistrationConstants.SYSTEM, "get gender types failure",
			"get gender types by langCode is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	INVALID_REQUEST_TYPE_CODE("RES-SER-267", RegistrationConstants.SYSTEM, "Invalid request type code", "\"Invalid Request Type. Please input eventId only for VID_CARD_DOWNLOAD,\" +\n" +
			"\t\t\t\"DOWNLOAD_PERSONALIZED_CARD, UPDATE_MY_UIN", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DOWNLOAD_PERSONALIZED_CARD("RES-SER-268", RegistrationConstants.SYSTEM, "Download personalized card",
			"Download card Html to pdf", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	DOWNLOAD_REGISTRATION_CENTER("RES-SER-269", RegistrationConstants.SYSTEM, "download registration center",
			"download registration center", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_REGISTRATION_CENTER_SUCCESS("RES-SER-270", RegistrationConstants.SYSTEM,
			"download registration center success",
			"download registration center success based on language code and hierarchy level", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	DOWNLOAD_SUPPORTING_DOCS("RES-SER-271", RegistrationConstants.SYSTEM, "download supporting docs",
			"ownload supporting docs", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_SUPPORTING_DOCS_SUCCESS("RES-SER-272", RegistrationConstants.SYSTEM, "download supporting docs success",
			"download supporting docs success based on language code", "RES-SER", "Resident service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),

	DOWNLOAD_REGISTRATION_CENTER_NEAREST_SUCCESS("RES-SER-273", RegistrationConstants.SYSTEM,
			"download registration center success",
			"download registration center success based on language code,longitude,latitude and distance", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),

	GRIEVANCE_TICKET_REQUEST("RES-SER-273", RegistrationConstants.SYSTEM,
			"Grievance ticket request",
			"Grievance ticket request", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GRIEVANCE_TICKET_REQUEST_SUCCESS("RES-SER-273", RegistrationConstants.SYSTEM,
			"Grievance ticket request success",
			"Grievance ticket request success", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GRIEVANCE_TICKET_REQUEST_FAILED("RES-SER-280", RegistrationConstants.SYSTEM,
			"Grievance ticket request failed",
			"Grievance ticket request failed", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_TYPES("RES-SER-274", RegistrationConstants.SYSTEM, "get document types",
			"get document types by documentCode and langCode", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_TYPES_SUCCESS("RES-SER-275", RegistrationConstants.SYSTEM, "get document types success",
			"get document types by documentCode and langCode is succeeded", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_DOCUMENT_TYPES_EXCEPTION("RES-SER-276", RegistrationConstants.SYSTEM, "get document types failure",
			"get document types by documentCode and langCode is failed", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_CODE("RES-SER-277", RegistrationConstants.SYSTEM, "get gender code",
			"get gender code by genderName and langCode", "RES-SER", "Residence service", "NO_ID", "NO_ID_TYPE",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	GET_GENDER_CODE_SUCCESS("RES-SER-278", RegistrationConstants.SYSTEM, "get gender code success",
			"get gender code by genderName and langCode is succeeded", "RES-SER", "Residence service", "NO_ID",
			"NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	INVALID_PAGE_START_VALUE("RES-SER-446", RegistrationConstants.SYSTEM, "%s",
			"Invalid page start value %s", "RES-SER", "Residence service", "%s", "pageStart",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	INVALID_PAGE_FETCH_VALUE("RES-SER-447", RegistrationConstants.SYSTEM, "%s",
			"Invalid page fetch value %s", "RES-SER", "Residence service", "%s", "pageFetch",
			RegistrationConstants.RESIDENT_APPLICATION_ID, RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ("RES-SER-281", RegistrationConstants.SYSTEM,
			"Login Request",
			"Login Request", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ_SUCCESS("RES-SER-282", RegistrationConstants.SYSTEM,
			"Login Request Success",
			"Login request is success", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGIN_REQ_FAILURE("RES-SER-283", RegistrationConstants.SYSTEM,
			"Login Request Failed",
			"Login request is failed", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ("RES-SER-284", RegistrationConstants.SYSTEM,
			"Logout Request",
			"Logout Request", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ_SUCCESS("RES-SER-285", RegistrationConstants.SYSTEM,
			"Logout Request Success",
			"Logout request is success", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	LOGOUT_REQ_FAILURE("RES-SER-286", RegistrationConstants.SYSTEM,
			"Logout Request Failed",
			"Logout request is failed", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_TOKEN_SUCCESS("RES-SER-287", RegistrationConstants.SYSTEM,
			"Validate Token Success",
			"Validate token is success", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME),
	
	VALIDATE_TOKEN_FAILURE("RES-SER-288", RegistrationConstants.SYSTEM,
			"Validate Token Failed",
			"Validate token is failed", "RES-SER",
			"Resident service", "NO_ID", "NO_ID_TYPE", RegistrationConstants.RESIDENT_APPLICATION_ID,
			RegistrationConstants.RESIDENT_APPLICATION_NAME);
	


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
		String id = e.getId();
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
		String id = e.getId();
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
