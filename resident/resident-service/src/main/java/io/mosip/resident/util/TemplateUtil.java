package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ProxyPartnerManagementServiceImpl;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.service.impl.UISchemaTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The Class TemplateUtil.
 * @author Kamesh Shekhar Prasad
 */

@Component
 public class TemplateUtil {

    private static final String LOGO_URL = "logoUrl";
    private static final CharSequence GENERATED = "generated";
    private static final CharSequence REVOKED = "revoked";

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;
    
    @Autowired
    private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;
    
    @Autowired
    private Utilitiy utilitiy;

    @Autowired
    private ResidentServiceImpl residentService;

    @Autowired
	private Environment env;

    @Autowired
    private ResidentCredentialServiceImpl residentCredentialServiceImpl;
    
    @Value("${resident.template.date.pattern}")
	private String templateDatePattern;
    
    @Value("${resident.template.time.pattern}")
	private String templateTimePattern;

    private static final Logger logger = LoggerConfiguration.logConfig(TemplateUtil.class);

    /**
     * Gets the ack template variables for authentication request.
     *
     * @param eventId the event id
     * @return the ack template variables for authentication request
     */

    public Map<String, String> getCommonTemplateVariables(String eventId, String languageCode) {
        Map<String, String> templateVariables = new HashMap<>();
        templateVariables.put(TemplateVariablesConstants.EVENT_ID, eventId);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesConstants.EVENT_TYPE,
                RequestType.valueOf(residentTransactionEntity.getRequestTypeCode()).getName());
        templateVariables.put(TemplateVariablesConstants.EVENT_STATUS,
                getEventStatusForRequestType(residentTransactionEntity.getStatusCode()));
        templateVariables.put(TemplateVariablesConstants.SUMMARY, replaceNullWithEmptyString(
                residentTransactionEntity.getRequestSummary()));
        templateVariables.put(TemplateVariablesConstants.TIMESTAMP,
                truncateMilliSecondInTimeStampString(residentTransactionEntity.getCrDtimes()));
        templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK, utilitiy.createTrackServiceRequestLink(eventId));
        templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_LINK, utilitiy.createTrackServiceRequestLink(eventId));
        templateVariables.put(TemplateVariablesConstants.PURPOSE, residentTransactionEntity.getPurpose());
        templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, replaceNullWithEmptyString(
                residentTransactionEntity.getAttributeList()));
        templateVariables.put(TemplateVariablesConstants.AUTHENTICATION_MODE,
                replaceNullWithEmptyString(residentTransactionEntity.getAuthTypeCode()));
        try {
            templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, getIndividualIdType());
        } catch (ApisResourceAccessException e) {
            logger.error(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),e);
            templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, "");
        }
        return templateVariables;
    }

    private String truncateMilliSecondInTimeStampString(LocalDateTime localDateTime) {
        return localDateTime.format(
                DateTimeFormatter.ofPattern(Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_DATE_TIME_PATTERN))));
    }
    public String getDescriptionTemplateVariablesForAuthenticationRequest(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForShareCredential(String eventId, String fileText) {
         ResidentTransactionEntity residentTransactionEntity =getEntityFromEventId(eventId);
         return residentCredentialServiceImpl.prepareReqSummaryMsg(Collections.singletonList(
                    residentTransactionEntity.getAttributeList()));
    }

    public String getDescriptionTemplateVariablesForDownloadPersonalizedCard(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForOrderPhysicalCard(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForGetMyId(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForUpdateMyUin(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForManageMyVid(String eventId, String fileText) {
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.VID_TYPE,
                residentTransactionEntity.getRefIdType());
        fileText = fileText.replace(ResidentConstants.MASKED_VID, residentTransactionEntity.getRefId());
        String requestType = residentTransactionEntity.getRequestTypeCode();
        if (requestType.equalsIgnoreCase(RequestType.GENERATE_VID.name())) {
            fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.ACTION_PERFORMED, GENERATED);
        } else if (requestType.equalsIgnoreCase(RequestType.REVOKE_VID.name())) {
            fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.ACTION_PERFORMED, REVOKED);
        }
        return fileText;
    }

    public String getDescriptionTemplateVariablesForVidCardDownload(String eventId, String fileText){
        return fileText;
    }

    public String getDescriptionTemplateVariablesForValidateOtp(String eventId, String fileText) {
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        String purpose = residentTransactionEntity.getPurpose();
        if (purpose != null && !purpose.isEmpty()) {
            fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.CHANNEL,
                    purpose);
        }
        if (fileText.contains("\n")) {
            fileText = fileText.replace("\n", " ");
        }
        return fileText;
    }

    public String getDescriptionTemplateVariablesForSecureMyId(String eventId, String fileText){
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
            String purpose = residentTransactionEntity.getPurpose();
            if (purpose != null && !purpose.isEmpty())
                return fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.AUTH_TYPES,
                        purpose);
            return fileText;
    }

    public Tuple2<Map<String, String>, String> getDefaultTemplateVariables(String eventId, String languageCode){
        return Tuples.of(getCommonTemplateVariables(eventId, languageCode), "");
    }

    public String replaceNullWithEmptyString(String input) {
        return input == null ? "" : input;
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
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, null);
        return templateVariables.get(TemplateVariablesConstants.EVENT_TYPE);
    }

    public String getIndividualIdType() throws ApisResourceAccessException {
        String individualId= identityServiceImpl.getResidentIndvidualId();
        return identityServiceImpl.getIndividualIdType(individualId);
    }

    private String getEventStatusForRequestType(String requestType) {
        String eventStatus = "";
        if(EventStatusSuccess.containsStatus(requestType)){
            eventStatus = EventStatus.SUCCESS.getStatus();
        } else if(EventStatusFailure.containsStatus(requestType)){
            eventStatus = EventStatus.FAILED.getStatus();
        } else {
            eventStatus = EventStatus.IN_PROGRESS.getStatus();
        }
        return eventStatus;
    }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForCredentialShare(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesConstants.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
        templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_SHARE_CREDENTIAL_TEMPLATE_PROPERTY)));
    }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthenticationRequest(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesConstants.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
        templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_AUTHENTICATION_REQUEST_TEMPLATE_PROPERTY)));
     }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                getEntityFromEventId(eventId), languageCode));
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_DOWNLOAD_PERSONALIZED_CARD_TEMPLATE_PROPERTY)));
    }

     public  Tuple2<Map<String, String>, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId, String languageCode) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
         ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
         templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                 residentTransactionEntity, languageCode));
         templateVariables.put(TemplateVariablesConstants.TRACKING_ID, residentTransactionEntity.getTrackingId());
         templateVariables.put(TemplateVariablesConstants.ORDER_TRACKING_LINK, residentTransactionEntity.getReferenceLink());
         templateVariables.put(TemplateVariablesConstants.PARTNER_NAME, residentTransactionEntity.getRequestedEntityName());
         templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO, getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
         templateVariables.put(TemplateVariablesConstants.PAYMENT_STATUS, getPaymentStatus(residentTransactionEntity.getStatusCode()));
         templateVariables.put(TemplateVariablesConstants.DOWNLOAD_CARD_LINK, residentTransactionEntity.getReferenceLink());
         return Tuples.of(templateVariables, Objects.requireNonNull(
                 this.env.getProperty(ResidentConstants.ACK_ORDER_PHYSICAL_CARD_TEMPLATE_PROPERTY)));
     }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGetMyId(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                getEntityFromEventId(eventId), languageCode));
        templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_GET_MY_ID_TEMPLATE_PROPERTY)));
    }

     public  Tuple2<Map<String, String>, String> getAckTemplateVariablesForBookAnAppointment(String eventId, String languageCode) {
         return Tuples.of(Collections.emptyMap(), "");
     }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForUpdateMyUin(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                getEntityFromEventId(eventId), languageCode));
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_UPDATE_MY_UIN_TEMPLATE_PROPERTY)));
    }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGenerateVid(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                residentTransactionEntity, languageCode));
        templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
        templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
        templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
    }

    public String getPurposeFromResidentTransactionEntityLangCode(ResidentTransactionEntity residentTransactionEntity, String languageCode){
        String purpose = "";
        try {
            purpose = residentService.getSummaryForLangCode(languageCode, residentService.getEventStatusCode(
                            residentTransactionEntity.getStatusCode()),
                    RequestType.valueOf(residentTransactionEntity.getRequestTypeCode().trim()), residentTransactionEntity.getEventId());
        } catch (ResidentServiceCheckedException e) {
            return "";
        }
        return purpose;
    }

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForRevokeVid(String eventId, String languageCode) {
        Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
        ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
        templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                residentTransactionEntity, languageCode));
        templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
        templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
        return Tuples.of(templateVariables, Objects.requireNonNull(
                this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
    }

    public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId) {
        return getCommonTemplateVariables(eventId, "");
    }

     public  Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId, String languageCode) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
         templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                 getEntityFromEventId(eventId), languageCode));
         templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
         templateVariables.put(ResidentConstants.AUTH_TYPES, templateVariables.get(TemplateVariablesConstants.PURPOSE));
         return Tuples.of(templateVariables, Objects.requireNonNull(
                 this.env.getProperty(ResidentConstants.ACK_AUTH_TYPE_LOCK_UNLOCK_TEMPLATE_PROPERTY)));
     }

     public  Tuple2<Map<String, String>, String> getAckTemplateVariablesForVidCardDownload(String eventId, String languageCode) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
         templateVariables.put(TemplateVariablesConstants.PURPOSE, getPurposeFromResidentTransactionEntityLangCode(
                 getEntityFromEventId(eventId), languageCode));
         templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
         return Tuples.of(templateVariables, Objects.requireNonNull(
                 this.env.getProperty(ResidentConstants.ACK_VID_CARD_DOWNLOAD_TEMPLATE_PROPERTY)));
     }
     
     public Tuple2<Map<String, String>, String> getAckTemplateVariablesForSendOtp(String eventId, String languageCode) {
         return Tuples.of(getCommonTemplateVariables(eventId, languageCode), "");
     }
     
     public Tuple2<Map<String, String>, String> getAckTemplateVariablesForValidateOtp(String eventId, String languageCode) {
         Map<String, String> templateVariables = getCommonTemplateVariables(eventId, languageCode);
         templateVariables.put(ResidentConstants.CHANNEL, replaceNullWithEmptyString(
                 getEntityFromEventId(eventId).getAttributeList()));
         return Tuples.of(templateVariables, Objects.requireNonNull(
                 this.env.getProperty(ResidentConstants.ACK_VERIFY_PHONE_EMAIL_TEMPLATE_PROPERTY)));
     }
     
     public Map<String, Object> getNotificationCommonTemplateVariables(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = new HashMap<>();
 		templateVariables.put(TemplateVariablesConstants.EVENT_ID, dto.getEventId());
 		templateVariables.put(TemplateVariablesConstants.NAME, getName(dto.getLangCode()));
 		templateVariables.put(TemplateVariablesConstants.EVENT_DETAILS, dto.getRequestType().name());
 		templateVariables.put(TemplateVariablesConstants.DATE, getDate());
 		templateVariables.put(TemplateVariablesConstants.TIME, getTime());
 		templateVariables.put(TemplateVariablesConstants.STATUS, dto.getTemplateType().getType());
         if(TemplateType.FAILURE.getType().equals(dto.getTemplateType().getType())) {
 			templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK, utilitiy.createTrackServiceRequestLink(dto.getEventId()));
 		}
 		return templateVariables;
 	}

     public Map<String, Object> getNotificationSendOtpVariables(NotificationTemplateVariableDTO dto){
        Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
        templateVariables.put(TemplateVariablesConstants.OTP, dto.getOtp());
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
    			templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
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
   			templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
   		}
 		return templateVariables;
 	}
     
     public Map<String, Object> getNotificationTemplateVariablesForOrderPhysicalCard(NotificationTemplateVariableDTO dto) {
 		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
 		 if(TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
 			templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
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
    			templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK, utilitiy.createDownloadLink(dto.getEventId()));
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
        Map<String, ?> partnerDetail = new HashMap<>();
        try {
            partnerDetail = proxyPartnerManagementServiceImpl.getPartnerDetailFromPartnerId(partnerId);
        }catch (Exception exception){
            logger.error(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(), exception);
            return "";
        }
        return (String) partnerDetail.get(LOGO_URL);
	}

 }
