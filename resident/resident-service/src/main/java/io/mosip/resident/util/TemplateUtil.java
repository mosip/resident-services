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
import io.mosip.resident.service.impl.ProxyPartnerManagementServiceImpl;
import io.mosip.resident.service.impl.UISchemaTypes;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private static final String LOGO_URL = "logoUrl";
    
    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    @Autowired
    private RequestValidator requestValidator;
    
    @Autowired
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;
    
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
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.EVENT_TYPE, residentTransactionEntity.getRequestTypeCode());
        templateVariables.put(TemplateVariablesEnum.EVENT_STATUS, getEventStatusForRequestType(residentTransactionEntity.getStatusCode()));
        templateVariables.put(TemplateVariablesEnum.SUMMARY, residentTransactionEntity.getRequestSummary());
        templateVariables.put(TemplateVariablesEnum.TIMESTAMP, DateUtils.formatToISOString(residentTransactionEntity.getCrDtimes()));
        try {
            templateVariables.put(TemplateVariablesEnum.INDIVIDUAL_ID, getIndividualIdType());
        } catch (ApisResourceAccessException e) {
            throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
        }
        return templateVariables;
    }
    
	private ResidentTransactionEntity getEntityFromEventId(String eventId) {
		Optional<ResidentTransactionEntity> residentTransactionEntity = residentTransactionRepository.findById(eventId);
		if (residentTransactionEntity.isPresent()) {
			return residentTransactionEntity.get();
		} else {
			throw new ResidentServiceException(ResidentErrorCode.EVENT_STATUS_NOT_FOUND,
					ResidentErrorCode.EVENT_STATUS_NOT_FOUND.getErrorMessage());
		}
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
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
        templateVariables.put(TemplateVariablesEnum.PURPOSE, residentTransactionEntity.getPurpose());
        templateVariables.put(TemplateVariablesEnum.TRACKING_ID, residentTransactionEntity.getTrackingId());
        templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, residentTransactionEntity.getReferenceLink());
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, residentTransactionEntity.getAttributeList());
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForAuthenticationRequest(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
        templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
        templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
        return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, residentTransactionEntity.getAttributeList());
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, residentTransactionEntity.getReferenceLink());
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
         templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, residentTransactionEntity.getAttributeList());
         templateVariables.put(TemplateVariablesEnum.TRACKING_ID, residentTransactionEntity.getTrackingId());
         templateVariables.put(TemplateVariablesEnum.ORDER_TRACKING_LINK, residentTransactionEntity.getReferenceLink());
         templateVariables.put(TemplateVariablesEnum.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
         templateVariables.put(TemplateVariablesEnum.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
         templateVariables.put(TemplateVariablesEnum.PAYMENT_STATUS, getPaymentStatus(residentTransactionEntity.getStatusCode()));
         templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, residentTransactionEntity.getReferenceLink());
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
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
        templateVariables.put(TemplateVariablesEnum.ATTRIBUTE_LIST, residentTransactionEntity.getAttributeList());
        templateVariables.put(TemplateVariablesEnum.DOWNLOAD_CARD_LINK, residentTransactionEntity.getReferenceLink());
        return templateVariables;
    }

     public  Map<String, String> getAckTemplateVariablesForGenerateVid(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
         templateVariables.put(TemplateVariablesEnum.VID_TYPE, residentTransactionEntity.getRefIdType());
         templateVariables.put(TemplateVariablesEnum.VID, residentTransactionEntity.getRefId());
         return templateVariables;
     }

    public Map<String, String> getAckTemplateVariablesForRevokeVid(String eventId) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
        templateVariables.put(TemplateVariablesEnum.VID_TYPE, residentTransactionEntity.getRefIdType());
        templateVariables.put(TemplateVariablesEnum.VID, residentTransactionEntity.getRefId());
        return templateVariables;
    }

    public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
        return getCommonTemplateVariables(eventId);
    }

     public  Map<String, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId);
         ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
         templateVariables.put(TemplateVariablesEnum.AUTHENTICATION_MODE, residentTransactionEntity.getAuthTypeCode());
         return templateVariables;
     }

     public  Map<String, String> getAckTemplateVariablesForVidCardDownload(String eventId) {
         return Collections.emptyMap();
     }
     
     public Map<String, String> getAckTemplateVariablesForSendOtp(String eventId) {
         return getCommonTemplateVariables(eventId);
     }
     
     public Map<String, String> getAckTemplateVariablesForValidateOtp(String eventId) {
         return getCommonTemplateVariables(eventId);
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
     
     public String getSummaryTemplateTypeCode(RequestType requestType, TemplateType templateType) {
    	 String summaryTemplateCodeProperty = requestType.getSummaryTemplateCodeProperty(templateType);
    	 return getTemplateTypeCode(summaryTemplateCodeProperty);
     }
    
     private String getTemplateTypeCode(String templateCodeProperty) {
    	 return env.getProperty(templateCodeProperty);
	}

	private String getPaymentStatus(String statusCode) {
		if (statusCode.equalsIgnoreCase(EventStatusFailure.PAYMENT_FAILED.name())) {
			return EventStatusFailure.PAYMENT_FAILED.name();
		} else {
			return EventStatusInProgress.PAYMENT_CONFIRMED.name();
		}
	}

	private String getPartnerLogo(String partnerId) {
		Map<String, ?> partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(partnerId);
		return (String) partnerDetail.get(LOGO_URL);
	}

 }
