package io.mosip.resident.service;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.NotificationTemplateVariableDTO;
import io.mosip.resident.dto.SMSRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 
 * @author Girish Yarru
 *
 */
@Component
public class NotificationService {
	private static final String LINE_BREAK = "<br>";
	private static final String EMAIL_CHANNEL = "email";
	private static final String PHONE_CHANNEL = "phone";
	private static final String IDENTITY = "identity";
	private static final Logger logger = LoggerConfiguration.logConfig(NotificationService.class);
	@Autowired
	private TemplateManager templateManager;

	@Value("${resident.notification.emails}")
	private String notificationEmails;

	@Value("${mosip.notificationtype}")
	private String notificationType;

	@Autowired
	private Environment env;

	@Autowired
	private ResidentServiceRestClient restClient;

	@Autowired
	private Utility utility;
	
	@Autowired
	private Utilities utilities;

	@Autowired
	private RequestValidator requestValidator;
	
	@Autowired
	private AuditUtil audit;
	
	@Autowired
	private TemplateUtil templateUtil;

	private static final String LINE_SEPARATOR = new  StringBuilder().append(LINE_BREAK).append(LINE_BREAK).toString();
	private static final String EMAIL = "_EMAIL";
	private static final String SMS = "_SMS";
	private static final String SUBJECT = "_SUB";
	private static final String SMS_EMAIL_SUCCESS = "Notification has been sent to the provided contact detail(s)";
	private static final String SMS_SUCCESS = "Notification has been sent to the provided contact phone number";
	private static final String EMAIL_SUCCESS = "Notification has been sent to the provided email ";
	private static final String SMS_EMAIL_FAILED = "Invalid phone number and email";
	private static final String IS_SMS_NOTIFICATION_SUCCESS = "NotificationService::sendSMSNotification()::isSuccess?::";
	private static final String IS_EMAIL_NOTIFICATION_SUCCESS = "NotificationService::sendEmailNotification()::isSuccess?::";
	private static final String TEMPLATE_CODE = "Template Code";
	private static final String SUCCESS = "success";
	private static final String SEPARATOR = "/";
	
	@SuppressWarnings("rawtypes")
	public NotificationResponseDTO sendNotification(NotificationRequestDto dto, Map identity) throws ResidentServiceCheckedException {
		return sendNotification(dto, null, null, null, identity);
	}
	
