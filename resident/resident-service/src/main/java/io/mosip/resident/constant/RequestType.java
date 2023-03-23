package io.mosip.resident.constant;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.function.QuadFunction;
import io.mosip.resident.util.TemplateUtil;
import reactor.util.function.Tuple2;

/**
 * The Enum RequestType.
 * 
 * @author Kamesh Shekhar Prasad
 */

public enum RequestType {
	AUTHENTICATION_REQUEST("Authentication Request", TemplateUtil::getAckTemplateVariablesForAuthenticationRequest,
			List.of(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL), List.of(EventStatusFailure.AUTHENTICATION_FAILED),
			List.of(), "", null, TemplateUtil::getDescriptionTemplateVariablesForAuthenticationRequest),
	SHARE_CRED_WITH_PARTNER("Share Credential With Partner", TemplateUtil::getAckTemplateVariablesForCredentialShare,
			List.of(EventStatusSuccess.RECEIVED, EventStatusSuccess.DATA_SHARED_SUCCESSFULLY),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED),
			"share-cred-with-partner", TemplateUtil::getNotificationTemplateVariablesForShareCredentialWithPartner,
			TemplateUtil::getDescriptionTemplateVariablesForShareCredential,
			ResidentConstants.ACK_SHARE_CREDENTIAL_NAMING_CONVENTION_PROPERTY),
	DOWNLOAD_PERSONALIZED_CARD("Download Personalized Card",
			TemplateUtil::getAckTemplateVariablesForDownloadPersonalizedCard,
			List.of(EventStatusSuccess.STORED, EventStatusSuccess.CARD_DOWNLOADED), List.of(EventStatusFailure.FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED), "cust-and-down-my-card",
			TemplateUtil::getNotificationTemplateVariablesForDownloadPersonalizedCard,
			TemplateUtil::getDescriptionTemplateVariablesForDownloadPersonalizedCard,
			ResidentConstants.ACK_PERSONALIZED_CARD_NAMING_CONVENTION_PROPERTY),
	ORDER_PHYSICAL_CARD("Order a Physical Card", TemplateUtil::getAckTemplateVariablesForOrderPhysicalCard,
			List.of(EventStatusSuccess.CARD_DELIVERED),
			List.of(EventStatusFailure.FAILED, EventStatusFailure.PAYMENT_FAILED),
			List.of(EventStatusInProgress.PAYMENT_CONFIRMED, EventStatusInProgress.NEW, EventStatusInProgress.ISSUED,
					EventStatusInProgress.PRINTING, EventStatusInProgress.IN_TRANSIT),
			"order-a-physical-card", TemplateUtil::getNotificationTemplateVariablesForOrderPhysicalCard,
			TemplateUtil::getDescriptionTemplateVariablesForOrderPhysicalCard,
			ResidentConstants.ACK_ORDER_PHYSICAL_CARD_NAMING_CONVENTION_PROPERTY),
	GET_MY_ID("Get UIN Card", TemplateUtil::getAckTemplateVariablesForGetMyId,
			List.of(EventStatusSuccess.CARD_DOWNLOADED, EventStatusSuccess.OTP_VERIFIED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW, EventStatusInProgress.OTP_REQUESTED),
			"get-my-uin-card", TemplateUtil::getNotificationTemplateVariablesForGetMyId,
			TemplateUtil::getDescriptionTemplateVariablesForGetMyId),
	BOOK_AN_APPOINTMENT("Book An Appointment", TemplateUtil::getAckTemplateVariablesForBookAnAppointment, List.of(),
			List.of(), List.of(), "", null, null),
	UPDATE_MY_UIN("Update UIN Data", TemplateUtil::getAckTemplateVariablesForUpdateMyUin,
			List.of(EventStatusSuccess.PROCESSED, EventStatusSuccess.DATA_UPDATED),
			List.of(EventStatusFailure.FAILED, EventStatusFailure.REJECTED, EventStatusFailure.REPROCESS_FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.PROCESSING, EventStatusInProgress.PAUSED,
					EventStatusInProgress.RESUMABLE, EventStatusInProgress.REPROCESS,
					EventStatusInProgress.PAUSED_FOR_ADDITIONAL_INFO),
			"update-demo-data", TemplateUtil::getNotificationTemplateVariablesForUpdateMyUin,
			TemplateUtil::getDescriptionTemplateVariablesForUpdateMyUin,
			ResidentConstants.ACK_UPDATE_MY_DATA_NAMING_CONVENTION_PROPERTY),
	GENERATE_VID("Generate VID", TemplateUtil::getAckTemplateVariablesForGenerateVid,
			List.of(EventStatusSuccess.VID_GENERATED), List.of(EventStatusFailure.FAILED),
			List.of(EventStatusInProgress.NEW), "gen-or-revoke-vid",
			TemplateUtil::getNotificationTemplateVariablesForGenerateOrRevokeVid,
			TemplateUtil::getDescriptionTemplateVariablesForManageMyVid,
			ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY),
	REVOKE_VID("Revoke VID", TemplateUtil::getAckTemplateVariablesForRevokeVid, List.of(EventStatusSuccess.VID_REVOKED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW), "gen-or-revoke-vid",
			TemplateUtil::getNotificationTemplateVariablesForGenerateOrRevokeVid,
			TemplateUtil::getDescriptionTemplateVariablesForManageMyVid,
			ResidentConstants.ACK_MANAGE_MY_VID_NAMING_CONVENTION_PROPERTY),
	AUTH_TYPE_LOCK_UNLOCK("Lock or/and Unlock Authentication Types",
			TemplateUtil::getAckTemplateVariablesForAuthTypeLockUnlock,
			List.of(EventStatusSuccess.LOCKED, EventStatusSuccess.UNLOCKED,
					EventStatusSuccess.AUTHENTICATION_TYPE_LOCKED, EventStatusSuccess.AUTHENTICATION_TYPE_UNLOCKED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW), "lock-unlock-auth",
			TemplateUtil::getNotificationTemplateVariablesForAuthTypeLockUnlock,
			TemplateUtil::getDescriptionTemplateVariablesForSecureMyId,
			ResidentConstants.ACK_SECURE_MY_ID_NAMING_CONVENTION_PROPERTY),
	VID_CARD_DOWNLOAD("Download VID Card", TemplateUtil::getAckTemplateVariablesForVidCardDownload,
			List.of(EventStatusSuccess.STORED, EventStatusSuccess.CARD_DOWNLOADED), List.of(EventStatusFailure.FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED), "vid-card-download",
			TemplateUtil::getNotificationTemplateVariablesForVidCardDownload,
			TemplateUtil::getDescriptionTemplateVariablesForVidCardDownload),

	SEND_OTP("Send OTP", TemplateUtil::getAckTemplateVariablesForSendOtp, List.of(), List.of(), List.of(), "send-otp",
			TemplateUtil::getNotificationSendOtpVariables, null),
	VALIDATE_OTP("Validate OTP", TemplateUtil::getAckTemplateVariablesForValidateOtp,
			List.of(EventStatusSuccess.OTP_VERIFIED), List.of(EventStatusFailure.OTP_VERIFICATION_FAILED),
			List.of(EventStatusInProgress.OTP_REQUESTED), "verify-my-phone-email",
			TemplateUtil::getNotificationCommonTemplateVariables,
			TemplateUtil::getDescriptionTemplateVariablesForValidateOtp),
	DEFAULT("Default", TemplateUtil::getDefaultTemplateVariables, List.of(), List.of(), List.of(), "",
			TemplateUtil::getNotificationCommonTemplateVariables, null);

	private QuadFunction<TemplateUtil, String, String, Integer, Tuple2<Map<String, String>, String>> ackTemplateVariablesFunction;
	private List<EventStatusSuccess> successStatusList;
	private List<EventStatusFailure> failureStatusList;
	private List<EventStatusInProgress> inProgressStatusList;
	private String featureName;
	private BiFunction<TemplateUtil, NotificationTemplateVariableDTO, Map<String, Object>> notificationTemplateVariablesFunction;
	private QuadFunction<TemplateUtil, String, String, String, String> getDescriptionTemplateVariables;
	private String namingProperty;

	private String name;

	private RequestType(String name,
			QuadFunction<TemplateUtil, String, String, Integer, Tuple2<Map<String, String>, String>> ackTemplateVariablesFunction,
			List<EventStatusSuccess> successStatusList, List<EventStatusFailure> failureStatusList,
			List<EventStatusInProgress> inProgressStatusList, String featureName,
			BiFunction<TemplateUtil, NotificationTemplateVariableDTO, Map<String, Object>> notificationTemplateVariablesFunction,
			QuadFunction<TemplateUtil, String, String, String, String> getDescriptionTemplateVariables) {
		this(name, ackTemplateVariablesFunction, successStatusList, failureStatusList, inProgressStatusList,
				featureName, notificationTemplateVariablesFunction, getDescriptionTemplateVariables, null);
	}

	private RequestType(String name,
			QuadFunction<TemplateUtil, String, String, Integer, Tuple2<Map<String, String>, String>> ackTemplateVariablesFunction,
			List<EventStatusSuccess> successStatusList, List<EventStatusFailure> failureStatusList,
			List<EventStatusInProgress> inProgressStatusList, String featureName,
			BiFunction<TemplateUtil, NotificationTemplateVariableDTO, Map<String, Object>> notificationTemplateVariablesFunction,
			QuadFunction<TemplateUtil, String, String, String, String> getDescriptionTemplateVariables,
			String namingProperty) {
		this.name = name;
		this.ackTemplateVariablesFunction = ackTemplateVariablesFunction;
		this.successStatusList = Collections.unmodifiableList(successStatusList);
		this.failureStatusList = Collections.unmodifiableList(failureStatusList);
		this.inProgressStatusList = Collections.unmodifiableList(inProgressStatusList);
		this.featureName = featureName;
		this.notificationTemplateVariablesFunction = notificationTemplateVariablesFunction;
		this.getDescriptionTemplateVariables = getDescriptionTemplateVariables;
		this.namingProperty = namingProperty;
	}

	public static RequestType getRequestTypeFromString(String requestTypeString) {
        for (RequestType requestType : values()) {
            if (requestType.name().equalsIgnoreCase(requestTypeString)) {
                return requestType;
            }
        }
        return RequestType.DEFAULT;
    }
	
	public static RequestType getRequestTypeByName(String name) {
        for (RequestType requestType : values()) {
            if (requestType.getName().equalsIgnoreCase(name)) {
                return requestType;
            }
        }
        return RequestType.DEFAULT;
    }

	public List<EventStatusSuccess> getSuccessStatusList() {
		return successStatusList;
	}

	public List<EventStatusFailure> getFailureStatusList() {
		return failureStatusList;
	}

	public List<EventStatusInProgress> getInProgressStatusList() {
		return inProgressStatusList;
	}
	
	public String getFeatureName() {
		return featureName;
	}

	public String getName() { return  name; }

	public String getEmailSubjectTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.email.subject." + templateType.getType() + "." + getFeatureName();
	}
	
	public String getEmailContentTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.email.content." + templateType.getType() + "." + getFeatureName();
	}

	public String getSmsTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.sms." + templateType.getType() + "." + getFeatureName();
	}
	
	public String getBellIconTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.bell-icon." + templateType.getType() + "." + getFeatureName();
	}
	
	public String getPurposeTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.purpose." + templateType.getType() + "." + getFeatureName();
	}
	
	public String getSummaryTemplateCodeProperty(TemplateType templateType) {
		return "resident.template.summary." + templateType.getType() + "." + getFeatureName();
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariables(TemplateUtil templateUtil, String eventId, String languageCode, Integer timeZoneOffset) {
		return ackTemplateVariablesFunction.apply(templateUtil, eventId, languageCode, timeZoneOffset);
	}
	
	public Map<String, Object> getNotificationTemplateVariables(TemplateUtil templateUtil, NotificationTemplateVariableDTO dto) {
		return notificationTemplateVariablesFunction.apply(templateUtil, dto);
	}

	public String getDescriptionTemplateVariables(TemplateUtil templateUtil, String eventId, String fileText, String languageCode){
		return getDescriptionTemplateVariables.apply(templateUtil, eventId, fileText, languageCode);
	}
	
	public String getNamingProperty() {
		return namingProperty;
	}

}