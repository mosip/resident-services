package io.mosip.resident.util;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.UISchemaTypes;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    
    @Autowired
    private Utilitiy utilitiy;

    @Autowired
	private Environment env;
    
    @Value("${resident.template.date.pattern}")
	private String templateDatePattern;
    
    @Value("${resident.template.time.pattern}")
	private String templateTimePattern;

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

    public String getFeatureName(String eventId){
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        return templateVariables.get(TemplateVariablesEnum.EVENT_TYPE);
    }

    public String getIndividualIdType() throws ApisResourceAccessException {
        String individualId= identityServiceImpl.getResidentIndvidualId();
        return getIndividualIdType(individualId);
    }

    public String getIndividualIdType(String individualId){
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
     
     public Map<String, Object> getNotificationCommonTemplateVariables(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = new HashMap<>();
 		templateVariables.put(TemplateVariablesEnum.EVENT_ID, dto.getEventId());
 		templateVariables.put(TemplateVariablesEnum.NAME, getName(dto.getLangCode()));
 		templateVariables.put(TemplateVariablesEnum.EVENT_DETAILS, dto.getRequestType().name());
 		templateVariables.put(TemplateVariablesEnum.DATE, getDate());
 		templateVariables.put(TemplateVariablesEnum.TIME, getTime());
 		templateVariables.put(TemplateVariablesEnum.STATUS, dto.getTemplateType().getType());
         if(TemplateType.FAILURE.getType().equals(dto.getTemplateType().getType())) {
 			templateVariables.put(TemplateVariablesEnum.TRACK_SERVICE_REQUEST_LINK, utilitiy.createTrackServiceRequestLink(dto.getEventId()));
 		}
 		return templateVariables;
 	}

     public Map<String, Object> getNotificationSendOtpVariables(NotificationTemplateVariableDTO dto){
        Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
        templateVariables.put(TemplateVariablesEnum.OTP, dto.getOtp());
        return templateVariables;
     }
     
     private String getTime() {
 		return DateUtils.getUTCCurrentDateTimeString(templateTimePattern);
 	}
     
     private String getDate() {
 		return DateUtils.getUTCCurrentDateTimeString(templateDatePattern);
 	}
     
     private String getName(String language) {
 		String name = "";
 		try {
 			String id=identityServiceImpl.getResidentIndvidualId();
 			Map<String, ?> idMap = identityServiceImpl.getIdentityAttributes(id,UISchemaTypes.UPDATE_DEMOGRAPHICS.getFileIdentifier());
 			name=identityServiceImpl.getNameForNotification(idMap, language);
 		} catch (ApisResourceAccessException | ResidentServiceCheckedException | IOException e) {
 			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                     ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
 		}
 		return name;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForGenerateOrRevokeVid(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForAuthTypeLockUnlock(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForUpdateMyUin(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		 if(TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
    			templateVariables.put(TemplateVariablesEnum.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
    		}
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForVerifyPhoneEmail(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForGetMyId(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForDownloadPersonalizedCard(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		 if(TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
   			templateVariables.put(TemplateVariablesEnum.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
   		}
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForOrderPhysicalCard(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		 if(TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
 			templateVariables.put(TemplateVariablesEnum.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
 		}
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForShareCredentialWithPartner(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForVidCardDownload(NotificationTemplateVariableDTO dto) {
    	 Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
    	 if(TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
    			templateVariables.put(TemplateVariablesEnum.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
    		}
    	 return templateVariables;
  	}
     
     public String getEmailSubjectTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String emailSubjectTemplateCodeProperty = requestType.getEmailSubjectTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(emailSubjectTemplateCodeProperty);
     }
     
     public String getEmailContentTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String emailContentTemplateCodeProperty = requestType.getEmailContentTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(emailContentTemplateCodeProperty);
     }
     
     public String getSmsTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String smsTemplateCodeProperty = requestType.getSmsTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(smsTemplateCodeProperty);
     }
     
     public String getBellIconTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String bellIconTemplateCodeProperty = requestType.getBellIconTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(bellIconTemplateCodeProperty);
     }
     
     public String getPurposeTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String purposeTemplateCodeProperty = requestType.getPurposeTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(purposeTemplateCodeProperty);
     }
    
     private String getTemplateTypeCode(String templateCodeProperty) {
    	 return env.getProperty(templateCodeProperty);
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
