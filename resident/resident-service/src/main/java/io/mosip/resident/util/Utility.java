package io.mosip.resident.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.IOUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.pdfgenerator.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.RegistrationConstants;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.DynamicFieldCodeValueDTO;
import io.mosip.resident.dto.DynamicFieldConsolidateResponseDto;
import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.JsonValue;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.mosip.resident.constant.MappingJsonConstants.EMAIL;
import static io.mosip.resident.constant.MappingJsonConstants.PHONE;
import static io.mosip.resident.constant.RegistrationConstants.DATETIME_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.IDENTITY;
import static io.mosip.resident.constant.ResidentConstants.LANGUAGE;
import static io.mosip.resident.constant.ResidentConstants.VALUE;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Girish Yarru
 * @version 1.0
 */

@Component
public class Utility {

	private static final String MEDIUM = "MEDIUM";
	private static final String EVENT_ID_PLACEHOLDER = "{eventId}";
	private static final String MAPPING_ATTRIBUTE_SEPARATOR = ",";
	private static final String ATTRIBUTE_VALUE_SEPARATOR = " ";
	private static final Logger logger = LoggerConfiguration.logConfig(Utility.class);
	private static final String DIGITAL_CARD_PARTNER = "digitalcardPartner";
	private static final String APP_ID_BASED_CREDENTIAL_ID_SUFFIX = "appIdBasedCredentialIdSuffix";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	@Value("${registration.processor.identityjson}")
	private String residentIdentityJson;

	@Value("${" + ResidentConstants.PREFERRED_LANG_PROPERTY + ":false}")
	private boolean isPreferedLangFlagEnabled;


	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Autowired
	private Environment env;

	@Autowired
	private PDFGenerator pdfGenerator;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Utilities utilities;

	@Autowired
	private ObjectStoreHelper objectStoreHelper;

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

	private static String regProcessorIdentityJson = "";

	private static String ANONYMOUS_USER = "anonymousUser";

	private String ridDelimeterValue;

	@Autowired(required = true)
	@Qualifier("varres")
	private VariableResolverFactory functionFactory;

	@Value("${resident.ui.track-service-request-url}")
	private String trackServiceUrl;

	@Value("${mosip.resident.download-card.url}")
	private String downloadCardUrl;

	@Value("${resident.date.time.replace.special.chars:{}}")
	private String specialCharsReplacement;

	@Autowired
	private ObjectMapper mapper;

	@Value("${resident.date.time.formmatting.style:" + MEDIUM + "}")
	private String formattingStyle;

	private Map<String, String> specialCharsReplacementMap;

	private JSONObject mappingJsonObject;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	@Autowired
	private SessionUserNameUtility sessionUserNameUtility;

	@Autowired
	private ProxyMasterDataServiceUtility proxyMasterDataServiceUtility;

	@PostConstruct
	private void loadRegProcessorIdentityJson() {
		regProcessorIdentityJson = residentRestTemplate.getForObject(configServerFileStorageURL + residentIdentityJson, String.class);
		logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(), "loadRegProcessorIdentityJson completed successfully");
		try {
			specialCharsReplacementMap = ((Map<String, Object>) mapper.readValue(specialCharsReplacement, Map.class))
					.entrySet()
					.stream()
					.collect(Collectors.toUnmodifiableMap(Entry::getKey, entry -> String.valueOf(entry.getValue())));
		} catch (JsonProcessingException e) {
			logger.error("Error parsing special chars map used for replacement in timestamp in filename.");
			specialCharsReplacementMap = Map.of();
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject retrieveIdrepoJson(String id) throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
				"Utility::retrieveIdrepoJson()::entry");
		List<String> pathsegments = new ArrayList<>();
		pathsegments.add(id);
		ResponseWrapper<IdRepoResponseDto> response = null;
		try {
			response = (ResponseWrapper<IdRepoResponseDto>) residentServiceRestClient.getApi(
					ApiName.IDREPOGETIDBYUIN, pathsegments, "", null, ResponseWrapper.class);

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
		}