	@SuppressWarnings("rawtypes")
	public NotificationResponseDTO sendNotification(NotificationRequestDto dto, List<String> channels, String email, String phone, Map identity) throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), dto.getId(),
				"NotificationService::sendNotification()::entry");
		boolean smsStatus = false;
		boolean emailStatus = false;
		Map demographicIdentity = (identity == null || identity.isEmpty()) ? utility.retrieveIdrepoJson(dto.getId()) : identity;
		Map mapperIdentity = getMapperIdentity();

		Set<String> templateLangauges;
		try {
			templateLangauges = getTemplateLanguages(demographicIdentity, mapperIdentity);
		} catch (ReflectiveOperationException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		
		Map<String, Object> notificationAttributes = utility.getMailingAttributes(dto.getId(), templateLangauges, demographicIdentity, mapperIdentity);
		if (dto.getAdditionalAttributes() != null && dto.getAdditionalAttributes().size() > 0) {
			notificationAttributes.putAll(dto.getAdditionalAttributes());
		}
		RequestType notificationRequestType = getNotificationRequestType(dto);
		TemplateType notificationTemplateType = getNotificationTemplateType(dto);
		String notificationEventId=getNotificationEventId(dto);
		String otp = getOtp(dto);
		if(otp!=null){
			notificationAttributes.put(TemplateVariablesConstants.OTP, otp);
		}
		if(phone!=null){
			notificationAttributes.put(TemplateVariablesConstants.PHONE, phone);
		}
		if(notificationEventId!=null) {
			notificationAttributes.put(TemplateVariablesConstants.EVENT_ID, notificationEventId);
		}
		if(channels == null || channels.isEmpty() || channels.stream().collect(Collectors.joining(",")).isEmpty() || channels.stream().collect(Collectors.joining(",")).equals("null")) {
			if (notificationType.equalsIgnoreCase("SMS|EMAIL")) {
				smsStatus = sendSMSNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, templateLangauges);
				emailStatus = sendEmailNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, null,
						templateLangauges, null);
			} else if (notificationType.equalsIgnoreCase("EMAIL")) {
					emailStatus = sendEmailNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, null,
							templateLangauges, null);
			} else if (notificationType.equalsIgnoreCase("SMS")) {
					smsStatus = sendSMSNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, templateLangauges);
			}
		} else {
			List<String> channelsLowerCase = channels.stream().map(String::toLowerCase).collect(Collectors.toList());
			if (channelsLowerCase.contains(PHONE_CHANNEL) && channelsLowerCase.contains(EMAIL_CHANNEL)) {
				smsStatus = sendSMSNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, templateLangauges);
				emailStatus = sendEmailNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, null,
						templateLangauges, null);
			} else if (channelsLowerCase.contains(PHONE_CHANNEL)) {
				smsStatus = sendSMSNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, templateLangauges);
			} else if (channelsLowerCase.contains(EMAIL_CHANNEL)) {
				emailStatus = sendEmailNotification(notificationAttributes, dto.getTemplateTypeCode(), notificationRequestType, notificationTemplateType, null,
						templateLangauges, email);
			}
		}

		logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), dto.getId(),
				IS_SMS_NOTIFICATION_SUCCESS + smsStatus);
		logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), dto.getId(),
				IS_EMAIL_NOTIFICATION_SUCCESS + emailStatus);
		NotificationResponseDTO notificationResponse = new NotificationResponseDTO();
		if (smsStatus && emailStatus) {
			notificationResponse.setMessage(SMS_EMAIL_SUCCESS);
			if(email != null && phone != null) {
				notificationResponse.setMaskedPhone(utility.maskPhone(phone));
				notificationResponse.setMaskedEmail(utility.maskEmail(email));
			}
			notificationResponse.setStatus(SUCCESS);
		} else if (smsStatus) {	
			notificationResponse.setMessage(SMS_SUCCESS);
			if(phone != null) {
				notificationResponse.setMaskedPhone(utility.maskPhone(phone));
			} 
		} else if (emailStatus) {
			notificationResponse.setMessage(EMAIL_SUCCESS);
			if(email != null) {
				notificationResponse.setMaskedEmail(utility.maskEmail(email));
			}
		} else {
			notificationResponse.setMessage(SMS_EMAIL_FAILED);
			throw new ResidentServiceException(ResidentErrorCode.NOTIFICATION_FAILURE.getErrorCode(),
					ResidentErrorCode.NOTIFICATION_FAILURE.getErrorMessage() + " " + SMS_EMAIL_FAILED);
		}

		logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), dto.getId(),
				"NotificationService::sendSMSNotification()::isSuccess?::" + notificationResponse.getMessage());
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), dto.getId(),
				"NotificationService::sendNotification()::exit");
		return notificationResponse;
	}

	@SuppressWarnings("rawtypes")
	private Map getMapperIdentity() throws ResidentServiceCheckedException {
		JSONObject mappingJsonObject = utility.getMappingJsonObject();
		Map mapperIdentity = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
		return mapperIdentity;
	}

	@SuppressWarnings("rawtypes")
	private Set<String> getTemplateLanguages(Map demographicIdentity, Map mapperIdentity) throws ReflectiveOperationException {
		Set<String> preferredLanguage = utility.getPreferredLanguage(demographicIdentity);
		Set<String> templateLangauges = new HashSet<>();
		if (preferredLanguage.isEmpty()) {
			List<String> defaultTemplateLanguages = utility.getDefaultTemplateLanguages();
			if (CollectionUtils.isEmpty(defaultTemplateLanguages)) {
				Set<String> dataCapturedLanguages = utility.getDataCapturedLanguages(mapperIdentity, demographicIdentity);
				templateLangauges.addAll(dataCapturedLanguages);
			} else {
				templateLangauges.addAll(defaultTemplateLanguages);
			}
		} else {
			templateLangauges.addAll(preferredLanguage);
		}
		return templateLangauges;
	}

	private String getOtp(NotificationRequestDto notificationRequestDto) {
		return notificationRequestDto instanceof NotificationRequestDtoV2?((NotificationRequestDtoV2) notificationRequestDto).getOtp():null;
	}

	private RequestType getNotificationRequestType(NotificationRequestDto notificationRequestDto) {
		return notificationRequestDto instanceof NotificationRequestDtoV2?((NotificationRequestDtoV2) notificationRequestDto).getRequestType():null;
	}

	private TemplateType getNotificationTemplateType(NotificationRequestDto notificationRequestDto) {
		return notificationRequestDto instanceof NotificationRequestDtoV2?((NotificationRequestDtoV2) notificationRequestDto).getTemplateType():null;
	}

	private String getNotificationEventId(NotificationRequestDto notificationRequestDto) {
		return notificationRequestDto instanceof NotificationRequestDtoV2?((NotificationRequestDtoV2) notificationRequestDto).getEventId():null;
	}

	@SuppressWarnings("unchecked")
	private String getTemplate(String langCode, String templateTypeCode) {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), TEMPLATE_CODE, templateTypeCode,
				"NotificationService::getTemplate()::entry");
		return templateUtil.getTemplateValueFromTemplateTypeCodeAndLangCode(langCode, templateTypeCode);
	}

	private String templateMerge(String fileText, Map<String, Object> mailingAttributes)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), "",
				"NotificationService::templateMerge()::entry");
		try {
			String mergeTemplate;
			InputStream templateInputStream = new ByteArrayInputStream(fileText.getBytes(Charset.forName("UTF-8")));

			InputStream resultedTemplate = templateManager.merge(templateInputStream, mailingAttributes);

			mergeTemplate = IOUtils.toString(resultedTemplate, StandardCharsets.UTF_8.name());
			logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), "",
					"NotificationService::templateMerge()::exit");
			return mergeTemplate;
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}

	private boolean sendSMSNotification(Map<String, Object> mailingAttributes,
			NotificationTemplateCode notificationTemplate, RequestType requestType, TemplateType templateType, Set<String> templateLangauges)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
				"NotificationService::sendSMSNotification()::entry");
		String eventId=(String) mailingAttributes.get(TemplateVariablesConstants.EVENT_ID);
		String phone="";
		if(mailingAttributes.get(TemplateVariablesConstants.PHONE)== null){
			phone = (String) mailingAttributes.get(utilities.getPhoneAttribute());
		} else{
			phone =  (String) mailingAttributes.get(TemplateVariablesConstants.PHONE);
		}

		if (nullValueCheck(phone) || !(requestValidator.phoneValidator(phone))) {
			logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
					"NotificationService::sendSMSNotification()::phoneValidatio::" + "false :: invalid phone number");
			return false;
		}
		String mergedTemplate = "";
		for (String language : templateLangauges) {
			String languageTemplate = "";
			if(notificationTemplate==null) {
				if(mailingAttributes.get(TemplateVariablesConstants.PHONE)== null){
					languageTemplate = templateMerge(getTemplate(language, templateUtil.getSmsTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language), mailingAttributes));
				} else{
					languageTemplate = templateMerge(getTemplate(language, templateUtil.getSmsTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language, (String) mailingAttributes.get(TemplateVariablesConstants.OTP)), mailingAttributes));
				}

			} else {
				languageTemplate = templateMerge(getTemplate(language, notificationTemplate + SMS),
						mailingAttributes);
			}
			if(languageTemplate.trim().endsWith(LINE_BREAK)) {
				languageTemplate = languageTemplate.substring(0, languageTemplate.length() - LINE_BREAK.length()).trim();
			}
			if (mergedTemplate.isBlank()) {
				mergedTemplate = languageTemplate;
			}else {
				mergedTemplate = mergedTemplate + LINE_SEPARATOR
						+ languageTemplate;
			}
		}
		SMSRequestDTO smsRequestDTO = new SMSRequestDTO();
		smsRequestDTO.setMessage(mergedTemplate);
		smsRequestDTO.setNumber(phone);
		RequestWrapper<SMSRequestDTO> req = new RequestWrapper<>();
		req.setRequest(smsRequestDTO);
		ResponseWrapper<NotificationResponseDTO> resp;
		try {
			resp = restClient.postApi(env.getProperty(ApiName.SMSNOTIFIER.name()), MediaType.APPLICATION_JSON, req,
					ResponseWrapper.class);
			if (nullCheckForResponse(resp)) {
				throw new ResidentServiceException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage() + " SMSNOTIFIER API"
								+ (resp != null ? resp.getErrors().get(0) : ""));
			}
			NotificationResponseDTO notifierResponse = JsonUtil
					.readValue(JsonUtil.writeValueAsString(resp.getResponse()), NotificationResponseDTO.class);
			logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
					"NotificationService::sendSMSNotification()::response::"
							+ JsonUtil.writeValueAsString(notifierResponse));

			if (SUCCESS.equalsIgnoreCase(notifierResponse.getStatus())) {
				logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
						"NotificationService::sendSMSNotification()::exit");
				return true;
			}
		} catch (ApisResourceAccessException e) {

			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
						e.getMessage() + httpClientException.getResponseBodyAsString());
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpClientException.getResponseBodyAsString());

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
						e.getMessage() + httpServerException.getResponseBodyAsString());
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpServerException.getResponseBodyAsString());
			} else {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
						e.getMessage() + ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
			}

		} catch (IOException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
					e.getMessage() + ExceptionUtils.getStackTrace(e));
			audit.setAuditRequestDto(EventEnum.TOKEN_GENERATION_FAILED);
			throw new ResidentServiceCheckedException(ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorCode(),
					ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorMessage(), e);
		}
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
				"NotificationService::sendSMSNotification()::exit");

		return false;

	}

	private boolean sendEmailNotification(Map<String, Object> mailingAttributes,
			NotificationTemplateCode notificationTemplate, RequestType requestType, TemplateType templateType, MultipartFile[] attachment, Set<String> templateLangauges, String newEmail)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
				"NotificationService::sendEmailNotification()::entry");
		String eventId=(String) mailingAttributes.get(TemplateVariablesConstants.EVENT_ID);
		String email = String.valueOf(mailingAttributes.get(utilities.getEmailAttribute()));
		String otp="";
		if(newEmail!=null){
			otp=(String) mailingAttributes.get(TemplateVariablesConstants.OTP);
		}
		if (nullValueCheck(email) || !(requestValidator.emailValidator(email))) {
			logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
					"NotificationService::sendEmailNotification()::emailValidation::" + "false :: invalid email");
			return false;
		}
		String mergedEmailSubject = "";
		String mergedTemplate = "";
		for (String language : templateLangauges) {
			String emailSubject = "";
			String languageTemplate = "";
			if(notificationTemplate==null) {
				if(newEmail==null) {
					emailSubject = templateMerge(getTemplate(language, templateUtil.getEmailSubjectTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language), mailingAttributes));

					languageTemplate = templateMerge(getTemplate(language, templateUtil.getEmailContentTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language), mailingAttributes));
				}
				else {
					emailSubject = templateMerge(getTemplate(language, templateUtil.getEmailSubjectTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language, otp), mailingAttributes));

					languageTemplate = templateMerge(getTemplate(language, templateUtil.getEmailContentTemplateTypeCode(requestType, templateType)),
							requestType.getNotificationTemplateVariables(templateUtil, new NotificationTemplateVariableDTO(eventId, requestType, templateType, language, otp), mailingAttributes));
				}
			} else {
				emailSubject = getTemplate(language, notificationTemplate + EMAIL + SUBJECT);
				languageTemplate = templateMerge(getTemplate(language, notificationTemplate + EMAIL),
						mailingAttributes);
			}
			if(languageTemplate.trim().endsWith(LINE_BREAK)) {
				languageTemplate = languageTemplate.substring(0, languageTemplate.length() - LINE_BREAK.length()).trim();
			}
			if (mergedTemplate.isBlank() || mergedEmailSubject.isBlank()) {
				mergedTemplate = languageTemplate;
				mergedEmailSubject = emailSubject;
			} else {
				mergedTemplate = mergedTemplate + LINE_SEPARATOR + languageTemplate;
				mergedEmailSubject = mergedEmailSubject + SEPARATOR + emailSubject;
			}
		}
		LinkedMultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		String[] mailTo = new String[1];
		if(newEmail==null){
			mailTo[0] = String.valueOf(mailingAttributes.get(utilities.getEmailAttribute()));
		} else{
			mailTo[0] = newEmail;
		}

		String[] mailCc = notificationEmails.split("\\|");

		for (String item : mailTo) {
			params.add("mailTo", item);
		}

		if (mailCc != null) {
			for (String item : mailCc) {
				params.add("mailCc", item);
			}
		}

		try {
			params.add("mailSubject", mergedEmailSubject);
			params.add("mailContent", mergedTemplate);
			params.add("attachments", attachment);
			ResponseWrapper<NotificationResponseDTO> response;

			response = restClient.postApi(env.getProperty(ApiName.EMAILNOTIFIER.name()), MediaType.MULTIPART_FORM_DATA, params,
					ResponseWrapper.class);
			if (nullCheckForResponse(response)) {
				throw new ResidentServiceException(ResidentErrorCode.INVALID_API_RESPONSE.getErrorCode(),
						ResidentErrorCode.INVALID_API_RESPONSE.getErrorMessage() + " EMAILNOTIFIER API"
								+ (response != null ? response.getErrors().get(0) : ""));
			}
			NotificationResponseDTO notifierResponse = JsonUtil
					.readValue(JsonUtil.writeValueAsString(response.getResponse()), NotificationResponseDTO.class);
			logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
					"NotificationService::sendEmailNotification()::response::"
							+ JsonUtil.writeValueAsString(notifierResponse));

			if ("success".equals(notifierResponse.getStatus())) {
				logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
						"NotificationService::sendEmailNotification()::exit");
				return true;
			}
		} catch (ApisResourceAccessException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpClientException.getResponseBodyAsString());

			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						httpServerException.getResponseBodyAsString());
			} else {
				throw new ResidentServiceCheckedException(
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
			}

		} catch (IOException e) {
			audit.setAuditRequestDto(EventEnum.TOKEN_GENERATION_FAILED);
			throw new ResidentServiceCheckedException(ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorCode(),
					ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorMessage(), e);
		}
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), " ",
				"NotificationService::sendEmailNotification()::exit");
		return false;

	}

	public boolean nullValueCheck(String value) {
		if (value == null || value.isEmpty())
			return true;
		return false;
	}

	public boolean nullCheckForResponse(ResponseWrapper<NotificationResponseDTO> response) {
		if (response == null || response.getResponse() == null
				|| response.getErrors() != null && !response.getErrors().isEmpty())
			return true;
		return false;

	}

}
