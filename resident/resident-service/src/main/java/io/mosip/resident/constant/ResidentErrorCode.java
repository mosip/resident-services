package io.mosip.resident.constant;

public enum ResidentErrorCode {

	NO_RID_FOUND_EXCEPTION("RES-SER-001", "RID not found"),
	INVALID_REQUEST_EXCEPTION("RES-SER-002","One or more input parameter is invalid or does not exist"),
	TOKEN_GENERATION_FAILED("RES-SER-003","Token generation failed"),
	OTP_VALIDATION_FAILED("RES-SER-004","OTP validation failed"),
	API_RESOURCE_UNAVAILABLE("RES-SER-005","API resource is not available"),
	API_RESOURCE_ACCESS_EXCEPTION("RES-SER-006", "Unable to access API resource"),
	VID_CREATION_EXCEPTION("RES-SER-007", "Exception while creating VID"),
	VID_ALREADY_PRESENT("RES-SER-008","Maximum allowed VIDs are active. Deactivate VID to generate new one."), 
	INVALID_INPUT("RES-SER-009","Invalid Input Parameter- "),
	INVALID_VID("RES-SER-010", "Invalid VID"), INVALID_UIN("RES-SER-011", "Invalid UIN"),
	INVALID_RID("RES-SER-012", "Invalid RID"), INVALID_VID_UIN("RES-SER-013", "Invalid UIN for given VID"),
	REQUEST_FAILED("RES-SER-014", "Your request is not successful, please try again later."),
	TEMPLATE_EXCEPTION("RES-SER-015","Template exception"),
	TEMPLATE_SUBJECT_EXCEPTION("RES-SER-016","Template subject exception"),
	NOTIFICATION_FAILURE("RES-SER-017","Sending notification(Email and SMS) to resident failed."),
	IN_VALID_UIN_OR_VID_OR_RID("RES-SER-018", "Invalid individualId"),
	RE_PRINT_REQUEST_FAILED("RES-SER-019","Re print UIN request failed"),
	VID_REVOCATION_EXCEPTION("RES-RID-005","VID revocation request failed. Please visit the nearest registration center for assistance."),
	BAD_REQUEST("RES-SER-020","Bad Request"),
	INVALID_API_RESPONSE("RES-SER-21", "Invalid APi response from - "),
	UIN_UPDATE_FAILED("RES-SER-22","Resident UIN update failed"),
	DOCUMENT_NOT_FOUND("RES-SER-23", "Could not find the submitted document"),
	//system exceptions
	RESIDENT_SYS_EXCEPTION("RES-SER-SYS-001","System exception occured"),
	IO_EXCEPTION("RES-SER-SYS-002","IO Exception occured"),
	JSON_PROCESSING_EXCEPTION("RES-SER-SYS-003","JSON Processing Exception occured"), 
	INVALID_RID_EXCEPTION("RES-TUG-001", "RID entered is not valid"),
	INVLAID_KEY_EXCEPTION("RES-SER-25",
			"Exception occured while encryting the packet Invalid Key"),
	UNKNOWN_EXCEPTION("RES-SER-26",
			"Unknown exception occured."),
	BASE_EXCEPTION("RES-SER-27",
			"Base exception."),
	PACKET_CREATION_EXCEPTION("RES-SER-28",
			"Exception while creating packet."),

	INVALID_ID("RES-SER-29", "Invalid id");
	
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
