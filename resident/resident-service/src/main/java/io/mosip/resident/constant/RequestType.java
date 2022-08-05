package io.mosip.resident.constant;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import io.mosip.resident.util.TemplateUtil;

/**
 * The Enum RequestType.
 * @author Kamesh Shekhar Prasad
 */

public enum RequestType {
	AUTHENTICATION_REQUEST(TemplateUtil::getAckTemplateVariablesForAuthenticationRequest,List.of(EventStatusSuccess.AUTHENTICATION_SUCCESSFUL),List.of(EventStatusFailure.AUTHENTICATION_FAILED),List.of()),
	SHARE_CRED_WITH_PARTNER(TemplateUtil::getAckTemplateVariablesForCredentialShare,List.of(EventStatusSuccess.RECEIVED,EventStatusSuccess.DATA_SHARED_SUCCESSFULLY),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW,EventStatusInProgress.ISSUED)),
	DOWNLOAD_PERSONALIZED_CARD(TemplateUtil::getAckTemplateVariablesForDownloadPersonalizedCard,List.of(EventStatusSuccess.STORED,EventStatusSuccess.CARD_DOWNLOADED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW,EventStatusInProgress.ISSUED)),
	ORDER_PHYSICAL_CARD(TemplateUtil::getAckTemplateVariablesForOrderPhysicalCard,List.of(EventStatusSuccess.CARD_DELIVERED),List.of(EventStatusFailure.FAILED,EventStatusFailure.PAYMENT_FAILED),List.of(EventStatusInProgress.PAYMENT_CONFIRMED,EventStatusInProgress.NEW,EventStatusInProgress.ISSUED,EventStatusInProgress.PRINTING,EventStatusInProgress.IN_TRANSIT)),
	GET_MY_ID(TemplateUtil::getAckTemplateVariablesForGetMyId,List.of(EventStatusSuccess.CARD_DOWNLOADED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW,EventStatusInProgress.OTP_REQUESTED,EventStatusInProgress.OTP_VERIFIED)),
	BOOK_AN_APPOINTMENT(TemplateUtil::getAckTemplateVariablesForBookAnAppointment,List.of(),List.of(),List.of()),
	UPDATE_MY_UIN(TemplateUtil::getAckTemplateVariablesForUpdateMyUin,List.of(EventStatusSuccess.PROCESSED,EventStatusSuccess.DATA_UPDATED),List.of(EventStatusFailure.FAILED,EventStatusFailure.REJECTED,EventStatusFailure.REPROCESS_FAILED),List.of(EventStatusInProgress.NEW,EventStatusInProgress.PROCESSING,EventStatusInProgress.PAUSED,EventStatusInProgress.RESUMABLE,EventStatusInProgress.REPROCESS,EventStatusInProgress.PAUSED_FOR_ADDITIONAL_INFO)),
	GENERATE_VID(TemplateUtil::getAckTemplateVariablesForGenerateVid,List.of(EventStatusSuccess.VID_GENERATED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW)),
	REVOKE_VID(TemplateUtil::getAckTemplateVariablesForRevokeVid,List.of(EventStatusSuccess.VID_REVOKED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW)),
	VERIFY_PHONE_EMAIL(TemplateUtil::getAckTemplateVariablesForVerifyPhoneEmail,List.of(EventStatusSuccess.EMAIL_VERIFIED,EventStatusSuccess.PHONE_VERIFIED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW,EventStatusInProgress.OTP_REQUESTED,EventStatusInProgress.OTP_VERIFIED)),
	AUTH_TYPE_LOCK_UNLOCK(TemplateUtil::getAckTemplateVariablesForAuthTypeLockUnlock,List.of(EventStatusSuccess.LOCKED,EventStatusSuccess.UNLOCKED,EventStatusSuccess.AUTHENTICATION_TYPE_LOCKED,EventStatusSuccess.AUTHENTICATION_TYPE_UNLOCKED),List.of(EventStatusFailure.FAILED),List.of(EventStatusInProgress.NEW));
	private BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction;
	private List<EventStatusSuccess> successStatusList;
	private List<EventStatusFailure> failureStatusList;
	private List<EventStatusInProgress> inProgressStatusList;
	private RequestType(BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction, List<EventStatusSuccess> successStatusList, List<EventStatusFailure> failureStatusList, List<EventStatusInProgress> inProgressStatusList) {
		this.ackTemplateVariablesFunction = ackTemplateVariablesFunction;
		this.successStatusList = Collections.unmodifiableList(successStatusList);
		this.failureStatusList = Collections.unmodifiableList(failureStatusList);
		this.inProgressStatusList = Collections.unmodifiableList(inProgressStatusList);
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
	public String getEmailTemplateCodeProperty() {
		return "resident.template.email." + this.name().toLowerCase();
	}
	public String getSmsTemplateCodeProperty() {
		return "resident.template.sms." + this.name().toLowerCase();
	}
	public Map<String, String> getAckTemplateVariables(TemplateUtil templateUtil, String eventId) {
		return ackTemplateVariablesFunction.apply(templateUtil, eventId);
	}

}
