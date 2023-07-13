package io.mosip.resident.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.IOUtils;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.openid.bridge.api.constants.AuthErrorCode;
import io.mosip.kernel.signature.dto.PDFSignatureRequestDto;
import io.mosip.kernel.signature.dto.SignatureResponseDto;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
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
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.util.Lists;
import org.json.simple.JSONArray;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Girish Yarru
 * @version 1.0
 */

@Component
public class Utility {

	private static final String MEDIUM = "MEDIUM";

	private static final String EVENT_ID_PLACEHOLDER = "{eventId}";

	private static final Logger logger = LoggerConfiguration.logConfig(Utility.class);

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

    @Value("${registration.processor.identityjson}")
	private String residentIdentityJson;

	@Value("${"+ResidentConstants.PREFERRED_LANG_PROPERTY+":false}")
	private boolean isPreferedLangFlagEnabled;

	@Value("${mosip.iam.userinfo_endpoint}")
	private String usefInfoEndpointUrl;

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Autowired
	@Qualifier("restClientWithPlainRestTemplate")
	private ResidentServiceRestClient restClientWithPlainRestTemplate;

	@Autowired
	private ValidateTokenUtil tokenValidationHelper;

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

	private static final String IDENTITY = "identity";
	private static final String VALUE = "value";
	private static final String ACR_AMR = "acr_amr";
	private static String regProcessorIdentityJson = "";

	private static String ANONYMOUS_USER = "anonymousUser";

	private static final String AUTHORIZATION = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";
	
	@Autowired(required = true)
	@Qualifier("varres")
	private VariableResolverFactory functionFactory;

	@Value("${resident.email.mask.function}")
	private String emailMaskFunction;
	
	@Value("${resident.phone.mask.function}")
	private String phoneMaskFunction;
	
	@Value("${resident.data.mask.function}")
	private String maskingFunction;
	
	@Value("${resident.ui.track-service-request-url}")
	private String trackServiceUrl;

	@Value("${mosip.resident.download-card.url}")
	private String downloadCardUrl;
	
	@Value("${resident.date.time.replace.special.chars:{}}")
	private String specialCharsReplacement;

	@Autowired
	private IdentityServiceImpl identityService;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	@Autowired
	private ObjectMapper mapper;
	
	/** The acr-amr mapping json file. */
	@Value("${amr-acr.json.filename}")
	private String amrAcrJsonFile;

	@Value("${resident.date.time.formmatting.style:" + MEDIUM + "}")
	private String formattingStyle;

	private Map<String, String> specialCharsReplacementMap;

