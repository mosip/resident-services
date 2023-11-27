package io.mosip.resident.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

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
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.constant.UISchemaTypes;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ProxyPartnerManagementService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import io.mosip.resident.service.impl.ResidentServiceImpl;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * The Class TemplateUtil.
 * 
 * @author Kamesh Shekhar Prasad
 */

@Component
public class TemplateUtil {

	private static final String RESIDENT = "Resident";
	private static final String LOGO_URL = "logoUrl";
	private static final String UNKNOWN = "UNKNOWN";
	private static final String RESIDENT_AUTH_TYPE_CODE_TEMPLATE_PROPERTY = "resident.auth-type-code.%s.code";
	private static final String RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR = "resident.id-auth.request-type.%s.%s.descr";
	private static final String RESIDENT_EVENT_TYPE_TEMPLATE_PROPERTY = "resident.event.type.%s.template.property";
	private static final String RESIDENT_SERVICE_TYPE_TEMPLATE_PROPERTY = "resident.service-type.%s.template.property";
	private static final String RESIDENT_TEMPLATE_EVENT_STATUS = "resident.event.status.%s.template.property";
	private static final String RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST = "resident.%s.template.property.attribute.list";

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ProxyPartnerManagementService proxyPartnerManagementService;

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

	    public Map<String, String> getCommonTemplateVariables(ResidentTransactionEntity residentTransactionEntity, RequestType requestType, String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = new HashMap<>();
		templateVariables.put(TemplateVariablesConstants.EVENT_ID, residentTransactionEntity.getEventId());
		Tuple2<String, String> statusCodes = residentService.getEventStatusCode(residentTransactionEntity.getStatusCode(), languageCode);
		Optional<String> serviceType = ServiceType.getServiceTypeFromRequestType(requestType);
		String eventTypeBasedOnLangcode = getEventTypeBasedOnLangcode(requestType, languageCode);
		templateVariables.put(TemplateVariablesConstants.EVENT_TYPE, eventTypeBasedOnLangcode);
		templateVariables.put(TemplateVariablesConstants.EVENT_TYPE_ENUM, requestType.name());
		templateVariables.put(TemplateVariablesConstants.EVENT_STATUS, statusCodes.getT2());
		templateVariables.put(TemplateVariablesConstants.EVENT_STATUS_ENUM, statusCodes.getT1());
		if (serviceType.isPresent()) {
			if (!serviceType.get().equals(ServiceType.ALL.name())) {
				templateVariables.put(TemplateVariablesConstants.SUMMARY,
						getSummaryFromResidentTransactionEntityLangCode(residentTransactionEntity, languageCode,
								statusCodes.getT1(), requestType));
			}
		} else {
			templateVariables.put(TemplateVariablesConstants.SUMMARY, eventTypeBasedOnLangcode);
		}
		templateVariables.put(TemplateVariablesConstants.TIMESTAMP,
				utility.formatWithOffsetForUI(timeZoneOffset, locale, residentTransactionEntity.getCrDtimes()));
		templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK,
				utility.createTrackServiceRequestLink(residentTransactionEntity.getEventId()));
		templateVariables.put(TemplateVariablesConstants.PDF_HEADER_LOGO, utility.getPDFHeaderLogo());
		templateVariables.put(TemplateVariablesConstants.AUTHENTICATION_MODE,
				getAuthTypeCodeTemplateData(residentTransactionEntity.getAuthTypeCode(), null, languageCode));
		try {
			templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, getIndividualIdType());
		} catch (ApisResourceAccessException e) {
			logger.error(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(), e);
			templateVariables.put(TemplateVariablesConstants.INDIVIDUAL_ID, "");
		}
		return templateVariables;
	}

	public String getEventTypeBasedOnLangcode(RequestType requestType, String languageCode) {
		String templateCodeProperty = String.format(RESIDENT_EVENT_TYPE_TEMPLATE_PROPERTY, requestType.name());
		String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", requestType.name()));
			templateTypeCode = getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
		}
		return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
	}

	public String getServiceTypeBasedOnLangcode(ServiceType serviceType, String languageCode) {
		String templateCodeProperty = String.format(RESIDENT_SERVICE_TYPE_TEMPLATE_PROPERTY, serviceType.name());
		String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", serviceType.name()));
			templateTypeCode = getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
		}
		return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
	}

	public String getEventStatusBasedOnLangcode(EventStatus eventStatus, String languageCode) {
		String templateCodeProperty = String.format(RESIDENT_TEMPLATE_EVENT_STATUS, eventStatus.name());
		String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", eventStatus.name()));
			templateTypeCode = getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
		}
		return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
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
	@SuppressWarnings("unchecked")
	private String getAttributesDisplayText(String attributesFromDB, String languageCode, RequestType requestType) {
		List<String> attributeListTemplateValue = new ArrayList<>();
		if (attributesFromDB != null && !attributesFromDB.isEmpty()) {
			Optional<String> schemaType = UISchemaTypes.getUISchemaTypeFromRequestTypeCode(requestType);
			if (schemaType.isPresent()) {
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
				attributeListTemplateValue = List.of(attributesFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
						.map(attribute -> getAttributeBasedOnLangcode(attribute.trim(), languageCode))
						.collect(Collectors.toList());
			}
		}
		if (attributeListTemplateValue.isEmpty()) {
			return "";
		} else {
			return attributeListTemplateValue.stream()
					.collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
		}
	}

	public String getTemplateValueFromTemplateTypeCodeAndLangCode(String languageCode, String templateTypeCode) {
		return proxyMasterdataService
					.getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
	}

	public String getDescriptionTemplateVariablesForAuthenticationRequest(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		String statusCode = residentService.getEventStatusCode(residentTransactionEntity.getStatusCode(), languageCode)
				.getT1();
		return getAuthTypeCodeTemplateData(residentTransactionEntity.getAuthTypeCode(), statusCode, languageCode);
	}

	private String getAuthTypeCodeTemplateData(String authTypeCodeFromDB, String statusCode, String languageCode) {
		List<String> authTypeCodeTemplateValues = new ArrayList<>();
		if (authTypeCodeFromDB != null && !authTypeCodeFromDB.isEmpty()) {
			authTypeCodeTemplateValues = List.of(authTypeCodeFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
					.map(authTypeCode -> {
						String templateTypeCode;
						if(statusCode == null) {
							templateTypeCode = getAuthTypeCodeTemplateTypeCode(authTypeCode.trim());
						} else {
							templateTypeCode = getIDAuthRequestTypeDescriptionTemplateTypeCode(authTypeCode.trim(), statusCode);
						}
						return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
					})
					.collect(Collectors.toList());
		}

		if (authTypeCodeTemplateValues.isEmpty()) {
			return "";
		} else {
			return authTypeCodeTemplateValues.stream()
					.collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
		}
	}

	public String getDescriptionTemplateVariablesForShareCredentialWithPartner(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return fileText;
	}

	public String getDescriptionTemplateVariablesForDownloadPersonalizedCard(
			ResidentTransactionEntity residentTransactionEntity, String fileText, String languageCode) {
		return fileText;
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
		return fileText;
	}

	public String getDescriptionTemplateVariablesForManageMyVid(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		RequestType requestType = RequestType.DEFAULT;
		String templateData = "";
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.VID_TYPE,
				replaceNullWithEmptyString(residentTransactionEntity.getRefIdType()));
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.MASKED_VID,
				replaceNullWithEmptyString(residentTransactionEntity.getRefId()));
		if (RequestType.GENERATE_VID.name().equalsIgnoreCase(residentTransactionEntity.getRequestTypeCode())) {
			requestType = RequestType.GENERATE_VID;
		} else if (RequestType.REVOKE_VID.name().equalsIgnoreCase(residentTransactionEntity.getRequestTypeCode())) {
			requestType = RequestType.REVOKE_VID;
		}
		templateData = getAttributeBasedOnLangcode(requestType.name(), languageCode);
		fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.ACTION_PERFORMED, templateData);
		return fileText;
	}

	public String getDescriptionTemplateVariablesForVidCardDownload(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		return fileText;
	}

	public String getDescriptionTemplateVariablesForValidateOtp(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		String channelsTemplateData = getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.VALIDATE_OTP);
			fileText = fileText.replace(ResidentConstants.DOLLAR + TemplateVariablesConstants.CHANNEL, channelsTemplateData);
		return fileText;
	}

	public String getDescriptionTemplateVariablesForSecureMyId(ResidentTransactionEntity residentTransactionEntity,
			String fileText, String languageCode) {
		String authTypeFromDB;
		if (residentTransactionEntity.getAttributeList() != null && !residentTransactionEntity.getAttributeList().isEmpty()) {
			authTypeFromDB = residentTransactionEntity.getAttributeList();
		} else {
			authTypeFromDB = residentTransactionEntity.getPurpose();
		}
		if (authTypeFromDB != null) {
			List<String> authTypeListFromEntity = List
					.of(authTypeFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER));
			return authTypeListFromEntity.stream().map(authType -> {
				String fileTextTemplate = fileText;
				String templateData = "";
				if (authType.contains(EventStatusSuccess.UNLOCKED.name())) {
					templateData = getAttributeBasedOnLangcode(EventStatusSuccess.UNLOCKED.name(), languageCode);
					fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
							templateData);
				} else {
					templateData = getAttributeBasedOnLangcode(EventStatusSuccess.LOCKED.name(), languageCode);
					fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.STATUS,
							templateData);
				}
				templateData = getAttributeBasedOnLangcode(authType.split(ResidentConstants.COLON)[0].trim(), languageCode);
				fileTextTemplate = fileTextTemplate.replace(ResidentConstants.DOLLAR + ResidentConstants.AUTH_TYPE,
						templateData);
				return fileTextTemplate;
			}).collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
		}
		return fileText;
	}

    public Tuple2<Map<String, String>, String> getAckTemplateVariablesForDefault(ResidentTransactionEntity residentTransactionEntity, String languageCode, Integer timeZoneOffset, String locale){
        return Tuples.of(getCommonTemplateVariables(residentTransactionEntity, RequestType.DEFAULT, languageCode, timeZoneOffset, locale), "");
    }

    public String replaceNullWithEmptyString(String input) {
        return input == null ? "" : input;
    }

	public String getIndividualIdType() throws ApisResourceAccessException {
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		return identityServiceImpl.getIndividualIdType(individualId).name();
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForShareCredentialWithPartner(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.SHARE_CRED_WITH_PARTNER,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.SHARE_CRED_WITH_PARTNER));
		templateVariables.put(TemplateVariablesConstants.PURPOSE, residentTransactionEntity.getPurpose());
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId(), residentTransactionEntity.getRequestedEntityType()));
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_SHARE_CREDENTIAL_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthenticationRequest(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.AUTHENTICATION_REQUEST,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId(), env.getProperty(
						ResidentConstants.RESIDENT_AUTHENTICATION_REQUEST_PARTNER_TYPE, ResidentConstants.AUTH_PARTNER)));
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_AUTHENTICATION_REQUEST_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForDownloadPersonalizedCard(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.DOWNLOAD_PERSONALIZED_CARD,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.DOWNLOAD_PERSONALIZED_CARD));
		return Tuples.of(templateVariables, Objects.requireNonNull(
				this.env.getProperty(ResidentConstants.ACK_DOWNLOAD_PERSONALIZED_CARD_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForOrderPhysicalCard(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.ORDER_PHYSICAL_CARD,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.ORDER_PHYSICAL_CARD));
		templateVariables.put(TemplateVariablesConstants.TRACKING_ID, residentTransactionEntity.getTrackingId());
		templateVariables.put(TemplateVariablesConstants.ORDER_TRACKING_LINK,
				residentTransactionEntity.getReferenceLink());
		templateVariables.put(TemplateVariablesConstants.PARTNER_NAME,
				residentTransactionEntity.getRequestedEntityName());
		templateVariables.put(TemplateVariablesConstants.PARTNER_LOGO,
				getPartnerLogo(residentTransactionEntity.getRequestedEntityId(), residentTransactionEntity.getRequestedEntityType()));
		templateVariables.put(TemplateVariablesConstants.PAYMENT_STATUS,
				getPaymentStatus(residentTransactionEntity.getStatusCode()));
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_CARD_LINK,
				residentTransactionEntity.getReferenceLink());
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_ORDER_PHYSICAL_CARD_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGetMyId(ResidentTransactionEntity residentTransactionEntity, String languageCode,
			Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.GET_MY_ID,
				languageCode, timeZoneOffset, locale);
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_GET_MY_ID_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForUpdateMyUin(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.UPDATE_MY_UIN,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.ATTRIBUTE_LIST, getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.UPDATE_MY_UIN));
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK,
				(!residentTransactionEntity.getStatusCode().equals(EventStatusSuccess.CARD_DOWNLOADED.name())
						&& !residentTransactionEntity.getStatusCode().equals(EventStatusFailure.FAILED.name()))
								? utility.createDownloadCardLinkFromEventId(residentTransactionEntity)
								: ResidentConstants.NOT_AVAILABLE);
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_UPDATE_MY_UIN_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForGenerateVid(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.GENERATE_VID,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
		templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
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

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForRevokeVid(ResidentTransactionEntity residentTransactionEntity, String languageCode,
			Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.REVOKE_VID,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.VID_TYPE, residentTransactionEntity.getRefIdType());
		templateVariables.put(TemplateVariablesConstants.VID, residentTransactionEntity.getRefId());
		return Tuples.of(templateVariables,
				Objects.requireNonNull(this.env.getProperty(ResidentConstants.ACK_MANAGE_MY_VID_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForAuthTypeLockUnlock(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.AUTH_TYPE_LOCK_UNLOCK,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(ResidentConstants.AUTH_TYPE, residentTransactionEntity.getAttributeList());
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_AUTH_TYPE_LOCK_UNLOCK_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForVidCardDownload(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.VID_CARD_DOWNLOAD,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.DOWNLOAD_LINK,
				(!residentTransactionEntity.getStatusCode().equals(EventStatusSuccess.CARD_DOWNLOADED.name())
						&& !residentTransactionEntity.getStatusCode().equals(EventStatusFailure.FAILED.name()))
								? utility.createDownloadCardLinkFromEventId(residentTransactionEntity)
								: ResidentConstants.NOT_AVAILABLE);
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_VID_CARD_DOWNLOAD_TEMPLATE_PROPERTY)));
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForSendOtp(ResidentTransactionEntity residentTransactionEntity, String languageCode,
			Integer timeZoneOffset, String locale) {
		return Tuples.of(getCommonTemplateVariables(residentTransactionEntity, RequestType.SEND_OTP, languageCode, timeZoneOffset, locale), "");
	}

	public Tuple2<Map<String, String>, String> getAckTemplateVariablesForValidateOtp(ResidentTransactionEntity residentTransactionEntity,
			String languageCode, Integer timeZoneOffset, String locale) {
		Map<String, String> templateVariables = getCommonTemplateVariables(residentTransactionEntity, RequestType.VALIDATE_OTP,
				languageCode, timeZoneOffset, locale);
		templateVariables.put(TemplateVariablesConstants.CHANNEL, getAttributesDisplayText(
				residentTransactionEntity.getAttributeList(), languageCode, RequestType.VALIDATE_OTP));
		return Tuples.of(templateVariables, Objects
				.requireNonNull(this.env.getProperty(ResidentConstants.ACK_VERIFY_PHONE_EMAIL_TEMPLATE_PROPERTY)));
	}

	public Map<String, Object> getNotificationCommonTemplateVariables(NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = new HashMap<>();
		String langCode = dto.getLangCode();
		try {
			String name = utility.getMappingValue(notificationAttributes, TemplateVariablesConstants.NAME, langCode);
			templateVariables.put(TemplateVariablesConstants.NAME, name);
		} catch (ResidentServiceCheckedException | IOException e) {
			logger.error("Error in getting name.. " + e.getMessage());
			templateVariables.put(TemplateVariablesConstants.NAME, RESIDENT);
		}
		templateVariables.put(TemplateVariablesConstants.EVENT_ID, dto.getEventId());
		templateVariables.put(TemplateVariablesConstants.EVENT_DETAILS, getEventTypeBasedOnLangcode(dto.getRequestType(), langCode));
		templateVariables.put(TemplateVariablesConstants.DATE, getDate());
		templateVariables.put(TemplateVariablesConstants.TIME, getTime());
		
		templateVariables.put(TemplateVariablesConstants.STATUS, dto.getTemplateType().getType());
		templateVariables.put(TemplateVariablesConstants.TRACK_SERVICE_REQUEST_LINK,
				utility.createTrackServiceRequestLink(dto.getEventId()));
		return templateVariables;
	}

	public Map<String, Object> getNotificationSendOtpVariables(NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		templateVariables.put(TemplateVariablesConstants.OTP, dto.getOtp());
		return templateVariables;
	}

	private String getTime() {
		return DateUtils.getUTCCurrentDateTimeString(templateTimePattern);
	}

	private String getDate() {
		return DateUtils.getUTCCurrentDateTimeString(templateDatePattern);
	}

	public Map<String, Object> getNotificationTemplateVariablesForGenerateOrRevokeVid(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForAuthTypeLockUnlock(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForUpdateMyUin(NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForVerifyPhoneEmail(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForGetMyId(NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForDownloadPersonalizedCard(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForOrderPhysicalCard(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForShareCredentialWithPartner(
			NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
		templateVariables.put(TemplateVariablesConstants.PARTNER_ID,
				notificationAttributes.get(TemplateVariablesConstants.PARTNER_ID));
		return templateVariables;
	}

	public Map<String, Object> getNotificationTemplateVariablesForVidCardDownload(NotificationTemplateVariableDTO dto, Map<String, Object> notificationAttributes) {
		Map<String, Object> templateVariables = getNotificationCommonTemplateVariables(dto, notificationAttributes);
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

	public String getPurposeTemplateTypeCode(RequestType requestType, TemplateType templateType) {
		String purposeTemplateCodeProperty = requestType.getPurposeTemplateCodeProperty(templateType);
		return getTemplateTypeCode(purposeTemplateCodeProperty);
	}

	public String getSummaryTemplateTypeCode(RequestType requestType, TemplateType templateType) {
		String summaryTemplateCodeProperty = requestType.getSummaryTemplateCodeProperty(templateType);
		return getTemplateTypeCode(summaryTemplateCodeProperty);
	}

	private String getAuthTypeCodeTemplateTypeCode(String authTypeCode) {
		String templateCodeProperty = String.format(RESIDENT_AUTH_TYPE_CODE_TEMPLATE_PROPERTY, authTypeCode);
		String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", authTypeCode));
			return getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
		} else {
			return templateTypeCode;
		}
	}

	private String getIDAuthRequestTypeDescriptionTemplateTypeCode(String authTypeCode, String statusCode) {
		String templateCodeProperty = String.format(RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR, authTypeCode, statusCode);
		String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", authTypeCode));
			return getTemplateTypeCode(String.format(RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR, UNKNOWN, statusCode));
		} else {
			return templateTypeCode;
		}
	}

	public String getAttributeBasedOnLangcode(String attributeName, String languageCode) {
		String templateTypeCode = getTemplateTypeCode(
				String.format(RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST, attributeName));
		if (templateTypeCode == null) {
			logger.warn(String.format("Template property is missing for %s", attributeName));
			templateTypeCode = getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
		}
		return getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
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

	private String getPartnerLogo(String partnerId, String partnerType) {
		Map<String, ?> partnerDetail = new HashMap<>();
		try {
			partnerDetail = proxyPartnerManagementService.getPartnerDetailFromPartnerIdAndPartnerType(partnerId,
					partnerType);
		} catch (Exception exception) {
			logger.error(ResidentErrorCode.PARTNER_SERVICE_EXCEPTION.getErrorCode(), exception);
			return "";
		}
		return (String) partnerDetail.get(LOGO_URL);
	}

}
