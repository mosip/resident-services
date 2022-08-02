package io.mosip.resident.util;

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

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "credential-share");
        templateVariables.put("purpose", "credential-share");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "123456789");
        templateVariables.put("authenticationMode", "otp");
        templateVariables.put("summary", "Credential share successful");
        templateVariables.put("trackingId", "trackingId");
        templateVariables.put("orderTrackingLink", "orderTrackingLink");
        templateVariables.put("partnerName", "partnerName");
        templateVariables.put("partnerLogo", "partnerLogo");
        templateVariables.put("attributeList", "attributeList");
        templateVariables.put("timestamp", "timestamp");
        return templateVariables;
    }
    public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "AUTHENTICATION_REQUEST");
        templateVariables.put("purpose", "AUTHENTICATION_REQUEST");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("summary", "Authentication using fingerprint and UIN was successfully done.");
        templateVariables.put("authenticationMode", "biometric authentication");
        templateVariables.put("partnerName", "partnerName");
        templateVariables.put("partnerLogo", "partnerLogo");
        templateVariables.put("timestamp", "timestamp");
        return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "DOWNLOAD_PERSONALIZED_CARD");
        templateVariables.put("purpose", "DOWNLOAD_PERSONALIZED_CARD");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("authenticationMode", "biometric authentication");
        templateVariables.put("summary", "Download of personalized card was successfully done.");
        templateVariables.put("attributeList", "attributeList");
        templateVariables.put("timestamp", "timestamp");
        templateVariables.put("downloadCardLink", "downloadCardLink");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
         Map<String, String> templateVariables = new HashMap<>();
         templateVariables.put("eventId", eventId);
         templateVariables.put("eventType", "ORDER_PHYSICAL_CARD");
         templateVariables.put("purpose", "ORDER_PHYSICAL_CARD");
         templateVariables.put("eventStatus", "success");
         templateVariables.put("individualId", "UIN");
         templateVariables.put("authenticationMode", "biometric authentication");
         templateVariables.put("summary", "Order of physical card was successfully done.");
         templateVariables.put("attributeList", "attributeList");
         templateVariables.put("trackingId", "trackingId");
         templateVariables.put("orderTrackingLink", "orderTrackingLink");
         templateVariables.put("partnerName", "partnerName");
         templateVariables.put("partnerLogo", "partnerLogo");
         templateVariables.put("paymentStatus", "paymentStatus");
         templateVariables.put("timestamp", "timestamp");
         templateVariables.put("downloadCardLink", "downloadCardLink");
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForGetMyId(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "GET_MY_ID");
        templateVariables.put("purpose", "GET_MY_ID");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("summary", "Get my id was successfully done.");
        templateVariables.put("timestamp", "timestamp");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForBookAnAppointment(String eventId) {
         return Collections.emptyMap();
     }

    public Map<String, String> getAckTemplateVariablesForUpdateMyUin(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "UPDATE_MY_UIN");
        templateVariables.put("purpose", "UPDATE_MY_UIN");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("authenticationMode", "biometric authentication");
        templateVariables.put("summary", "UIN was successfully updated.");
        templateVariables.put("attributeList", "attributeList");
        templateVariables.put("timestamp", "timestamp");
        templateVariables.put("downloadCardLink", "downloadCardLink");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         Map<String, String> templateVariables = new HashMap<>();
         templateVariables.put("eventId", eventId);
         templateVariables.put("eventType", "GENERATE_VID");
         templateVariables.put("purpose", "GENERATE_VID");
         templateVariables.put("eventStatus", "success");
         templateVariables.put("individualId", "UIN");
         templateVariables.put("summary", "VID was successfully generated.");
         templateVariables.put("authenticationMode", "biometric authentication");
         templateVariables.put("vidType", "Perpetual");
         templateVariables.put("vid", "VID");
         templateVariables.put("timestamp", "timestamp");
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "REVOKE_VID");
        templateVariables.put("purpose", "REVOKE_VID");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("summary", "VID was successfully revoked.");
        templateVariables.put("authenticationMode", "biometric authentication");
        templateVariables.put("vidType", "Perpetual");
        templateVariables.put("vid", "VID");
        templateVariables.put("timestamp", "timestamp");
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "VERIFY_PHONE_EMAIL");
        templateVariables.put("purpose", "VERIFY_PHONE_EMAIL");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN");
        templateVariables.put("summary", "Phone and email was successfully verified.");
        templateVariables.put("timestamp", "timestamp");
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         Map<String, String> templateVariables = new HashMap<>();
         templateVariables.put("eventId", eventId);
         templateVariables.put("eventType", "AUTH_TYPE_LOCK_UNLOCK");
         templateVariables.put("purpose", "AUTH_TYPE_LOCK_UNLOCK");
         templateVariables.put("eventStatus", "success");
         templateVariables.put("individualId", "UIN");
         templateVariables.put("summary", "Auth type lock unlock was successfully done.");
         templateVariables.put("authenticationMode", "biometric authentication");
         templateVariables.put("timestamp", "timestamp");
         return templateVariables;
     }
 }
