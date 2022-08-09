package io.mosip.resident.util;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.*;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The Class TemplateUtil.
 * @author Kamesh Shekhar Prasad
 */

@Component
 public class TemplateUtil {

    private static final String UIN = "UIN";
    private static final String VID = "VID";
    private static final String AID = "AID";
    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    @Autowired
    private RequestValidator requestValidator;

    /**
     * Gets the ack template variables for authentication request.
     *
     * @param eventId the event id
     * @return the ack template variables for authentication request
     */

    private Map<String, String> getCommonTemplateVariables(String eventId) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put(TemplateVariablesEnum.EVENT_ID, eventId);
        Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository
                .findById(eventId);
        if(residentTransactionEntity.isPresent()) {
            ResidentTransactionEntity residentTransaction = residentTransactionEntity.get();
            templateVariables.put(TemplateVariablesEnum.EVENT_TYPE, residentTransaction.getRequestTypeCode());
            templateVariables.put(TemplateVariablesEnum.PURPOSE, residentTransaction.getPurpose());
            templateVariables.put(TemplateVariablesEnum.EVENT_STATUS, getEventStatusForRequestType(residentTransaction.getStatusCode()));
            templateVariables.put(TemplateVariablesEnum.SUMMARY, residentTransaction.getRequestSummary());
            templateVariables.put(TemplateVariablesEnum.TIMESTAMP, DateUtils.formatToISOString(residentTransaction.getCrDtimes()));
        } else{
            throw new ResidentServiceException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND,
                    ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorMessage());
        }
        try {
            templateVariables.put(TemplateVariablesEnum.INDIVIDUAL_ID, getIndividualIdType());
        } catch (ApisResourceAccessException e) {
            throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
        }
        return templateVariables;
    }

    private String getIndividualIdType() throws ApisResourceAccessException {
        String individualId= identityServiceImpl.getResidentIndvidualId();
        if(requestValidator.validateUin(individualId)){
            return UIN;
        } else if(requestValidator.validateVid(individualId)){
            return VID;
        } else{
            return AID;
        }
    }

    private String getEventStatusForRequestType(String statusCode) {
        String eventStatus = "";
        if(EventStatusSuccess.containsStatus(statusCode)){
            eventStatus = EventStatus.SUCCESS.name();
        } else if(EventStatusFailure.containsStatus(statusCode)){
            eventStatus = EventStatus.FAILED.name();
        } else {
            eventStatus = EventStatus.IN_PROGRESS.name();
        }
        return eventStatus;
    }

    public Map<String, String> getAckTemplateVariablesForCredentialShare(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
        templateVariables.put(TemplateVariablesEnum.TRACKING_ID, getTrackingId(eventId));
        templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, getOrderTrackingLink(eventId));
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, getPartnerName(eventId));
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(eventId));
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, getAttributeList(eventId));
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, getPartnerName(eventId));
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(eventId));
        return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, getAttributeList(eventId));
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, getDownloadCardLink(eventId));
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
         templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, getAttributeList(eventId));
         templateVariables.put(TemplateVariablesEnum.TRACKING_ID, getTrackingId(eventId));
         templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, getOrderTrackingLink(eventId));
         templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, getPartnerName(eventId));
         templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(eventId));
         templateVariables.put(TemplateVariablesEnum.PAYMENT_STATUS, getPaymentStatus(eventId));
         templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, getDownloadCardLink(eventId));
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
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, getAttributeList(eventId));
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, getDownloadCardLink(eventId));
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
         templateVariables.put(TemplateVariablesEnum.VID_TYPE, getVidType(eventId));
         templateVariables.put(TemplateVariablesEnum.VID, getVidNumber(eventId));
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
        templateVariables.put(TemplateVariablesEnum.VID_TYPE, getVidType(eventId));
        templateVariables.put(TemplateVariablesEnum.VID, getVidNumber(eventId));
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
        return getCommonTemplateVariables(eventId);
    }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, getAuthenticationMode(eventId));
         return templateVariables;
     }

     public  Map<String, String> getAckTemplateVariablesForVidCardDownload(String eventId) {
         return Collections.emptyMap();
     }

     //ToDo: Need to change method implementation
     private String getVidNumber(String eventId) {
         return "vid";
     }
     //ToDo: Need to change method implementation
     private String getVidType(String eventId) {
         return "vidType";
     }
     //TODO: Need to change the method implementation
     private String getPaymentStatus(String eventId) {
        return "paymentStatus";
     }

     //TODO: Need to change the method implementation
     private String getDownloadCardLink(String eventId) {
         return "downloadCardLink";
     }

    //TODO: Need to change the method implementation
    private String getAttributeList(String eventId) {
        return "attributeList";
    }

    //TODO: Need to change the method implementation
    private String getPartnerLogo(String eventId) {
        return "partnerLogo";
    }

    //ToDo: Need to change the logic to get the partner name from the partner id
    private String getPartnerName(String eventId) {
        return "partnerName";
    }

    //TODO: Need to change the method implementation
    private String getOrderTrackingLink(String eventId) {
        return "orderTrackingLink";
    }

    //TODO: Need to change the implementation of this method
    private String getTrackingId(String eventId) {
        return "trackingId";
    }

    //TODO: Need to change the implementation of this method
    private String getAuthenticationMode(String eventId) {
        return "otp";
    }
 }
