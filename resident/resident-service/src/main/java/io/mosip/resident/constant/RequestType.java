package io.mosip.resident.constant;

import io.mosip.resident.util.TemplateUtil;

import java.util.Map;
import java.util.function.BiFunction;

public enum RequestType {
	AUTHENTICATION_REQUEST(TemplateUtil::getAckTemplateVariablesForAuthenticationRequest),
	SHARE_CRED_WITH_PARTNER(TemplateUtil::getAckTemplateVariablesForCredentialShare),
	DOWNLOAD_PERSONALIZED_CARD(TemplateUtil::getAckTemplateVariablesForDownloadPersonalizedCard),
	ORDER_PHYSICAL_CARD(TemplateUtil::getAckTemplateVariablesForOrderPhysicalCard),
	GET_MY_ID(TemplateUtil::getAckTemplateVariablesForGetMyId),
	BOOK_AN_APPOINTMENT(TemplateUtil::getAckTemplateVariablesForBookAnAppointment),
	UPDATE_MY_UIN(TemplateUtil::getAckTemplateVariablesForUpdateMyUin),
	GENERATE_VID(TemplateUtil::getAckTemplateVariablesForGenerateVid),
	REVOKE_VID(TemplateUtil::getAckTemplateVariablesForRevokeVid),
	VERIFY_PHONE_EMAIL(TemplateUtil::getAckTemplateVariablesForVerifyPhoneEmail),
	AUTH_TYPE_LOCK_UNLOCK(TemplateUtil::getAckTemplateVariablesForAuthTypeLockUnlock);
	private BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction;
	private RequestType(BiFunction<TemplateUtil, String, Map<String, String>> ackTemplateVariablesFunction) {
		this.ackTemplateVariablesFunction = ackTemplateVariablesFunction;
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
