package io.mosip.resident.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
 public class TemplateUtil {

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId){
        return null;
    }

     public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {

         Map<String, String> eventStatus = new HashMap<>();
         eventStatus.put("eventId", eventId);
         eventStatus.put("eventType", "Secure my ID");
         eventStatus.put("purpose", "OTP authentication was locked/unlocked");
         eventStatus.put("eventStatus", "success");
         eventStatus.put("individualId", "Credentials used to perform the transaction: UIN/VID");
         eventStatus.put("summary", "OTP authentication has been successfully locked (New | Success | Failed)");
         eventStatus.put("authenticationMode", "OTP authentication/biometric authentication (mention which modality)/QR code scan");
         eventStatus.put("timestamp", "Date and timestamp of locking/unlocking of the authentication type ");
         return eventStatus;
     }

     public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        return null;
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

    public Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
        Map<String, String> eventStatus = new HashMap<>();
        eventStatus.put("eventId", eventId);
        eventStatus.put("eventType", "Secure my ID");
        eventStatus.put("purpose", "OTP authentication was locked/unlocked");
        eventStatus.put("eventStatus", "success");
        eventStatus.put("individualId", "Credentials used to perform the transaction: UIN/VID");
        eventStatus.put("summary", "OTP authentication has been successfully locked (New | Success | Failed)");
        eventStatus.put("authenticationMode", "OTP authentication/biometric authentication (mention which modality)/QR code scan");
        eventStatus.put("timestamp", "Date and timestamp of locking/unlocking of the authentication type ");
        return eventStatus;
    }
 }