		return retrieveErrorCode(response, id);
	}

	public JSONObject retrieveErrorCode(ResponseWrapper<IdRepoResponseDto> response, String id)
			throws ResidentServiceCheckedException {
		ResidentErrorCode errorCode;
		errorCode = ResidentErrorCode.INVALID_ID;
		try {
			if (response == null)
				throw new IdRepoAppException(errorCode.getErrorCode(), errorCode.getErrorMessage(),
						"In valid response while requesting ID Repositary");
			if (!response.getErrors().isEmpty()) {
				List<ServiceError> error = response.getErrors();
				throw new IdRepoAppException(errorCode.getErrorCode(), errorCode.getErrorMessage(),
						error.get(0).getMessage());
			}

			String jsonResponse = JsonUtil.writeValueAsString(response.getResponse());
			JSONObject json = JsonUtil.readValue(jsonResponse, JSONObject.class);
			logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
					"Utility::retrieveIdrepoJson()::exit");
			return JsonUtil.getJSONObject(json, "identity");
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	public Map<String, Object> getMailingAttributes(String id, Set<String> templateLangauges, Map demographicIdentity, Map mapperIdentity)
			throws ResidentServiceCheckedException {
		logger.debug("Utility::getMailingAttributes()::entry");
		if (id == null || id.isEmpty()) {
			throw new ResidentServiceException(ResidentErrorCode.UNABLE_TO_PROCESS.getErrorCode(),
					ResidentErrorCode.UNABLE_TO_PROCESS.getErrorMessage() + ": individual_id is not available.");
		}

		return getMailingAttributesFromIdentity(templateLangauges, demographicIdentity, mapperIdentity);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public Map<String, Object> getMailingAttributesFromIdentity(Set<String> templateLangauges, Map demographicIdentity, Map mapperIdentity)
			throws ResidentServiceCheckedException {
		Map<String, Object> attributes = new HashMap<>();

		Set<String> mapperJsonKeys = mapperIdentity.keySet();

		for (String key : mapperJsonKeys) {
			Object mapperValueObj = mapperIdentity.get(key);
			if (mapperValueObj instanceof Map) {
				Map<String, String> mapperValueMap = (Map<String, String>) mapperValueObj;
				String mappingValueStr = mapperValueMap.get(VALUE);
				for (String mappingValue : mappingValueStr.split(",")) {
					Object identityNodeObj = demographicIdentity.get(mappingValue);
					if (identityNodeObj instanceof ArrayList) {
						List identityValueList = (List) identityNodeObj;
						for (Object identityValue : identityValueList) {
							if(identityValue instanceof String){
								continue;
							}
							JsonValue jsonValue = mapper.convertValue(identityValue, JsonValue.class);
							if (templateLangauges.contains(jsonValue.getLanguage())) {
								attributes.put(mappingValue + "_" + jsonValue.getLanguage(), jsonValue.getValue());
							}
						}
					} else if (identityNodeObj instanceof LinkedHashMap) {
						Map json = (Map) identityNodeObj;
						attributes.put(mappingValue, (String) json.get(VALUE));
					} else {
						attributes.put(mappingValue, identityNodeObj == null ? null : String.valueOf(identityNodeObj));
					}
				}
			}
		}
		logger.debug("Utility::getMailingAttributes()::exit");
		return attributes;
	}

	@Cacheable(value = "getPreferredLanguage", key = "#demographicIdentity")
	public Set<String> getPreferredLanguage(Map demographicIdentity) {
		String preferredLang = null;
		String preferredLangAttribute = env.getProperty("mosip.default.user-preferred-language-attribute");
		if (!StringUtils.isBlank(preferredLangAttribute)) {
			Object object = demographicIdentity.get(preferredLangAttribute);
			if (object != null) {
				preferredLang = String.valueOf(object);
				if (preferredLang.contains(ResidentConstants.COMMA)) {
					String[] preferredLangArray = preferredLang.split(ResidentConstants.COMMA);
					return Stream.of(preferredLangArray)
							.map(lang -> getPreferredLanguageCodeForLanguageNameBasedOnFlag(preferredLangAttribute, lang))
							.collect(Collectors.toSet());
				}
			}
		}
		if (preferredLang != null) {
			String preferredLanguage = getPreferredLanguageCodeForLanguageNameBasedOnFlag(preferredLangAttribute, preferredLang);
			if (preferredLanguage == null || preferredLanguage.isEmpty()) {
				return Set.of();
			}
			return Set.of(preferredLanguage);
		}
		return Set.of();
	}

	public String getPreferredLanguageCodeForLanguageNameBasedOnFlag(String fieldName, String preferredLang) {
		if (isPreferedLangFlagEnabled) {
			try {
				ResponseWrapper<?> responseWrapper = (ResponseWrapper<DynamicFieldConsolidateResponseDto>)
						proxyMasterDataServiceUtility.getDynamicFieldBasedOnLangCodeAndFieldName(fieldName,
								env.getProperty(ResidentConstants.MANDATORY_LANGUAGE), true);
				DynamicFieldConsolidateResponseDto dynamicFieldConsolidateResponseDto = mapper.readValue(
						mapper.writeValueAsString(responseWrapper.getResponse()),
						DynamicFieldConsolidateResponseDto.class);
				return dynamicFieldConsolidateResponseDto.getValues()
						.stream()
						.filter(dynamicFieldCodeValueDTO -> preferredLang.equalsIgnoreCase(dynamicFieldCodeValueDTO.getValue()))
						.findAny()
						.map(DynamicFieldCodeValueDTO::getCode)
						.orElse(null);
			} catch (ResidentServiceCheckedException e) {
				throw new RuntimeException(e);
			} catch (JsonMappingException e) {
				throw new RuntimeException(e);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		return preferredLang;

	}

	public Set<String> getDataCapturedLanguages(Map mapperIdentity, Map demographicIdentity)
			throws ReflectiveOperationException {
		Set<String> dataCapturedLangauges = new HashSet<String>();
		Object nameValue = mapperIdentity.get(MappingJsonConstants.NAME);
		if (nameValue instanceof Map) {
			Map<String, Object> jsonObject = (Map<String, Object>) nameValue;
			String values = String.valueOf(jsonObject.get(VALUE));
			for (String value : values.split(",")) {
				Object object = demographicIdentity.get(value);
				if (object instanceof List) {
					List nodes = (List) object;
					for (Object jsonValueObj : nodes) {
						JsonValue jsonValue = mapper.convertValue(jsonValueObj, JsonValue.class);
						dataCapturedLangauges.add(jsonValue.getLanguage());
					}
				}
			}
		}
		return dataCapturedLangauges;
	}

	public List<String> getDefaultTemplateLanguages() {
		String defaultLanguages = env.getProperty("mosip.default.template-languages");
		List<String> strList = Collections.emptyList();
		if (defaultLanguages != null && !StringUtils.isBlank(defaultLanguages)) {
			String[] lanaguages = defaultLanguages.split(",");
			if (lanaguages != null && lanaguages.length > 0) {
				strList = Lists.newArrayList(lanaguages);
			}
			return strList;
		}
		return strList;
	}

	public String getMappingJson() {
		if (StringUtils.isBlank(regProcessorIdentityJson)) {
			return residentRestTemplate.getForObject(configServerFileStorageURL + residentIdentityJson, String.class);
		}
		return regProcessorIdentityJson;
	}

	/**
	 * Read resource content.
	 *
	 * @param resFile the res file
	 * @return the string
	 */
	public static String readResourceContent(Resource resFile) {
		try {
			return IOUtils.readInputStreamToString(resFile.getInputStream(), UTF_8);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION, e);
		}
	}

	public String getPassword(List<String> attributeValues) {
		Map<String, List<String>> context = new HashMap<>();
		context.put("attributeValues", attributeValues);
		VariableResolverFactory myVarFactory = new MapVariableResolverFactory(context);
		myVarFactory.setNextFactory(functionFactory);
		String maskingFunctionName = this.env.getProperty(ResidentConstants.CREATE_PASSWORD_METHOD_NAME);
		Serializable serializable = MVEL.compileExpression(maskingFunctionName + "(attributeValues);");
		return MVEL.executeExpression(serializable, context, myVarFactory, String.class);
	}

	public ResidentTransactionEntity createEntity(RequestType requestType) {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setRequestDtimes(DateUtils.getUTCCurrentDateTime());
		residentTransactionEntity.setResponseDtime(DateUtils.getUTCCurrentDateTime());
		residentTransactionEntity.setCrBy(sessionUserNameUtility.getSessionUserName());
		residentTransactionEntity.setCrDtimes(DateUtils.getUTCCurrentDateTime());
		// Initialize with true, so that it is updated as false in later when needed for notification
		if (ServiceType.ASYNC.getRequestTypes().contains(requestType)) {
			residentTransactionEntity.setReadStatus(false);
		} else {
			residentTransactionEntity.setReadStatus(true);
		}
		residentTransactionEntity.setRequestTypeCode(requestType.name());
		return residentTransactionEntity;
	}

	public String createEventId() {
		/* return a random long of 16 length */
		long smallest = 1000_0000_0000_0000L;
		long biggest = 9999_9999_9999_9999L;

		// return a long between smallest and biggest (+1 to include biggest as well with the upper bound)
		long random = utilities.getSecureRandom().longs(smallest, biggest + 1).findFirst().getAsLong();
		return String.valueOf(random);
	}


	public static boolean isSecureSession() {
		return Optional.ofNullable(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication).map(Authentication::getPrincipal).filter(obj -> !obj.equals(ANONYMOUS_USER)).isPresent();
	}

	public String createTrackServiceRequestLink(String eventId) {
		return trackServiceUrl + eventId;
	}

	public String createDownloadCardLinkFromEventId(ResidentTransactionEntity residentTransactionEntity) {
		if (residentTransactionEntity.getReferenceLink() != null
				&& !residentTransactionEntity.getReferenceLink().isEmpty()) {
			return downloadCardUrl.replace(EVENT_ID_PLACEHOLDER, residentTransactionEntity.getEventId());
		}
		return ResidentConstants.NOT_AVAILABLE;
	}

	public String getPDFHeaderLogo() {
		return env.getProperty(ResidentConstants.MOSIP_PDF_HEADER_LOGO_URL);
	}

	public byte[] signPdf(InputStream in, String password) {
		logger.debug("UinCardGeneratorImpl::generateUinCard()::entry");
		byte[] pdfSignatured = null;
		try {
			ByteArrayOutputStream pdfValue = (ByteArrayOutputStream) pdfGenerator.generate(in);
			PDFSignatureRequestDto request = new PDFSignatureRequestDto(
					Integer.parseInt(Objects.requireNonNull(env.getProperty(ResidentConstants.LOWER_LEFT_X))),
					Integer.parseInt(Objects.requireNonNull(env.getProperty(ResidentConstants.LOWER_LEFT_Y))),
					Integer.parseInt(Objects.requireNonNull(env.getProperty(ResidentConstants.UPPER_RIGHT_X))),
					Integer.parseInt(Objects.requireNonNull(env.getProperty(ResidentConstants.UPPER_RIGHT_Y))),
					env.getProperty(ResidentConstants.REASON), utilities.getTotalNumberOfPageInPdf(pdfValue), password);
			request.setApplicationId(env.getProperty(ResidentConstants.SIGN_PDF_APPLICATION_ID));
			request.setReferenceId(env.getProperty(ResidentConstants.SIGN_PDF_REFERENCE_ID));
			request.setData(org.apache.commons.codec.binary.Base64.encodeBase64String(pdfValue.toByteArray()));
			DateTimeFormatter format = DateTimeFormatter.ofPattern(Objects.requireNonNull(env.getProperty(DATETIME_PATTERN)));
			LocalDateTime localdatetime = LocalDateTime
					.parse(DateUtils.getUTCCurrentDateTimeString(Objects.requireNonNull(env.getProperty(DATETIME_PATTERN))), format);

			request.setTimeStamp(DateUtils.getUTCCurrentDateTimeString());
			RequestWrapper<PDFSignatureRequestDto> requestWrapper = new RequestWrapper<>();

			requestWrapper.setRequest(request);
			requestWrapper.setRequesttime(localdatetime);
			ResponseWrapper<?> responseWrapper;
			SignatureResponseDto signatureResponseDto;

			responseWrapper = residentServiceRestClient.postApi(env.getProperty(ApiName.PDFSIGN.name())
					, MediaType.APPLICATION_JSON, requestWrapper, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ServiceError error = responseWrapper.getErrors().get(0);
				throw new ResidentServiceException(ResidentErrorCode.valueOf(error.getMessage()));
			}
			String signatureData = objectMapper.writeValueAsString(responseWrapper.getResponse());
			signatureResponseDto = objectMapper.readValue(signatureData,
					SignatureResponseDto.class);

			pdfSignatured = Base64.decodeBase64(signatureResponseDto.getData());

		} catch (Exception e) {
			logger.error(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorMessage(), e.getMessage()
					+ ExceptionUtils.getStackTrace(e));
		}
		logger.debug("UinCardGeneratorImpl::generateUinCard()::exit");

		return pdfSignatured;
	}

	public String getFileName(String eventId, String propertyName, int timeZoneOffset, String locale) {
		if (eventId != null && propertyName.contains("{" + TemplateVariablesConstants.EVENT_ID + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.EVENT_ID + "}", eventId);
		}
		if (propertyName.contains("{" + TemplateVariablesConstants.TIMESTAMP + "}")) {
			String dateTimeFormat = formatWithOffsetForFileName(timeZoneOffset, locale, DateUtils.getUTCCurrentDateTime());
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.TIMESTAMP + "}", dateTimeFormat);
		}
		return propertyName;
	}

	public String getFileNameForId(String id, String propertyName, int timeZoneOffset, String locale) {
		if (id != null && propertyName.contains("{" + TemplateVariablesConstants.ID + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.ID + "}", id);
		}
		if (propertyName.contains("{" + TemplateVariablesConstants.TIMESTAMP + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.TIMESTAMP + "}", formatWithOffsetForFileName(timeZoneOffset, locale, DateUtils.getUTCCurrentDateTime()));
		}
		return propertyName;
	}

	private String replaceSpecialChars(String fileName) {
		if (!specialCharsReplacementMap.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder(fileName);
			specialCharsReplacementMap.entrySet().forEach(entry -> {
				String key = entry.getKey();
				String value = entry.getValue();
				int index;
				while ((index = stringBuilder.indexOf(key)) != -1) {
					stringBuilder.replace(index, index + key.length(), value);
				}
			});
			return stringBuilder.toString();
		}
		return fileName;
	}

	public String getIdForResidentTransaction(List<String> channel, IdentityDTO identityDTO, String idaToken) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		String email = "";
		String phone = "";
		if (identityDTO != null) {
			email = identityDTO.getEmail();
			phone = identityDTO.getPhone();
		}
		String id;
		if (email != null && phone != null && channel.size() == 2) {
			id = email + phone + idaToken;
		} else if (email != null && channel.size() == 1 && channel.get(0).equalsIgnoreCase(EMAIL)) {
			id = email + idaToken;
		} else if (phone != null && channel.size() == 1 && channel.get(0).equalsIgnoreCase(PHONE)) {
			id = phone + idaToken;
		} else {
			throw new ResidentServiceCheckedException(ResidentErrorCode.NO_CHANNEL_IN_IDENTITY);
		}
		return getRefIdHash(id);
	}

	public String getFileNameAck(String featureName, String eventId, String propertyName, int timeZoneOffset, String locale) {
		if (eventId != null && propertyName.contains("{" + TemplateVariablesConstants.FEATURE_NAME + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.FEATURE_NAME + "}", featureName);
		}
		if (eventId != null && propertyName.contains("{" + TemplateVariablesConstants.EVENT_ID + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.EVENT_ID + "}", eventId);
		}
		if (propertyName.contains("{" + TemplateVariablesConstants.TIMESTAMP + "}")) {
			propertyName = propertyName.replace("{" + TemplateVariablesConstants.TIMESTAMP + "}",
					formatWithOffsetForFileName(timeZoneOffset, locale, DateUtils.getUTCCurrentDateTime()));
		}
		return propertyName;
	}

	public String getFileNameAsPerFeatureName(String eventId, RequestType requestType, int timeZoneOffset, String locale) {
		String namingProperty = requestType.getNamingProperty();
		if (namingProperty == null) {
			namingProperty = ResidentConstants.ACK_NAMING_CONVENTION_PROPERTY;
		}
		return getFileNameAck(requestType.getName(), eventId, Objects.requireNonNull(this.env.getProperty(namingProperty)),
				timeZoneOffset, locale);
	}

	public String getRefIdHash(String value) throws NoSuchAlgorithmException {
		return HMACUtils2.digestAsPlainText(value.getBytes());
	}

	private String formatDateTimeForPattern(LocalDateTime localDateTime, String locale, String defaultDateTimePattern, int timeZoneOffset) {
		return localDateTime == null ? null : formatToLocaleDateTime(locale, defaultDateTimePattern, localDateTime);
	}

	public String formatWithOffsetForUI(int timeZoneOffset, String locale, LocalDateTime localDateTime) {
		return formatDateTimeForPattern(applyTimeZoneOffsetOnDateTime(timeZoneOffset, localDateTime), locale, Objects.requireNonNull(env.getProperty(ResidentConstants.UI_DATE_TIME_PATTERN_DEFAULT)), timeZoneOffset);
	}

	public String formatWithOffsetForFileName(int timeZoneOffset, String locale, LocalDateTime localDateTime) {
		return replaceSpecialChars(formatDateTimeForPattern(applyTimeZoneOffsetOnDateTime(timeZoneOffset, localDateTime), locale, Objects.requireNonNull(env.getProperty(ResidentConstants.FILENAME_DATETIME_PATTERN_DEFAULT)), timeZoneOffset));
	}

	public LocalDateTime applyTimeZoneOffsetOnDateTime(int timeZoneOffset, LocalDateTime localDateTime) {
		return localDateTime == null ? null : localDateTime.minusMinutes(timeZoneOffset); //Converting UTC to local time zone
	}

	private String formatToLocaleDateTime(String localeStr, String defaultDateTimePattern, LocalDateTime localDateTime) {
		Locale locale = null;
		if (localeStr != null && !localeStr.isEmpty()) {
			String[] localeElements = localeStr.replace('-', '_').split("_");
			if (localeElements.length == 1) {
				locale = new Locale.Builder().setLanguage(localeElements[0].toLowerCase()).build();
			} else if (localeElements.length >= 2) {
				locale = new Locale.Builder().setLanguage(localeElements[0]).setRegion(localeElements[1].toUpperCase())
						.build();
			}
		}

		if (locale == null && defaultDateTimePattern == null) {
			locale = Locale.getDefault();
		}

		if (locale != null) {
			Chronology chronology = Chronology.ofLocale(locale);
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.valueOf(formattingStyle)).withLocale(locale).withChronology(chronology);
			String dateTime = localDateTime.format(formatter);
			return dateTime;
		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(defaultDateTimePattern);
			String dateTime = localDateTime.format(formatter);
			return dateTime;
		}
	}

	public String getClientIp(HttpServletRequest req) {
		logger.debug("Utilitiy::getClientIp()::entry");
		String[] IP_HEADERS = {
				ResidentConstants.X_FORWARDED_FOR,
				ResidentConstants.X_REAL_IP,
				ResidentConstants.PROXY_CLIENT_IP,
				ResidentConstants.WL_PROXY_CLIENT_IP,
				ResidentConstants.HTTP_X_FORWARDED_FOR,
				ResidentConstants.HTTP_X_FORWARDED,
				ResidentConstants.HTTP_X_CLUSTER_CLIENT_IP,
				ResidentConstants.HTTP_CLIENT_IP,
				ResidentConstants.HTTP_FORWARDED_FOR,
				ResidentConstants.HTTP_FORWARDED,
				ResidentConstants.HTTP_VIA,
				ResidentConstants.REMOTE_ADDR
		};
		for (String header : IP_HEADERS) {
			String value = req.getHeader(header);
			if (value == null || value.isEmpty()) {
				continue;
			}
			String[] parts = value.split(",");
			logger.debug("Utilitiy::getClientIp()::exit");
			return parts[0].trim();
		}
		logger.debug("Utilitiy::getClientIp()::exit - excecuted till end");
		return req.getRemoteAddr();
	}

	public String getCardOrderTrackingId(String transactionId, String individualId)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		Object object = residentServiceRestClient.getApi(ApiName.GET_ORDER_STATUS_URL, RequestType.getAllNewOrInprogressStatusList(env),
				List.of(TemplateVariablesConstants.TRANSACTION_ID, TemplateVariablesConstants.INDIVIDUAL_ID),
				List.of(transactionId, individualId), ResponseWrapper.class);
		ResponseWrapper<Map<String, String>> responseWrapper = JsonUtil.convertValue(object,
				new TypeReference<ResponseWrapper<Map<String, String>>>() {
				});
		if (Objects.nonNull(responseWrapper.getErrors()) && !responseWrapper.getErrors().isEmpty()) {
			logger.error("ORDER_STATUS_URL returned error " + responseWrapper.getErrors());
			throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
		}
		return responseWrapper.getResponse().get(TemplateVariablesConstants.TRACKING_ID);
	}

	@CacheEvict(value = "userInfoCache", key = "#token")
	public void clearUserInfoCache(String token) {
		logger.info("Clearing User Info cache");
	}

	public JSONObject getMappingJsonObject() throws ResidentServiceCheckedException {
		if (mappingJsonObject != null) {
			return mappingJsonObject;
		}

		String mappingJsonString = getMappingJson();
		if (mappingJsonString == null || mappingJsonString.trim().isEmpty()) {
			throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
		}
		try {
			mappingJsonObject = JsonUtil.readValue(mappingJsonString, JSONObject.class);
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		return mappingJsonObject;
	}

	public String getMappingValue(Map<?, ?> identity, String mappingName)
			throws ResidentServiceCheckedException, IOException {
		return getMappingValue(identity, mappingName, null);
	}

	public String getMappingValue(Map<?, ?> identity, String mappingName, String langCode)
			throws ResidentServiceCheckedException, IOException {
		String mappingJson = getMappingJson();
		if (mappingJson == null || mappingJson.trim().isEmpty()) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
		}
		JSONObject mappingJsonObject = JsonUtil.readValue(mappingJson, JSONObject.class);
		JSONObject identityMappingJsonObject = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
		String mappingAttributes = getMappingAttribute(identityMappingJsonObject, mappingName);

		return Stream.of(mappingAttributes.split(MAPPING_ATTRIBUTE_SEPARATOR))
				.map(mappingAttribute -> {
					Object value = identity.get(mappingAttribute);
					if (value == null && langCode != null) {
						value = identity.get(mappingAttribute + "_" + langCode);
					}
					return value;
				})
				.map(attributeValue -> {
					if (attributeValue instanceof String) {
						return (String) attributeValue;
					} else if (attributeValue instanceof List) {
						if (langCode == null) {
							return null;
						} else {
							return getValueForLang((List<Map<String, Object>>) attributeValue, langCode);
						}
					} else if (attributeValue instanceof Map) {
						return ((String) ((Map) attributeValue).get(VALUE));
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.joining(ATTRIBUTE_VALUE_SEPARATOR));
	}

	private String getValueForLang(List<Map<String, Object>> attributeValue, String langCode) {
		return attributeValue.stream()
				.filter(map -> map.get(LANGUAGE) instanceof String && ((String) map.get(LANGUAGE)).equalsIgnoreCase(langCode))
				.map(map -> (String) map.get(VALUE))
				.findAny()
				.orElse(null);
	}

	private String getMappingAttribute(JSONObject identityJson, String name) {
		JSONObject docJson = JsonUtil.getJSONObject(identityJson, name);
		if (docJson != null) {
			return JsonUtil.getJSONValue(docJson, VALUE);
		}
		return name;
	}

	@CacheEvict(value = "identityMapCache", key = "#accessToken")
	public void clearIdentityMapCache(String accessToken) {
		logger.info("Clearing Identity Map cache IdResponseDto1");
	}

	@CacheEvict(value = "partnerListCache", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.partnerCache}")
	public void emptyPartnerListCache() {
		logger.info("Emptying Partner list cache");
	}

	@CacheEvict(value = "partnerDetailCache", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.partnerCache}")
	public void emptyPartnerDetailCache() {
		logger.info("Emptying Partner detail cache");
	}

	@CacheEvict(value = "getValidDocumentByLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getValidDocumentByLangCode}")
	public void emptyGetValidDocumentByLangCodeCache() {
		logger.info("Emptying getValidDocumentByLangCode cache");
	}

	@CacheEvict(value = "getLocationHierarchyLevelByLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getLocationHierarchyLevelByLangCode}")
	public void emptyGetLocationHierarchyLevelByLangCodeCache() {
		logger.info("Emptying getLocationHierarchyLevelByLangCode cache");
	}

	@CacheEvict(value = "getImmediateChildrenByLocCodeAndLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getImmediateChildrenByLocCodeAndLangCode}")
	public void emptyGetImmediateChildrenByLocCodeAndLangCodeCache() {
		logger.info("Emptying getImmediateChildrenByLocCodeAndLangCode cache");
	}

	@CacheEvict(value = "getLocationDetailsByLocCodeAndLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getLocationDetailsByLocCodeAndLangCode}")
	public void emptyGetLocationDetailsByLocCodeAndLangCodeCache() {
		logger.info("Emptying getLocationDetailsByLocCodeAndLangCode cache");
	}

	@CacheEvict(value = "getCoordinateSpecificRegistrationCenters", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getCoordinateSpecificRegistrationCenters}")
	public void emptyGetCoordinateSpecificRegistrationCentersCache() {
		logger.info("Emptying getCoordinateSpecificRegistrationCenters cache");
	}

	@CacheEvict(value = "getApplicantValidDocument", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getApplicantValidDocument}")
	public void emptyGetApplicantValidDocumentCache() {
		logger.info("Emptying getApplicantValidDocument cache");
	}

	@CacheEvict(value = "getRegistrationCentersByHierarchyLevel", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getRegistrationCentersByHierarchyLevel}")
	public void emptyGetRegistrationCentersByHierarchyLevelCache() {
		logger.info("Emptying getRegistrationCentersByHierarchyLevel cache");
	}

	@CacheEvict(value = "getRegistrationCenterByHierarchyLevelAndTextPaginated", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getRegistrationCenterByHierarchyLevelAndTextPaginated}")
	public void emptyGetRegistrationCenterByHierarchyLevelAndTextPaginatedCache() {
		logger.info("Emptying getRegistrationCenterByHierarchyLevelAndTextPaginated cache");
	}

	@CacheEvict(value = "getRegistrationCenterWorkingDays", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getRegistrationCenterWorkingDays}")
	public void emptyGetRegistrationCenterWorkingDaysCache() {
		logger.info("Emptying getRegistrationCenterWorkingDays cache");
	}

	@CacheEvict(value = "getLatestIdSchema", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getLatestIdSchema}")
	public void emptyGetLatestIdSchemaCache() {
		logger.info("Emptying getLatestIdSchema cache");
	}

	@CacheEvict(value = "getGenderCodeByGenderTypeAndLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getGenderCodeByGenderTypeAndLangCode}")
	public void emptyGetGenderCodeByGenderTypeAndLangCodeCache() {
		logger.info("Emptying getGenderCodeByGenderTypeAndLangCode cache");
	}

	@CacheEvict(value = "getDocumentTypesByDocumentCategoryAndLangCode", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getDocumentTypesByDocumentCategoryAndLangCode}")
	public void emptyGetDocumentTypesByDocumentCategoryAndLangCodeCache() {
		logger.info("Emptying getDocumentTypesByDocumentCategoryAndLangCode cache");
	}

	@CacheEvict(value = "getDynamicFieldBasedOnLangCodeAndFieldName", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getDynamicFieldBasedOnLangCodeAndFieldName}")
	public void emptyGetDynamicFieldBasedOnLangCodeAndFieldNameCache() {
		logger.info("Emptying getDynamicFieldBasedOnLangCodeAndFieldName cache");
	}

	@Cacheable(value = "getCenterDetails", key = "{#centerId, #langCode}")
	public ResponseWrapper<?> getCenterDetails(String centerId, String langCode) throws ApisResourceAccessException {
		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(centerId);
		pathSegments.add(langCode);
		return (ResponseWrapper<?>) residentServiceRestClient.getApi(ApiName.CENTERDETAILS, pathSegments, "",
				"", ResponseWrapper.class);
	}

	@CacheEvict(value = "getCenterDetails", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getCenterDetails}")
	public void emptyGetCenterDetailsCache() {
		logger.info("Emptying getCenterDetails cache");
	}

	@CacheEvict(value = "getLocationHierarchyLevels", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getLocationHierarchyLevels}")
	public void emptyGetLocationHierarchyLevels() {
		logger.info("Emptying getLocationHierarchyLevels cache");
	}

	@CacheEvict(value = "getAllDynamicFieldByName", allEntries = true)
	@Scheduled(fixedRateString = "${resident.cache.expiry.time.millisec.getAllDynamicFieldByName}")
	public void emptyGetAllDynamicFieldByName() {
		logger.info("Emptying getAllDynamicFieldByName cache");
	}

	public void updateEntity(String statusCode, String requestSummary, boolean readStatus, String statusComment, ResidentTransactionEntity residentTransactionEntity) {
		residentTransactionEntity.setStatusCode(statusCode);
		residentTransactionEntity.setRequestSummary(requestSummary);
		residentTransactionEntity.setReadStatus(readStatus);
		residentTransactionEntity.setStatusComment(statusComment);
		residentTransactionEntity.setUpdBy(ResidentConstants.RESIDENT);
		residentTransactionEntity.setUpdDtimes(DateUtils.getUTCCurrentDateTime());
		saveEntity(residentTransactionEntity);
	}

	public void saveEntity(ResidentTransactionEntity residentTransactionEntity) {
		residentTransactionRepository.save(residentTransactionEntity);
	}

	@PostConstruct
	public String getRidDeliMeterValue() throws ResidentServiceCheckedException {
		if (Objects.isNull(ridDelimeterValue)) {
			try {
				JsonNode policyJson = mapper.readValue(new URL(Objects.requireNonNull(env.getProperty(
						ResidentConstants.REG_PROC_CREDENTIAL_PARTNER_POLICY_URL))), JsonNode.class);
				JsonNode partnersArray = policyJson.get(ResidentConstants.PARTNERS);

				for (JsonNode partner : partnersArray) {
					if (DIGITAL_CARD_PARTNER.equals(partner.get(RegistrationConstants.ID).asText())) {
						ridDelimeterValue = partner.get(APP_ID_BASED_CREDENTIAL_ID_SUFFIX).asText();
						break;
					}
				}

			} catch (IOException e) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
						"getRidDeliMeterValue",
						ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode() + org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceCheckedException(ResidentErrorCode.POLICY_EXCEPTION.getErrorCode(),
						ResidentErrorCode.POLICY_EXCEPTION.getErrorMessage(), e);
			}
		}
		return ridDelimeterValue;
	}
}
