package io.mosip.resident.constant;

public enum ResidentErrorCode {

	NO_RID_FOUND_EXCEPTION("RES-SER-408", "RID not found"),
	INVALID_REQUEST_EXCEPTION("RES-SER-002", "One or more input parameter is invalid or does not exist"),
	TOKEN_GENERATION_FAILED("RES-SER-409", "Token generation failed"),
	OTP_VALIDATION_FAILED("RES-SER-422", "OTP validation failed"),
	API_RESOURCE_UNAVAILABLE("RES-SER-411", "API resource is not available"),
	API_RESOURCE_ACCESS_EXCEPTION("RES-SER-412", "Unable to access API resource"),
	CREDENTIAL_ISSUED_EXCEPTION("RES-SER-24", "Credential is issued"),
	VID_CREATION_EXCEPTION("RES-SER-406", "Exception while creating VID"),
	VID_ALREADY_PRESENT("RES-SER-405", "Maximum allowed VIDs are active. Deactivate VID to generate new one."),
	INVALID_INPUT("RES-SER-410", "Invalid Input Parameter- "), INVALID_VID("RES-SER-010", "Invalid VID"),
	INVALID_UIN("RES-SER-011", "Invalid UIN"), INVALID_RID("RES-SER-413", "Invalid RID"),
	INVALID_VID_UIN("RES-SER-013", "Invalid UIN for given VID"),
	REQUEST_FAILED("RES-SER-402", "Your request is not successful, please try again later."),
	TEMPLATE_EXCEPTION("RES-SER-415", "Template exception"),
	TEMPLATE_SUBJECT_EXCEPTION("RES-SER-416", "Template subject exception"),
	NOTIFICATION_FAILURE("RES-SER-417", "Sending notification(Email and SMS) to resident failed."),
	IN_VALID_UIN_OR_VID_OR_RID("RES-SER-018", "Invalid individualId"),
	RE_PRINT_REQUEST_FAILED("RES-SER-019", "Re print UIN request failed"),
	VID_REVOCATION_EXCEPTION("RES-SER-407",
			"VID revocation request failed. Please visit the nearest registration center for assistance."),
	BAD_REQUEST("RES-SER-418", "Bad Request"), INVALID_API_RESPONSE("RES-SER-419", "Invalid APi response from - "),
	UIN_UPDATE_FAILED("RES-SER-22", "Resident UIN update failed"),
	DOCUMENT_NOT_FOUND("RES-SER-23", "Could not find the submitted document"),
	// system exceptions
	RESIDENT_SYS_EXCEPTION("RES-SER-SYS-001", "System exception occured"),
	IO_EXCEPTION("RES-SER-420", "IO Exception occured"),
	JSON_PROCESSING_EXCEPTION("RES-SER-421", "JSON Processing Exception occured"),
	INVALID_RID_EXCEPTION("RES-TUG-001", "RID entered is not valid"),
	INVLAID_KEY_EXCEPTION("RES-SER-25", "Exception occured while encryting the packet Invalid Key"),
	UNKNOWN_EXCEPTION("RES-SER-423", "Unknown exception occured."), BASE_EXCEPTION("RES-SER-401", "Base exception."),
	PACKET_CREATION_EXCEPTION("RES-SER-424", "Exception while creating packet."),
	INVALID_ID("RES-SER-29", "Invalid id"),
	OTP_GENERATION_EXCEPTION("RES-SER-425", "while generating otp error is occured"),
	POLICY_EXCEPTION("RES-SER-426", "while retrieving policy details error is occured"),
	PACKET_SIGNKEY_EXCEPTION("RES-SER-430", "Public sign key is not available from key manager"),
	MACHINE_MASTER_CREATE_EXCEPTION("RES-SER-431", "Machine is not created in master data"),
	INDIVIDUAL_ID_TYPE_INVALID("RES-SER-432", "Individual Id type is invalid"),
	INDIVIDUAL_ID_UIN_MISMATCH("RES-SER-433", "Individual Id in request and identity json UIN is not matching"),

