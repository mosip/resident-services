package io.mosip.resident.constant;

/**
 * The Enum ApiName.
 * 
 */
public enum ApiName {

	KERNELAUTHMANAGER,

	INTERNALAUTH,
	
	INTERNALAUTHTRANSACTIONS,

	KERNELENCRYPTIONSERVICE,

	REGPROCPRINT,

	IDREPOGETIDBYUIN,

	IDREPOGETIDBYRID,

	GETUINBYVID,

	TEMPLATES,

	SMSNOTIFIER,

	EMAILNOTIFIER,

	IDAUTHCREATEVID,

	AUTHTYPESTATUSUPDATE,
	
	REGISTRATIONSTATUSSEARCH,

	IDAUTHREVOKEVID,

	RETRIEVE_VIDS,
	
	PACKETMANAGER_CREATE,

	MIDSCHEMAURL,
	MACHINEDETAILS,
	CENTERDETAILS,
	RIDGENERATION,
	SYNCSERVICE,
	PACKETRECEIVER,
	CREATEVID,
	IDREPOSITORY,
	ENCRYPTURL, CREDENTIAL_REQ_URL,
	CREDENTIAL_STATUS_URL,

	CREDENTIAL_TYPES_URL,

	CREDENTIAL_CANCELREQ_URL,

	PACKETSIGNPUBLICKEY,
	MACHINESEARCH,
	MACHINECREATE,

	PARTNER_API_URL, DECRYPT_API_URL, OTP_GEN_URL, POLICY_REQ_URL,
	
	VALID_DOCUMENT_BY_LANGCODE_URL,
	LOCATION_HIERARCHY_LEVEL_BY_LANGCODE_URL,
	LOCATION_HIERARCHY,
	IMMEDIATE_CHILDREN_BY_LOCATIONCODE_AND_LANGCODE_URL,
	LOCATION_INFO_BY_LOCCODE_AND_LANGCODE_URL,
	COORDINATE_SPECIFIC_REGISTRATION_CENTERS_URL,
	APPLICANT_VALID_DOCUMENT_URL,
	REGISTRATION_CENTER_FOR_LOCATION_CODE_URL,
	REGISTRATION_CENTER_BY_LOCATION_TYPE_AND_SEARCH_TEXT_PAGINATED_URL,
	IDREPO_IDENTITY_URL,
	WORKING_DAYS_BY_REGISTRATION_ID,
	LATEST_ID_SCHEMA_URL,
	PARTNER_SERVICE_URL,
	RESIDENT_REQ_CREDENTIAL_URL,
	DIGITAL_CARD_STATUS_URL,
	GET_ORDER_STATUS_URL,
	TEMPLATES_BY_LANGCODE_AND_TEMPLATETYPECODE_URL,
	IDREPO_IDENTITY_UPDATE_COUNT,
	DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME,
	GET_RID_BY_INDIVIDUAL_ID,
	PDFSIGN,
	PARTNER_DETAILS_NEW_URL,
	DOCUMENT_TYPE_BY_DOCUMENT_CATEGORY_AND_LANG_CODE,
	GET_RID_STATUS, DYNAMIC_FIELD_BASED_ON_FIELD_NAME;

}
