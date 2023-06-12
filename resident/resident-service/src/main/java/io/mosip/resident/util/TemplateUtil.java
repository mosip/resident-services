package io.mosip.resident.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.AuthenticationModeEnum;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ProxyPartnerManagementServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import io.mosip.resident.service.impl.UISchemaTypes;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * The Class TemplateUtil.
 * 
 * @author Kamesh Shekhar Prasad
 */

@Component
public class TemplateUtil {

	private static final String DEFAULT = "default";
	private static final String RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST = "resident.%s.template.property.attribute.list";
	private static final String LOGO_URL = "logoUrl";
	private static final CharSequence GENERATED = "generated";
	private static final CharSequence REVOKED = "revoked";
	private static final String UNKNOWN = "Unknown";

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ProxyPartnerManagementServiceImpl proxyPartnerManagementServiceImpl;

	@Autowired
	private Utility utility;

	@Autowired
	private ResidentServiceImpl residentService;

	@Autowired
	Environment env;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	@Autowired
	private ResidentConfigService residentConfigService;

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

	    public Tuple2<Map<String, String>, ResidentTransactionEntity> getCommonTemplateVariables(String eventId, String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put(TemplateVariablesConstants.EVENT_ID, eventId);
		ResidentTransactionEntity residentTransactionEntity = getEntityFromEventId(eventId);
		String statusCode = residentService.getEventStatusCode(residentTransactionEntity.getStatusCode());
		RequestType requestType = RequestType.getRequestTypeFromString(residentTransactionEntity.getRequestTypeCode());
		Optional<String> serviceType = ServiceType.getServiceTypeFromRequestType(requestType);
		templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, requestType.getName());
		templateVariables.put(TemplateVariablesConstants.EVENT_STATUS, statusCode);
		if (serviceType.isPresent()) {
			if (!serviceType.get().equals(ServiceType.ALL.name())) {
				templateVariables.put(TemplateVariablesConstants.SUMMARY,
						getSummaryFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode,
								statusCode, requestType));
			}
		} else {
			templateVariables.put(TemplateVariablesConstants.SUMMARY, requestType.name());
		}
		templateVariables.put(TemplateVariablesConstants.TIMESTAMP,
				utility.formatWithOffsetForUI(timeZoneOffset, locale, residentTransactionEntity.getCrDtimes()));
		templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK,
				utility.createTrackServiceRequestLink(eventId));
		templateVariables.put(TemplateVariablesConstants.PURPOSE, residentTransactionEntity.getPurpose());
		templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, getAttributesDisplayText(
				replaceNullWithEmptyString(residentTransactionEntity.getAttributeList()), languageCode, requestType));
		String authenticationMode = getAuthTypeCodeTemplateValue(
				replaceNullWithEmptyString(residentTransactionEntity.getAuthTypeCode()), languageCode);
		if (authenticationMode.equalsIgnoreCase(UNKNOWN)) {
			templateVariables.put(TemplateVariablesConstants.AUTHENTICATION_MODE,
					replaceNullWithEmptyString(residentTransactionEntity.getAuthTypeCode()));
		} else {
			templateVariables.put(TemplateVariablesConstants.AUTHENTICATION_MODE, authenticationMode);
		}
		try {
			templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, getIndividualIdType());
		} catch (ApisResourceAccessException e) {
			logger.error(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), e);
			templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, "");
		}
		return Tuples.of(templateVariables, residentTransactionEntity);
	}

	/**
	 * This method accepts a string having comma-separated attributes with camel
	 * case convention and splits it by a comma. Then it takes each attribute value
	 * from the template in logged-in language and appends it to a string with
	 * comma-separated value.
	 * 
	 * @param attributesFromDB attribute values having comma separated attributes.
	 * @param languageCode     logged in language code.
	 * @return attribute value stored in the template.
	 */
	private String getAttributesDisplayText(String attributesFromDB, String languageCode, RequestType requestType) {
		List<String> attributeListTemplateValue = new ArrayList<>();
		if (attributesFromDB != null && !attributesFromDB.isEmpty()) {
			Optional<String> schemaType = UISchemaTypes.getUISchemaTypeFromRequestTypeCode(requestType);
			if (schemaType.isPresent() && attributesFromDB.contains(ResidentConstants.SEMI_COLON)) {
//	    		Cacheable UI Schema data
				Map<String, Map<String, Object>> uiSchemaDataMap = residentConfigService
						.getUISchemaCacheableData(schemaType.get()).get(languageCode);
				List<String> attributeListFromDB = List.of(attributesFromDB.split(ResidentConstants.SEMI_COLON));
				attributeListTemplateValue = attributeListFromDB.stream().map(attribute -> {
					String[] attrArray = attribute.trim().split(ResidentConstants.COLON);
					String attr = attrArray[0];
					if (uiSchemaDataMap.containsKey(attr)) {
						Map<String, Object> attributeDataFromUISchema = (Map<String, Object>) uiSchemaDataMap.get(attr);
						attr = (String) attributeDataFromUISchema.get(ResidentConstants.LABEL);
						if (attrArray.length > 1) {
							String formatAttr = attrArray[1];
							Map<String, String> formatDataMapFromUISchema = (Map<String, String>) attributeDataFromUISchema
									.get(ResidentConstants.FORMAT_OPTION);
							List<String> formatAttrList = List
									.of(formatAttr.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
									.map(String::trim).map(format -> formatDataMapFromUISchema.get(format))
									.collect(Collectors.toList());
							if (!formatAttrList.contains(null)) {
								return String.format("%s%s%s%s", attr, ResidentConstants.OPEN_PARENTHESIS,
										formatAttrList.stream().collect(
												Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER)),
										ResidentConstants.CLOSE_PARENTHESIS);
							}
						}
					}
					return attr;
				}).collect(Collectors.toList());
			} else {
				List<String> attributeListFromDB = List
						.of(attributesFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER));
				for (String attribute : attributeListFromDB) {
					attributeListTemplateValue.add(getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode,
							getAttributeListTemplateTypeCode(attribute.trim())));
				}
			}
		}
		if (attributeListTemplateValue.isEmpty()) {
			return "";
		} else {
			return attributeListTemplateValue.stream()
					.collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
		}
	}

	private String getAuthTypeCodeTemplateValue(String authenticationMode, String languageCode) {
		return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode,
				AuthenticationModeEnum.getTemplatePropertyName(authenticationMode, env));
	}

	public String getTemplateValueFromTemplateTypeCodeAndLangCode(String languageCode, String templateTypeCode) {
		try {
			ResponseWrapper<?> proxyResponseWrapper = proxyMasterdataService
					.getAllTemplateBylangCodeAndTemplateTypeCode(languageCode, templateTypeCode);
			logger.debug(String.format("Template data from DB:- %s", proxyResponseWrapper.getResponse()));
			Map<String, String> templateResponse = new LinkedHashMap<>(
					(Map<String, String>) proxyResponseWrapper.getResponse());
			return templateResponse.get(ResidentConstants.FILE_TEXT);
		} catch (ResidentServiceCheckedException e) {
			throw new ResidentServiceException(ResidentErrorCode.TEMPLATE_EXCEPTION, e);
		}
	}

	public String getDescriptionTemplateVariablesForAuthenticationRequest(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		return residentTransactionEntity.getStatusComment();
	}

	public String getDescriptionTemplateVariablesForShareCredential(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return addAttributeListInPurpose(fileText, residentTransactionEntity.getAttributeList(), languageCode);
	}

	public String getDescriptionTemplateVariablesForDownloadPersonalizedCard(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		return addAttributeListInPurpose(fileText, residentTransactionEntity.getAttributeList(), languageCode);
	}

	public String getDescriptionTemplateVariablesForOrderPhysicalCard(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		return fileText;
	}

	public String getDescriptionTemplateVariablesForGetMyId(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return fileText;
	}

	public String getDescriptionTemplateVariablesForUpdateMyUin(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return addAttributeListInPurpose(fileText, residentTransactionEntity.getAttributeList(), languageCode);
	}

	public String getDescriptionTemplateVariablesForManageMyVid(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.VID_TYPE,
				replaceNullWithEmptyString(residentTransactionEntity.getRefIdType()));
		fileText = fileText.replace(ResidentConstants.MASKED_VID,
				replaceNullWithEmptyString(residentTransactionEntity.getRefId()));
		String requestType = residentTransactionEntity.getRequestTypeCode();
		if (requestType.equalsIgnoreCase(RequestType.GENERATE_VID.name())) {
			fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.ACTION_PERFORMED, GENERATED);
		} else if (requestType.equalsIgnoreCase(RequestType.REVOKE_VID.name())) {
			fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.ACTION_PERFORMED, REVOKED);
		}
		return fileText;
	}

	public String getDescriptionTemplateVariablesForVidCardDownload(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return fileText;
	}

	public String getDescriptionTemplateVariablesForValidateOtp(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		String channels = residentTransactionEntity.getPurpose();
		if (channels != null && !channels.isEmpty()) {
			fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.CHANNEL, channels);
		}
		return fileText;
	}

	public String getDescriptionTemplateVariablesForSecureMyId(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		if (residentTransactionEntity.getPurpose() != null && !residentTransactionEntity.getPurpose().isEmpty()) {
			List<String> authTypeListFromEntity = List
					.of(residentTransactionEntity.getPurpose().split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER));
			return authTypeListFromEntity.stream().map(authType -> {
				String fileTextTemplate = fileText;
				String templateData = "";
				if (authType.contains(EventStatusSuccess.UNLOCKED.name())) {
					templateData = getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode,
							getAttributeListTemplateTypeCode(EventStatusSuccess.UNLOCKED.name()));
					fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
							templateData);
				} else {
					templateData = getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode,
							getAttributeListTemplateTypeCode(EventStatusSuccess.LOCKED.name()));
					fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
							templateData);
				}
				templateData = getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode,
						getAttributeListTemplateTypeCode(authType.split(ResidentConstants.COLON)[0].trim()));
				fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.AUTH_TYPE,
						templateData);
				return fileTextTemplate;
			}).collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
		}
		return fileText;
	}

    public Tuple2<Map<String, String>, String> getDefaultTemplateVariables(String eventId, String languageCode, Integer timeZoneOffset, String locale){
        return Tuples.of(getCommonTemplateVariables(eventId, languageCode, timeZoneOffset, locale).getT1(), "");
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

	public String getFeatureName(String eventId, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				this.env.getProperty(ResidentConstants.MANDATORY_LANGUAGE), ResidentConstants.UTC_TIMEZONE_OFFSET, null);
		Map<String, String> templateVariables = tupleResponse.getT1();
		return templateVariables.get(TemplateVariablesConstants.EVENT_TYPE);
	}

	public String getIndividualIdType() throws ApisResourceAccessException {
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		return identityServiceImpl.getIndividualIdType(individualId);
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForCredentialShare(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_SHARE_CREDENTIAL_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthenticationRequest(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_AUTHENTICATION_REQUEST_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForDownloadPersonalizedCard(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		return Tuples.of(templateVariables, Objects.requireNonNull(
				this.env.getProperty(ResidentConstants.ACK_DOWNLOAD_PERSONALIZED_CARD_TEMPLATE_PROPERTY)));
	}

	/**
	 * This method will replace attribute placeholder in template and add attribute
	 * list into it.
	 * 
	 * @param fileText     This contains value of template.
	 * @param attributes   This contains attributes of request type stored in
	 *                     template.
	 * @param languageCode This contains logged-in language code.
	 * @return purpose after adding attributes.
	 */
	private String addAttributeListInPurpose(String fileText, String attributes, String languageCode) {
		if (fileText != null && fileText.contains(ResidentConstants.ATTRIBUTES)) {
			fileText = fileText.replace(ResidentConstants.DOLLAR + ResidentConstants.ATTRIBUTES,
					getAttributesDisplayText(attributes, languageCode, RequestType.DEFAULT));
		}
		return fileText;
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForOrderPhysicalCard(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.put(TemplateVariablesConstants.TRACKING_ID, residentTransactionEntity.getTrackingId());
		templateVariables.put(TemplateVariablesConstants.ORDER_TRACKING_LINK,
				residentTransactionEntity.getReferenceLink());
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId()));
		templateVariables.put(TemplateVariablesConstants.PAYMENT_STATUS,
				getPaymentStatus(residentTransactionEntity.getStatusCode()));
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_CARD_LINK,
				residentTransactionEntity.getReferenceLink());
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_ORDER_PHYSICAL_CARD_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGetMyId(String eventId, String languageCode,
			Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_GET_MY_ID_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForBookAnAppointment(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		return Tuples.of(Collections.emptyMap(), "");
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForUpdateMyUin(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK,
				(!residentTransactionEntity.getStatusCode().equals(EventStatusSuccess.CARD_DOWNLOADED.name())
						&& !residentTransactionEntity.getStatusCode().equals(EventStatusFailure.FAILED.name()))
								? utility.createDownloadCardLinkFromEventId(residentTransactionEntity)
								: ResidentConstants.NOT_AVAILABLE);
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_UPDATE_MY_UIN_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGenerateVid(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
		templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
		templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
	}

	public String getPurposeFromResidentTransactionEntityLangCode(ResidentTransactionEntity residentTransactionEntity,
			String languageCode) {
		String purpose = "";
		try {
			purpose = residentService.getDescriptionForLangCode(residentTransactionEntity, languageCode,
					residentService.getEventStatusCode(residentTransactionEntity.getStatusCode()),
					RequestType.getRequestTypeFromString(residentTransactionEntity.getRequestTypeCode()));
		} catch (ResidentServiceCheckedException e) {
			return "";
		}
		return purpose;
	}

	public String getSummaryFromResidentTransactionEntityLangCode(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, String statusCode, RequestType requestType) {
		try {
			return residentService.getSummaryForLangCode(residentTransactionEntity, languageCode, statusCode,
					requestType);
		} catch (ResidentServiceCheckedException e) {
			return requestType.name();
		}
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForRevokeVid(String eventId, String languageCode,
			Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
		templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
	}

	public Map<String, String> getAckTemplateVariablesForVerifyPhoneEmail(String eventId, Integer timeZoneOffset, String locale) {
		return getCommonTemplateVariables(eventId, "", timeZoneOffset, locale).getT1();
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthTypeLockUnlock(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
		templateVariables.put(ResidentConstants.AUTH_TYPE, templateVariables.get(TemplateVariablesConstants.PURPOSE));
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_AUTH_TYPE_LOCK_UNLOCK_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForVidCardDownload(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(TemplateVariablesConstants.PURPOSE,
				getPurposeFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode));
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK,
				(!residentTransactionEntity.getStatusCode().equals(EventStatusSuccess.CARD_DOWNLOADED.name())
						&& !residentTransactionEntity.getStatusCode().equals(EventStatusFailure.FAILED.name()))
								? utility.createDownloadCardLinkFromEventId(residentTransactionEntity)
								: ResidentConstants.NOT_AVAILABLE);
		templateVariables.remove(TemplateVariablesConstants.ATTRIBUTE_LIST);
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_VID_CARD_DOWNLOAD_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForSendOtp(String eventId, String languageCode,
			Integer timeZoneOffset, String locale) {
		return Tuples.of(getCommonTemplateVariables(eventId, languageCode, timeZoneOffset, locale).getT1(), "");
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForValidateOtp(String eventId,
			String languageCode, Integer timeZoneOffset, String locale) {
		Tuple2<Map<String, String>, ResidentTransactionEntity> tupleResponse = getCommonTemplateVariables(eventId,
				languageCode, timeZoneOffset, locale);
		Map<String, String> templateVariables = tupleResponse.getT1();
		ResidentTransactionEntity residentTransactionEntity = tupleResponse.getT2();
		templateVariables.put(ResidentConstants.CHANNEL,
				replaceNullWithEmptyString(residentTransactionEntity.getAttributeList()));
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_VERIFY_PHONE_EMAIL_TEMPLATE_PROPERTY)));
	}

	public Map<String, Object> getNotificationCommonTemplateVariables(NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = new HashMap<>();
		templateVariables.put(TemplateVariablesConstants.EVENT_ID, dto.getEventId());
		templateVariables.put(TemplateVariablesConstants.NAME, getName(dto.getLangCode(), dto.getEventId()));
		templateVariables.put(TemplateVariablesConstants.EVENT_DETAILS, dto.getRequestType().getName());
		templateVariables.put(TemplateVariablesConstants.DATE, getDate());
		templateVariables.put(TemplateVariablesConstants.TIME, getTime());
		templateVariables.put(TemplateVariablesConstants.STATUS, dto.getTemplateType().getType());
		templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK,
				utility.createTrackServiceRequestLink(dto.getEventId()));
		return templateVariables;
	}

	public Map<String, Object> getNotificationSendOtpVariables(NotificationTemplateVariableDTO dto) {
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

	private String getName(String language, String eventId) {
		String name = "";
		String individualId = "";
		try {
			if (Utility.isSecureSession()) {
				individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
			} else {
				individualId = getEntityFromEventId(eventId).getIndividualId();
			}

			if (individualId != null && !individualId.isEmpty()) {
				Map<String, ?> idMap = identityServiceImpl.getIdentityAttributes(individualId,
						UISchemaTypes.UPDATE_DEMOGRAPHICS.getFileIdentifier());
				name = identityServiceImpl.getNameForNotification(idMap, language);
			}
		} catch (ApisResourceAccessException | ResidentServiceCheckedException | IOException
				| ResidentServiceException e) {
			logger.error(String.format("Error occured while getting individualId: %s : %s : %s",
					e.getClass().getSimpleName(), e.getMessage(),
					(e.getCause() != null ? "rootcause: " + e.getCause().getMessage() : "")));
		}
		return name;
	}

	public Map<String, Object> getNotificationTemplateVariablesForGenerateOrRevokeVid(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForAuthTypeLockUnlock(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForUpdateMyUin(NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForVerifyPhoneEmail(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForGetMyId(NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForDownloadPersonalizedCard(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForOrderPhysicalCard(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForShareCredentialWithPartner(
			NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
		if (TemplateType.SUCCESS.getType().equals(dto.getTemplateType().getType())) {
			templateVariables.put(TemplateVariablesConstants.PARTNER_ID,
					getEntityFromEventId(dto.getEventId()).getRequestedEntityId());
		}
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForVidCardDownload(NotificationTemplateVariableDTO dto) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto);
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

	public String getAttributeListTemplateTypeCode(String attributeName) {
		String templateTypeCode = getTemplateTypeCode(
				String.format(RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST, attributeName));
		if (templateTypeCode != null && !templateTypeCode.isEmpty()) {
			return templateTypeCode;
		} else {
			return getTemplateTypeCode(String.format(RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST, DEFAULT));
		}
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
		} catch (Exception exception) {
			logger.error(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(), exception);
			return "";
		}
		return (String) partnerDetail.get(LOGO_URL);
	}

}