	FAILED_TO_UPLOAD_DOC("RES-SER-434", "Failed to upload document"),
	FAILED_TO_RETRIEVE_DOC("RES-SER-435", "Failed to retrieve document(s) from object store"),
	ENCRYPT_DECRYPT_ERROR("RES-SER-436", "Failed to encrypt/decrypt data"),
	VIRUS_SCAN_FAILED("RES-SER-437", "Virus scanning failed for attached document"),
	CHANNEL_IS_NOT_VALID("RES-SER-438", "Invalid OTP Channel"),

	CLAIM_NOT_AVAILABLE("RES-SER-439", "Claim not available: %s"),

	NO_CHANNEL_IN_IDENTITY("RES-SER-440", "Identity data does not contain email/phone."),

	PARTNER_SERVICE_EXCEPTION("RES-SER-441", "Exception while calling partner service"),
	AUTH_LOCK_STATUS_FAILED("RES-SER-442", "Failed to retrieve auth lock status"),

	AUTH_TYPE_CALLBACK_NOT_AVAILABLE("RES-SER-443", "Callback url is not available for auth type: %s"),
	RESIDENT_WEBSUB_UPDATE_AUTH_TYPE_FAILED("RES-SER-444", "Failed to update auth type for resident websub"),

	RESIDENT_AUTH_TXN_DETAILS_FAILURE("RES-SER-445", "Failed to retrieve auth transaction details"),
	INVALID_PAGE_START_VALUE("RES-SER-446", "Invalid page start value"),
	INVALID_PAGE_FETCH_VALUE("RES-SER-447", "Invalid page fetch value"),
	PERPETUAL_VID_NOT_AVALIABLE("RES-SER-448", "Perpatual VID not available"),
	AID_STATUS_IS_NOT_READY("RES-SER-449", "AID is not ready"),

	BIOMETRIC_MISSING("RES-SER-450", "Biometric data is not available in database"),
	EMPTY_COLLECTION_FOUND("RES-SER-451", "Collection is empty"),

	IDVID_NOT_MATCH_TO_SESSION("RES-SER-452", "Provided individualId does not belong to the logged in session."),
	UNAUTHORIZED("RES-ATH-401", "Authentication Failed"),
	VID_NOT_BELONG_TO_INDIVITUAL("RES-SER-453", "Provided VID does not belong to the individualId."),
	VID_NOT_BELONG_TO_SESSION("RES-SER-454", "Provided VID does not belong to the logged in session."),
	RID_NOT_FOUND("RES-SER-456", "RID not found"), AID_NOT_FOUND("RES-SER-457", "AID not found"),
	CREDENTIAL_REQUEST_ID_NOT_FOUND("RES-SER-458", "Credential request id not found"),
	CREDENTIAL_REQUEST_NOT_FOUND("RES-SER-459", "Credential request not found"),

	PACKET_ENCRYPTION_FAILURE_EXCEPTION("RES-SER-455", "Packet encryption failed"),

	REQUEST_ID_NOT_FOUND("RES-SER-456", "Request id not found"),
	DIGITAL_CARD_RID_NOT_FOUND("RES-SER-457", "Digital Card Rid not found"),

	PAYMENT_REQUIRED("RES-SER-402", "Payment is not made for this card"),
	FAILED_TO_DELETE_DOC("RES-SER-458", "Failed to delete document"),
	EVENT_STATUS_NOT_FOUND("RES-SER-459", "EID not available in database"),
	CONSENT_DENIED("RES-SER-461",
			"Accepting the terms and conditions is a mandatory action to proceed further. Please accept the consent to proceed"),

