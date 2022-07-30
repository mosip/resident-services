package io.mosip.resident.util;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
 public class TemplateUtil {

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId){
        return null;
    }

     public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        return null;
     }

     public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        Map<String, String> templateVariables = new LinkedHashMap<>();
        templateVariables.put("eventId", eventId);
        templateVariables.put("eventType", "Download a personalized card");
        templateVariables.put("purpose", "Personalised card was downloaded");
        templateVariables.put("eventStatus", "success");
        templateVariables.put("individualId", "UIN/VID");
        templateVariables.put("authenticationMode", "OTP authentication/biometric authentication (mention which modality)/QR code scan");
        templateVariables.put("summary", "Personalised card was generated successfully and sent to the registered email ID");
        templateVariables.put("attributeList","Name, DOB, Gender , etc.");
        templateVariables.put("timestamp","Date and time stamp of card download");
        templateVariables.put("downloadCardLink", "Download button to download UIN card.");
        return templateVariables;
     }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
        return null;
     }

     public  Map<String, String> getAckTemplateVariablesForGetMyId(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForBookAnAppointment(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForUpdateMyUin(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
         return null;
     }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         return null;
     }
 }
