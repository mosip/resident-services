package io.mosip.resident.util;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * The Class TemplateUtil.
 * @author Kamesh Shekhar Prasad
 */

@Component
 public class TemplateUtil {

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId){
        return Collections.emptyMap();
    }

     public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        return Collections.emptyMap();
     }

     public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
        return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForGetMyId(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForBookAnAppointment(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForUpdateMyUin(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
         return Collections.emptyMap();
     }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         return Collections.emptyMap();
     }
 }