	ACK_TEMPLATE_NOT_FOUND("RES-SER-460", "Acknowledgment template not found"),
	NO_DOCUMENT_FOUND_FOR_TRANSACTION_ID("RES-SER-461", "No document found for transactionID: "),
	CONFIG_FILE_NOT_FOUND_EXCEPTION("RES-SER-462","Config file not found in the config server"),
	SEND_OTP_FAILED("RES-SER-463", "Send OTP failed."),
	OTP_ALREADY_SENT("RES-SER-464", "OTP is already sent - Please use the Sent OTP or try again after sometime"),
	BLOCKED_OTP_VALIDATE("RES-SER-465", "OTP is blocked" ),
	UNABLE_TO_PROCESS("RES-SER-466", "Unable to process" ),
	SERVER_ERROR("RES-SER-467", "Server error occurred" ),
	VALIDATION_UNSUCCESS("RES-SER-468", "Validation un-success"),
	EXPIRED_OTP("RES-SER-469", "OTP expired - Please send OTP again"),
	NO_RECORDS_FOUND("RES-SER-470", "No Record(s) found"),
	VID_VALIDATION("RES-SER-471","This VID cannot be revoked since you have logged in using same VID"),
	CARD_NOT_FOUND("RES-SER-472", "Card not found." ),
	INVALID_REQUEST_TYPE_CODE("RES-SER-473", "Invalid Request Type. Please input eventId only for VID_CARD_DOWNLOAD," +
			"UPDATE_MY_UIN"),
	INVALID_INDIVIDUAL_ID("RES-SER-474", "Individual ID is invalid." ),
	PAYMENT_FAILED("RES-SER-475", "Payment has failed"),
	PAYMENT_CANCELED("RES-SER-476", "Payment has been cancelled"),
	TECHNICAL_ERROR("RES-SER-477", "Technical error has occurred"),
	CAN_T_PLACE_ORDER("RES-SER-478", "Cannot place order at the moment"),
	DOWNLOAD_PERSONALIZED_CARD("RES-SER-479", "Error in downloading personalized card" ),
	VID_REQUEST_CARD_FAILED("RES-SER-480", "Error in Request card from vid"),
	PATNER_NOT_FOUND("RES-SER-481","Partner not found."),
	REDIRECT_URL_NOT_FOUND("RES-SER-482","Redirect url not found."),
	CARD_NOT_READY("RES_SER_509", "The card is not ready for download."),
	OTP_REQUEST_FLOODED("RES_SER_510", "Innumerous OTP requests received"),
	EID_NOT_BELONG_TO_SESSION("RES-SER-511", "The entered EID is not associated with the UIN/VID used to log in."),
	GRIEVANCE_TICKET_GENERATION_FAILED("RES-SER-512", "Unable to Generate Ticket of grievance"),
	MISSING_INPUT_PARAMETER("RES-SER-414", "Missing input Parameter- %s"),
	OTP_EXPIRED("RES-OTP-006","OTP has expired"),
	OTP_INVALID("RES-OTP-007","OTP is invalid"),
	INVALID_TRANSACTION_ID("RES-OTP-008","Input transactionId does not match transactionId of OTP Request"),
	SMS_AUTH_LOCKED("RES-OTP-010","Your SMS OTP authentication is locked"),
	EMAIL_AUTH_LOCKED("RES-OTP-011","Your email OTP authentication is locked"),
	SMS_AND_EMAIL_AUTH_LOCKED("RES-OTP-012","Your SMS and email OTP authentication is locked"),
	VID_CREATION_FAILED_WITH_REVOCATION("RES-SER-513",
			"Failed to create VID, as the existing VID cannot be revoked since this VID was used to log into the current session."),
	DOCUMENT_FILE_SIZE("RES-SER-514","File size cannot be more than 2MB."),
	CHAR_LIMIT_EXCEEDS("RES-SER-514","Input text size exceeds the limit; Character limit=%d; inputType=%s"),
	CONTAINS_SPECIAL_CHAR("RES-SER-515","Input text contains special characters;inputType=%s");
	


	private final String errorCode;
	private final String errorMessage;

	private ResidentErrorCode(final String errorCode, final String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
}
