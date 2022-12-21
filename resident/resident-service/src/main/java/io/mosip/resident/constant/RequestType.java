package io.mosip.resident.constant;

import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.util.TemplateUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The Enum RequestType.
 * 
 * @author Kamesh Shekhar Prasad
 */

public enum RequestType {
	AUTHENTICATION_REQUEST(TemplateUtil::getAckTemplateVariablesForAuthenticationRequest,
			List.of(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL), List.of(EventStatusFailure.AUTHENTICATION_FAILED),
			List.of(),"",null),
	SHARE_CRED_WITH_PARTNER(TemplateUtil::getAckTemplateVariablesForCredentialShare,
			List.of(EventStatusSuccess.RECEIVED, EventStatusSuccess.DATA_SHARED_SUCCESSFULLY),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED),"share-cred-with-partner",
			TemplateUtil::getNotificationTemplateVariablesForShareCredentialWithPartner),
	DOWNLOAD_PERSONALIZED_CARD(TemplateUtil::getAckTemplateVariablesForDownloadPersonalizedCard,
			List.of(EventStatusSuccess.STORED, EventStatusSuccess.CARD_DOWNLOADED), List.of(EventStatusFailure.FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED),"cust-and-down-my-card",
			TemplateUtil::getNotificationTemplateVariablesForDownloadPersonalizedCard),
	ORDER_PHYSICAL_CARD(TemplateUtil::getAckTemplateVariablesForOrderPhysicalCard,
			List.of(EventStatusSuccess.CARD_DELIVERED),
			List.of(EventStatusFailure.FAILED, EventStatusFailure.PAYMENT_FAILED),
			List.of(EventStatusInProgress.PAYMENT_CONFIRMED, EventStatusInProgress.NEW, EventStatusInProgress.ISSUED,
					EventStatusInProgress.PRINTING, EventStatusInProgress.IN_TRANSIT),"order-a-physical-card",
			TemplateUtil::getNotificationTemplateVariablesForOrderPhysicalCard),
	GET_MY_ID(TemplateUtil::getAckTemplateVariablesForGetMyId,
			List.of(EventStatusSuccess.CARD_DOWNLOADED, EventStatusSuccess.OTP_VERIFIED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW, EventStatusInProgress.OTP_REQUESTED),
			"get-my-uin-card", TemplateUtil::getNotificationTemplateVariablesForGetMyId),
	BOOK_AN_APPOINTMENT(TemplateUtil::getAckTemplateVariablesForBookAnAppointment, List.of(), List.of(), List.of(),"", null),
	UPDATE_MY_UIN(TemplateUtil::getAckTemplateVariablesForUpdateMyUin,
			List.of(EventStatusSuccess.PROCESSED, EventStatusSuccess.DATA_UPDATED),
			List.of(EventStatusFailure.FAILED, EventStatusFailure.REJECTED, EventStatusFailure.REPROCESS_FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.PROCESSING, EventStatusInProgress.PAUSED,
					EventStatusInProgress.RESUMABLE, EventStatusInProgress.REPROCESS,
					EventStatusInProgress.PAUSED_FOR_ADDITIONAL_INFO),"update-demo-data",
			TemplateUtil::getNotificationTemplateVariablesForUpdateMyUin),
	GENERATE_VID(TemplateUtil::getAckTemplateVariablesForGenerateVid, List.of(EventStatusSuccess.VID_GENERATED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW),"gen-or-revoke-vid",
			TemplateUtil::getNotificationTemplateVariablesForGenerateOrRevokeVid),
	REVOKE_VID(TemplateUtil::getAckTemplateVariablesForRevokeVid, List.of(EventStatusSuccess.VID_REVOKED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW),"gen-or-revoke-vid",
			TemplateUtil::getNotificationTemplateVariablesForGenerateOrRevokeVid),
	AUTH_TYPE_LOCK_UNLOCK(TemplateUtil::getAckTemplateVariablesForAuthTypeLockUnlock,
			List.of(EventStatusSuccess.LOCKED, EventStatusSuccess.UNLOCKED,
					EventStatusSuccess.AUTHENTICATION_TYPE_LOCKED, EventStatusSuccess.AUTHENTICATION_TYPE_UNLOCKED),
			List.of(EventStatusFailure.FAILED), List.of(EventStatusInProgress.NEW),"lock-unlock-auth",
			TemplateUtil::getNotificationTemplateVariablesForAuthTypeLockUnlock),
	VID_CARD_DOWNLOAD(TemplateUtil::getAckTemplateVariablesForVidCardDownload,
			List.of(EventStatusSuccess.STORED, EventStatusSuccess.CARD_DOWNLOADED), List.of(EventStatusFailure.FAILED),
			List.of(EventStatusInProgress.NEW, EventStatusInProgress.ISSUED),"",
			TemplateUtil::getNotificationTemplateVariablesForVidCardDownload),

	SEND_OTP(TemplateUtil::getAckTemplateVariablesForSendOtp, List.of(), List.of(), List.of(), "send-otp",
			TemplateUtil::getNotificationSendOtpVariables),
	VALIDATE_OTP(TemplateUtil::getAckTemplateVariablesForValidateOtp, List.of(EventStatusSuccess.OTP_VERIFIED),
			List.of(EventStatusFailure.OTP_VERIFICATION_FAILED), List.of(EventStatusInProgress.OTP_REQUESTED),
			"verify-my-phone-email", TemplateUtil::getNotificationCommonTemplateVariables),
	DEFAULT(TemplateUtil::getCommonTemplateVariables, List.of(), List.of(), List.of(), "",
			TemplateUtil::getNotificationCommonTemplateVariables);

	private BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction;
	private List<EventStatusSuccess> successStatusList;
	private List<EventStatusFailure> failureStatusList;
	private List<EventStatusInProgress> inProgressStatusList;
	private String featureName;
	private BiFunction<TemplateUtil, NotificationTemplateVariableDTO, Map<String, Object>> notificationTemplateVariablesFunction;

	private RequestType(BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction,
			List<EventStatusSuccess> successStatusList, List<EventStatusFailure> failureStatusList,
			List<EventStatusInProgress> inProgressStatusList, String featureName,
			BiFunction<TemplateUtil, NotificationTemplateVariableDTO, Map<String, Object>> notificationTemplateVariablesFunction) {
		this.ackTemplateVariablesFunction = ackTemplateVariablesFunction;
		this.successStatusList = Collections.unmodifiableList(successStatusList);
		this.failureStatusList = Collections.unmodifiableList(failureStatusList);
		this.inProgressStatusList = Collections.unmodifiableList(inProgressStatusList);
		this.featureName = featureName;
		this.notificationTemplateVariablesFunction=notificationTemplateVariablesFunction;
	}
	
	public static RequestType getRequestTypeFromString(String requestTypeString) {
        for (RequestType requestType : values()) {
            if (requestType.name().equalsIgnoreCase(requestTypeString)) {
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

	public Map<String, String> getAckTemplateVariables(TemplateUtil templateUtil, String eventId) {
		return ackTemplateVariablesFunction.apply(templateUtil, eventId);
	}
	
	public Map<String, Object> getNotificationTemplateVariables(TemplateUtil templateUtil, NotificationTemplateVariableDTO dto) {
		return notificationTemplateVariablesFunction.apply(templateUtil, dto);
	}

}