    @PostConstruct
    private void loadRegProcessorIdentityJson() {
        regProcessorIdentityJson = residentRestTemplate.getForObject(configServerFileStorageURL + residentIdentityJson, String.class);
        logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "loadRegProcessorIdentityJson completed successfully");
        try {
			specialCharsReplacementMap = ((Map<String, Object>)mapper.readValue(specialCharsReplacement, Map.class))
								.entrySet()
								.stream()
								.collect(Collectors.toUnmodifiableMap(Entry::getKey, entry -> String.valueOf(entry.getValue())));
		} catch (JsonProcessingException e) {
			logger.error("Error parsing special chars map used for replacement in timestamp in filename.");
			specialCharsReplacementMap = Map.of();
		}
	}

	@Cacheable(value = "amr-acr-mapping")
	public Map<String, String> getAmrAcrMapping() throws ResidentServiceCheckedException {
		String amrAcrJson = residentRestTemplate.getForObject(configServerFileStorageURL + amrAcrJsonFile,
				String.class);
		Map<String, Object> amrAcrMap = Map.of();
		try {
			if (amrAcrJson != null) {
				amrAcrMap = objectMapper.readValue(amrAcrJson.getBytes(UTF_8), Map.class);
			}
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		Object obj = amrAcrMap.get(ACR_AMR);
		Map<String, Object> map = (Map<String, Object>) obj;
		Map<String, String> acrAmrMap = map.entrySet().stream().collect(
				Collectors.toMap(entry -> entry.getKey(), entry -> (String) ((ArrayList) entry.getValue()).get(0)));
		return acrAmrMap;
	}

	public String getAuthTypeCodefromkey(String reqTypeCode) throws ResidentServiceCheckedException {
		Map<String, String> map = getAmrAcrMapping();
		String authTypeCode = map.get(reqTypeCode);
		return authTypeCode;
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

	@SuppressWarnings("unchecked")
	public Map<String, Object> getMailingAttributes(String id, Set<String> templateLangauges)
			throws ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
				"Utility::getMailingAttributes()::entry");
		if(id == null || id.isEmpty()) {
			throw new ResidentServiceException(ResidentErrorCode.UNABLE_TO_PROCESS.getErrorCode(),
					ResidentErrorCode.UNABLE_TO_PROCESS.getErrorMessage() + ": individual_id is not available." );
		}
		
		Map<String, Object> attributes = new HashMap<>();
		String mappingJsonString = getMappingJson();
		if(mappingJsonString==null || mappingJsonString.trim().isEmpty()) {
			throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
					ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage() );
		}
		JSONObject mappingJsonObject;
		try {
			JSONObject demographicIdentity = retrieveIdrepoJson(id);
			mappingJsonObject = JsonUtil.readValue(mappingJsonString, JSONObject.class);
			JSONObject mapperIdentity = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
			List<String> mapperJsonKeys = new ArrayList<>(mapperIdentity.keySet());

			Set<String> preferredLanguage = getPreferredLanguage(demographicIdentity);
			if (preferredLanguage.isEmpty()) {
				List<String> defaultTemplateLanguages = getDefaultTemplateLanguages();
				if (CollectionUtils.isEmpty(defaultTemplateLanguages)) {
					Set<String> dataCapturedLanguages = getDataCapturedLanguages(mapperIdentity, demographicIdentity);
					templateLangauges.addAll(dataCapturedLanguages);
				} else {
					templateLangauges.addAll(defaultTemplateLanguages);
				}
			} else {
				templateLangauges.addAll(preferredLanguage);
			}

			for (String key : mapperJsonKeys) {
				LinkedHashMap<String, String> jsonObject = JsonUtil.getJSONValue(mapperIdentity, key);
				String values = jsonObject.get(VALUE);
				for (String value : values.split(",")) {
					Object object = demographicIdentity.get(value);
					if (object instanceof ArrayList) {
						JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
						JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
						for (JsonValue jsonValue : jsonValues) {
							if (templateLangauges.contains(jsonValue.getLanguage()))
								attributes.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());
						}
					} else if (object instanceof LinkedHashMap) {
						JSONObject json = JsonUtil.getJSONObject(demographicIdentity, value);
						attributes.put(value, (String) json.get(VALUE));
					} else {
						attributes.put(value, String.valueOf(object));
					}
				}
			}
		} catch (IOException | ReflectiveOperationException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
				"Utility::getMailingAttributes()::exit");
		return attributes;
	}

	private Set<String> getPreferredLanguage(JSONObject demographicIdentity) {
		String preferredLang = null;
		String preferredLangAttribute = env.getProperty("mosip.default.user-preferred-language-attribute");
		if (!StringUtils.isBlank(preferredLangAttribute)) {
			Object object = demographicIdentity.get(preferredLangAttribute);
			if(object!=null) {
				preferredLang = String.valueOf(object);
				if(preferredLang.contains(ResidentConstants.COMMA)){
					String[] preferredLangArray = preferredLang.split(ResidentConstants.COMMA);
					return Stream.of(preferredLangArray)
							.map(lang -> getPreferredLanguageCodeForLanguageNameBasedOnFlag(preferredLangAttribute, lang))
							.collect(Collectors.toSet());
				}
			}
		}
		if(preferredLang!=null){
			String preferredLanguage=getPreferredLanguageCodeForLanguageNameBasedOnFlag(preferredLangAttribute, preferredLang);
			if(preferredLanguage==null || preferredLanguage.isEmpty()){
				return Set.of();
			}
			return Set.of(preferredLanguage);
		}
		return Set.of();
	}

	public String getPreferredLanguageCodeForLanguageNameBasedOnFlag(String fieldName, String preferredLang) {
		if(isPreferedLangFlagEnabled){
		try {
			ResponseWrapper<?> responseWrapper = (ResponseWrapper<DynamicFieldConsolidateResponseDto>)
					proxyMasterdataService.getDynamicFieldBasedOnLangCodeAndFieldName(fieldName,
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

	private Set<String> getDataCapturedLanguages(JSONObject mapperIdentity, JSONObject demographicIdentity)
			throws ReflectiveOperationException {
		Set<String> dataCapturedLangauges = new HashSet<String>();
		LinkedHashMap<String, String> jsonObject = JsonUtil.getJSONValue(mapperIdentity, MappingJsonConstants.NAME);
		String values = jsonObject.get(VALUE);
		for (String value : values.split(",")) {
			Object object = demographicIdentity.get(value);
			if (object instanceof ArrayList) {
				JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
				JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
				for (JsonValue jsonValue : jsonValues) {
					dataCapturedLangauges.add(jsonValue.getLanguage());
				}
			}
		}
		return dataCapturedLangauges;
	}
	
	private List<String> getDefaultTemplateLanguages() {
		String defaultLanguages = env.getProperty("mosip.default.template-languages");
		List<String> strList = Collections.emptyList() ;
		if (defaultLanguages !=null && !StringUtils.isBlank(defaultLanguages)) {
			String[] lanaguages = defaultLanguages.split(",");
			if(lanaguages!=null && lanaguages.length >0 ) {
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
	

	public String maskData(Object object, String maskingFunctionName) {
		Map context = new HashMap();
		context.put("value", String.valueOf(object));
		VariableResolverFactory myVarFactory = new MapVariableResolverFactory(context);
		myVarFactory.setNextFactory(functionFactory);
		Serializable serializable = MVEL.compileExpression(maskingFunctionName + "(value);");
		String formattedObject = MVEL.executeExpression(serializable, context, myVarFactory, String.class);
		return formattedObject;
	}
	
	public String maskEmail(String email) {
		return maskData(email, emailMaskFunction);
	}

	public String maskPhone(String phone) {
		return maskData(phone, phoneMaskFunction);
	}

	public String convertToMaskData(String maskData) {
		return maskData(maskData, maskingFunction);
	}

	public String getPassword(List<String> attributeValues) {
		Map<String, List<String>> context = new HashMap<>();
		context.put("attributeValues", attributeValues);
		VariableResolverFactory myVarFactory = new MapVariableResolverFactory(context);
		myVarFactory.setNextFactory(functionFactory);
		String maskingFunctionName = this.env.getProperty(ResidentConstants.CREATE_PASSWORD_METHOD_NAME);
		Serializable serializable = MVEL.compileExpression(maskingFunctionName+"(attributeValues);");
		return MVEL.executeExpression(serializable, context, myVarFactory, String.class);
	}

	public ResidentTransactionEntity createEntity(RequestType requestType){
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setRequestDtimes(DateUtils.getUTCCurrentDateTime());
		residentTransactionEntity.setResponseDtime(DateUtils.getUTCCurrentDateTime());
		residentTransactionEntity.setCrBy(getSessionUserName());
		residentTransactionEntity.setCrDtimes(DateUtils.getUTCCurrentDateTime());
		// Initialize with true, so that it is updated as false in later when needed for notification
		if(ServiceType.ASYNC.getRequestTypes().contains(requestType)){
			residentTransactionEntity.setReadStatus(false);
		}else {
			residentTransactionEntity.setReadStatus(true);
		}
		residentTransactionEntity.setRequestTypeCode(requestType.name());
		return residentTransactionEntity;
	}

	public String createEventId() {
		/* return a random long of 16 length */
		long smallest = 1000_0000_0000_0000L;
		long biggest =  9999_9999_9999_9999L;

		// return a long between smallest and biggest (+1 to include biggest as well with the upper bound)
		long random = utilities.getSecureRandom().longs(smallest, biggest + 1).findFirst().getAsLong();
		return String.valueOf(random);
	}


	public static boolean isSecureSession(){
		return Optional.ofNullable(SecurityContextHolder.getContext()) .map(SecurityContext::getAuthentication) .map(Authentication::getPrincipal) .filter(obj -> !obj.equals(ANONYMOUS_USER)) .isPresent();
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

	public byte[] signPdf(InputStream in, String password) {
		logger.debug("UinCardGeneratorImpl::generateUinCard()::entry");
		byte[] pdfSignatured=null;
		try {
			ByteArrayOutputStream pdfValue= (ByteArrayOutputStream)pdfGenerator.generate(in);
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

			responseWrapper= residentServiceRestClient.postApi(env.getProperty(ApiName.PDFSIGN.name())
					, MediaType.APPLICATION_JSON,requestWrapper, ResponseWrapper.class);

			if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
				ServiceError error = responseWrapper.getErrors().get(0);
				throw new ResidentServiceException(ResidentErrorCode.valueOf(error.getMessage()));
			}
			String signatureData= objectMapper.writeValueAsString(responseWrapper.getResponse());
			signatureResponseDto = objectMapper.readValue(signatureData,
					SignatureResponseDto.class);

			pdfSignatured = Base64.decodeBase64(signatureResponseDto.getData());

		} catch (Exception e) {
			logger.error(io.mosip.kernel.pdfgenerator.itext.constant.PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorMessage(),e.getMessage()
					+ ExceptionUtils.getStackTrace(e));
		}
		logger.debug("UinCardGeneratorImpl::generateUinCard()::exit");

		return pdfSignatured;
	}

	public String getFileName(String eventId, String propertyName, int timeZoneOffset, String locale){
		if(eventId!=null && propertyName.contains("{" + TemplateVariablesConstants.EVENT_ID + "}")){
			propertyName = propertyName.replace("{" +TemplateVariablesConstants.EVENT_ID+ "}", eventId);
		}
		if(propertyName.contains("{" + TemplateVariablesConstants.TIMESTAMP + "}")){
			String dateTimeFormat = formatWithOffsetForFileName(timeZoneOffset, locale, DateUtils.getUTCCurrentDateTime());
			propertyName = propertyName.replace("{" +TemplateVariablesConstants.TIMESTAMP+ "}", dateTimeFormat);
		}
		return propertyName;
	}
	
	public String getFileNameForId(String id, String propertyName, int timeZoneOffset, String locale){
		if(id!=null && propertyName.contains("{" + TemplateVariablesConstants.ID + "}")){
			propertyName = propertyName.replace("{" +TemplateVariablesConstants.ID+ "}", id);
		}
		if(propertyName.contains("{" + TemplateVariablesConstants.TIMESTAMP + "}")){
			propertyName = propertyName.replace("{" +TemplateVariablesConstants.TIMESTAMP+ "}", formatWithOffsetForFileName(timeZoneOffset, locale, DateUtils.getUTCCurrentDateTime()));
		}
		return propertyName;
	}

	private String replaceSpecialChars(String fileName) {
		if(!specialCharsReplacementMap.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder(fileName);
			specialCharsReplacementMap.entrySet().forEach(entry -> {
				String key = entry.getKey();
			String value = entry.getValue();
				int index;
				while((index = stringBuilder.indexOf(key)) != -1) {
					stringBuilder.replace(index, index + key.length(), value);
				}
			});
			return stringBuilder.toString();
		}
		return fileName;
	}

	public String getIdForResidentTransaction(String individualId, List<String> channel) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		IdentityDTO identityDTO = identityService.getIdentity(individualId);
		String uin ="";
		String email ="";
		String phone ="";
		if (identityDTO != null) {
			uin = identityDTO.getUIN();
			email = identityDTO.getEmail();
			phone = identityDTO.getPhone();
		}
		String idaToken= identityService.getIDAToken(uin);
		String id;
		if(email != null && phone !=null && channel.size()==2) {
			id= email+phone+idaToken;
		} else if(email != null && channel.size()==1 && channel.get(0).equalsIgnoreCase(EMAIL)) {
			id= email+idaToken;
		} else if(phone != null && channel.size()==1 && channel.get(0).equalsIgnoreCase(PHONE)) {
			id= phone+idaToken;
		}
		else {
			throw new ResidentServiceCheckedException(ResidentErrorCode.NO_CHANNEL_IN_IDENTITY);
		}
		return HMACUtils2.digestAsPlainText(id.getBytes());
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

	public String getFileNameAsPerFeatureName(String eventId, String featureName, int timeZoneOffset, String locale) {
		String namingProperty = RequestType.getRequestTypeByName(featureName).getNamingProperty();
		if (namingProperty == null) {
			namingProperty = ResidentConstants.ACK_NAMING_CONVENTION_PROPERTY;
		}
		return getFileNameAck(featureName, eventId, Objects.requireNonNull(this.env.getProperty(namingProperty)),
				timeZoneOffset, locale);
	}
	
	public String getRefIdHash(String individualId) throws NoSuchAlgorithmException {
		return HMACUtils2.digestAsPlainText(individualId.getBytes());
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
		
		if(locale == null && defaultDateTimePattern == null) {
			locale = Locale.getDefault();
		}
		
		if(locale != null) {
			Chronology chronology = Chronology.ofLocale(locale);
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.valueOf(formattingStyle)).withLocale( locale ).withChronology( chronology ) ;
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
	
	public String getSessionUserName() {
		String name = null;
		try {
			name = identityService.getAvailableclaimValue(this.env.getProperty(ResidentConstants.NAME_FROM_PROFILE));
			if (name == null || name.trim().isEmpty()) {
				name = ResidentConstants.UNKNOWN;
			}
		} catch (ApisResourceAccessException e) {
			throw new RuntimeException(e);
		}
		return name;
	}

	@Cacheable(value = "userInfoCache", key = "#token")
	public Map<String, Object> getUserInfo(String token) throws ApisResourceAccessException {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(usefInfoEndpointUrl);
		UriComponents uriComponent = builder.build(false).encode();

		Map<String, Object> responseMap;
		try {
			MultiValueMap<String, String> headers =
					new LinkedMultiValueMap<String, String>(Map.of(AUTHORIZATION, List.of(BEARER_PREFIX + token)));
			String responseStr = restClientWithPlainRestTemplate.getApi(uriComponent.toUri(), String.class, headers);
			responseMap = (Map<String, Object>) decodeAndDecryptUserInfo(responseStr);
		} catch (ApisResourceAccessException e) {
			throw e;
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "NA",
					"IdAuthServiceImp::lencryptRSA():: ENCRYPTIONSERVICE GET service call"
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Could not fetch public key from kernel keymanager", e);
		}
		return responseMap;
	}

	private Map<String, Object> decodeAndDecryptUserInfo(String userInfoResponseStr) throws JsonParseException, JsonMappingException, UnsupportedEncodingException, IOException  {
		String userInfoStr;
		if (Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_SIGNED))) {
			DecodedJWT decodedJWT = JWT.decode(userInfoResponseStr);
			if (Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_JWT_VERIFY_ENABLED))) {
				ImmutablePair<Boolean, AuthErrorCode> verifySignagure = tokenValidationHelper
						.verifyJWTSignagure(decodedJWT);
				if (verifySignagure.left) {
					userInfoStr = decodeString(getPayload(decodedJWT));
				} else {
					throw new ResidentServiceException(ResidentErrorCode.CLAIM_NOT_AVAILABLE,
							String.format(ResidentErrorCode.CLAIM_NOT_AVAILABLE.getErrorMessage(),
									String.format("User info signature validation failed. Error: %s: %s",
											verifySignagure.getRight().getErrorCode(),
											verifySignagure.getRight().getErrorMessage())));
				}
			} else {
				userInfoStr = decodeString(getPayload(decodedJWT));
			}
		} else {
			userInfoStr = userInfoResponseStr;
		}
		if(Boolean.parseBoolean(this.env.getProperty(ResidentConstants.MOSIP_OIDC_ENCRYPTION_ENABLED))){
			userInfoStr = decodeString(decryptPayload((String) userInfoStr));
		}
		return objectMapper.readValue(userInfoStr.getBytes(UTF_8), Map.class);
	}

	public String decodeString(String payload)
	{
		byte[] bytes = java.util.Base64.getUrlDecoder().decode(payload);
		return new String(bytes, UTF_8);
	}

	private String getPayload(DecodedJWT decodedJWT) {
		return decodedJWT.getPayload();
	}

	public String decryptPayload(String payload) {
		return objectStoreHelper.decryptData(payload, this.env.getProperty(ResidentConstants.RESIDENT_APP_ID),
				this.env.getProperty(ResidentConstants.IDP_REFERENCE_ID));
	}

	@CacheEvict(value = "userInfoCache", key = "#token")
	public void clearUserInfoCache(String token) {
		logger.info("Clearing User Info cache");
	}
	
}
