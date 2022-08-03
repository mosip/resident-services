package io.mosip.resident.util;

import io.mosip.resident.constant.TemplateVariablesEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class TemplateUtil.
 * @author Kamesh Shekhar Prasad
 */

@Component
 public class TemplateUtil {

    private Map<String, String> getCommonTemplateVariables(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put(TemplateVariablesEnum.EVENT_ID, eventId);
        templateVariables.put(TemplateVariablesEnum.EVENT_TYPE, "");
        templateVariables.put(TemplateVariablesEnum.PURPOSE, "");
        templateVariables.put(TemplateVariablesEnum.EVENT_STATUS, "");
        templateVariables.put(TemplateVariablesEnum.INDIVIDUAL_ID, "");
        templateVariables.put(TemplateVariablesEnum.SUMMARY, "");
        templateVariables.put(TemplateVariablesEnum.TIMESTAMP, "");
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
        templateVariables.put(TemplateVariablesEnum.TRACKING_ID, "trackingId");
        templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, "orderTrackingLink");
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, "partnerName");
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, "partnerLogo");
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, "attributeList");
        templateVariables.put(TemplateVariablesEnum.TIMESTAMP, "timestamp");
        return templateVariables;
    }
    public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, "partnerName");
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, "partnerLogo");
        return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, "attributeList");
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, "downloadCardLink");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
         templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, "attributeList");
         templateVariables.put(TemplateVariablesEnum.TRACKING_ID, "trackingId");
         templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, "orderTrackingLink");
         templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, "partnerName");
         templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, "partnerLogo");
         templateVariables.put(TemplateVariablesEnum.PAYMENT_STATUS, "paymentStatus");
         templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, "downloadCardLink");
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForGetMyId(String eventId) {
        return getCommonTemplateVariables(eventId);
    }

     public  Map<String, String> getAckTemplateVariablesForBookAnAppointment(String eventId) {
         return Collections.emptyMap();
     }

    public Map<String, String> getAckTemplateVariablesForUpdateMyUin(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, "attributeList");
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, "downloadCardLink");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
         templateVariables.put(TemplateVariablesEnum.VID_TYPE, "Perpetual");
         templateVariables.put(TemplateVariablesEnum.VID, "VID");
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
        templateVariables.put(TemplateVariablesEnum.VID_TYPE, "Perpetual");
        templateVariables.put(TemplateVariablesEnum.VID, "VID");
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
        return getCommonTemplateVariables(eventId);
    }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, "otp");
         return templateVariables;
     }
 }
