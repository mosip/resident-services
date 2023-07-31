package io.mosip.resident.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.pdf.PdfReader;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RegistrationConstants;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TransactionStage;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.VidResponseDTO1;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.IndividualIdNotFoundException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.service.ProxyMasterdataService;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentConstants.AID_STATUS;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;
import static io.mosip.resident.constant.ResidentConstants.TRANSACTION_TYPE_CODE;
import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * The Class Utilities.
 *
 * @author Girish Yarru
 */
@Component

/**
 * Instantiates a new utilities.
 */
@Data
public class Utilities {
	private final Logger logger = LoggerConfiguration.logConfig(Utilities.class);
	/** The reg proc logger. */
	private static final String sourceStr = "source";

	/** The Constant UIN. */
	private static final String UIN = "UIN";

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = "\\";

	/** The Constant RE_PROCESSING. */
	private static final String RE_PROCESSING = "re-processing";

	/** The Constant HANDLER. */
	private static final String HANDLER = "handler";

	/** The Constant NEW_PACKET. */
	private static final String NEW_PACKET = "New-packet";

	@Value("${IDSchema.Version}")
	private String idschemaVersion;

	@Value("${provider.packetwriter.resident}")
	private String provider;

	@Autowired
	@Qualifier("selfTokenRestTemplate")
	private RestTemplate residentRestTemplate;

	@Autowired
	private ObjectMapper objMapper;

	@Autowired
	private Environment env;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private ProxyMasterdataService proxyMasterdataService;

	/** The config server file storage URL. */
	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	/** The get reg processor identity json. */
	@Value("${registration.processor.identityjson}")
	private String residentIdentityJson;

	/** The id repo update. */
	@Value("${id.repo.update}")
	private String idRepoUpdate;

	/** The vid version. */
	@Value("${resident.vid.version}")
	private String vidVersion;


	/** The Constant NAME. */
	private static final String NAME = "name";

	private static final String VALUE = "value";

	private String mappingJsonString = null;

    private static String regProcessorIdentityJson = "";
	private SecureRandom secureRandom;

	/** The acr-amr mapping json file. */
	@Value("${amr-acr.json.filename}")
	private String amrAcrJsonFile;

	private static final String ACR_AMR = "acr_amr";


	@PostConstruct
    private void loadRegProcessorIdentityJson() {
        regProcessorIdentityJson = residentRestTemplate.getForObject(configServerFileStorageURL + residentIdentityJson, String.class);
        logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "loadRegProcessorIdentityJson completed successfully");
    }

    public JSONObject retrieveIdrepoJson(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {

		if (uin != null) {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) residentServiceRestClient.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class);
			if (idResponseDto == null) {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
				return null;
			}
			if (!idResponseDto.getErrors().isEmpty()) {
				List<ServiceError> error = idResponseDto.getErrors();
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
				throw new IdRepoAppException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), error.get(0).getMessage());
			}
			return convertIdResponseIdentityObjectToJsonObject(idResponseDto.getResponse().getIdentity());
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::retrieveIdrepoJson()::exit UIN is null");
		return null;
	}

	public JSONObject convertIdResponseIdentityObjectToJsonObject(Object identityObject) throws JsonProcessingException {
		String response = objMapper.writeValueAsString(identityObject);
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
		try {
			return (JSONObject) new JSONParser().parse(response);
		} catch (org.json.simple.parser.ParseException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					ExceptionUtils.getStackTrace(e));
			throw new IdRepoAppException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), "Error while parsing string to JSONObject",e);
		}
	}

	public Tuple3<JSONObject, String, IdResponseDTO1> getIdentityDataFromIndividualID(String individualId) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		IdResponseDTO1 idResponseDto = retrieveIdRepoJsonIdResponseDto(individualId);
		JSONObject idRepoJson = convertIdResponseIdentityObjectToJsonObject(idResponseDto.getResponse().getIdentity());
		String schemaJson = getSchemaJsonFromIdRepoJson(idRepoJson);
		return Tuples.of(idRepoJson, schemaJson, idResponseDto);
	}

	public String getSchemaJsonFromIdRepoJson(JSONObject idRepoJson) throws ResidentServiceCheckedException {
		String idSchemaVersionStr = String.valueOf(idRepoJson.get(ResidentConstants.ID_SCHEMA_VERSION));
		Double idSchemaVersion = Double.parseDouble(idSchemaVersionStr);
		ResponseWrapper<?> idSchemaResponse = proxyMasterdataService.getLatestIdSchema(idSchemaVersion, null, null);
		Object idSchema = idSchemaResponse.getResponse();
		Map<String, ?> map = objMapper.convertValue(idSchema, Map.class);
		return ((String) map.get("schemaJson"));
	}

	public JSONObject getRegistrationProcessorMappingJson() throws IOException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::entry");

		mappingJsonString = (mappingJsonString != null && !mappingJsonString.isEmpty()) ?
				mappingJsonString : getJson(configServerFileStorageURL, residentIdentityJson);
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::exit");
		return JsonUtil.getJSONObject(objMapper.readValue(mappingJsonString, JSONObject.class), MappingJsonConstants.IDENTITY);

	}

	public String getUinByVid(String vid) throws ApisResourceAccessException, VidCreationException, IOException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::getUinByVid():: entry");
		List<String> pathSegments = new ArrayList<>();
		pathSegments.add(vid);
		String uin = null;
		VidResponseDTO1 response;
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Stage::methodname():: RETRIEVEIUINBYVID GET service call Started");

		response = (VidResponseDTO1) residentServiceRestClient.getApi(ApiName.GETUINBYVID, pathSegments, "", "",
				VidResponseDTO1.class);
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::getUinByVid():: RETRIEVEIUINBYVID GET service call ended successfully");

		if (!response.getErrors().isEmpty()) {
			throw new VidCreationException("VID creation exception");

		} else {
			uin = response.getResponse().getUin();
		}
		return uin;
	}

	public String getRidByIndividualId(String individualId) throws ApisResourceAccessException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::getRidByIndividualId():: entry");
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("individualId", individualId);
		String rid = null;
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Stage::methodname():: RETRIEVEIUINBYVID GET service call Started");

		ResponseWrapper<?> response = residentServiceRestClient.getApi(ApiName.GET_RID_BY_INDIVIDUAL_ID,
				pathsegments, ResponseWrapper.class);
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::getRidByIndividualId():: GET_RID_BY_INDIVIDUAL_ID GET service call ended successfully");

		if (!response.getErrors().isEmpty()) {
			throw new IndividualIdNotFoundException("Individual ID not found exception");

		} else {
			rid = (String) ((Map<String, ?>)response.getResponse()).get(ResidentConstants.RID);
		}
		return rid;
	}

	public ArrayList<?> getRidStatus(String rid) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Utilities::getRidStatus():: entry");
		Map<String, String> pathsegments = new HashMap<String, String>();
		pathsegments.put("rid", rid);
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"Stage::methodname():: RETRIEVEIUINBYVID GET service call Started");
		ResponseWrapper<?> responseWrapper = (ResponseWrapper<?>)residentServiceRestClient.getApi(ApiName.GET_RID_STATUS,
				pathsegments, ResponseWrapper.class);
		if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
			logger.debug(responseWrapper.getErrors().get(0).toString());
			throw new ResidentServiceCheckedException(ResidentErrorCode.RID_NOT_FOUND.getErrorCode(),
					responseWrapper.getErrors().get(0).getMessage());
		}
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::getRidByIndividualId():: GET_RID_BY_INDIVIDUAL_ID GET service call ended successfully");
		ArrayList<?> objectArrayList = objMapper.readValue(
				objMapper.writeValueAsString(responseWrapper.getResponse()), ArrayList.class);
		return objectArrayList;
	}

	public Map<String, String> getPacketStatus(String rid)
			throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
		Map<String, String> packetStatusMap = new HashMap<>();
		ArrayList<?> regTransactionList = getRidStatus(rid);
		for (Object object : regTransactionList) {
			if (object instanceof Map) {
				Map<String, Object> packetData = (Map<String, Object>) object;
				Optional<String> packetStatusCode = getPacketStatusCode(packetData);
				Optional<String> transactionTypeCode = getTransactionTypeCode(packetData);
				if (packetStatusCode.isPresent() && transactionTypeCode.isPresent()) {
					packetStatusMap.put(AID_STATUS, packetStatusCode.get());
					packetStatusMap.put(TRANSACTION_TYPE_CODE, transactionTypeCode.get());
					return packetStatusMap;
				}
			}
		}
		throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(),
				String.format("%s - Unable to get the RID status from Reg-proc",
						ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorMessage()));
	}

	private Optional<String> getPacketStatusCode(Map<String, Object> packetData) {
		String statusCode = (String) packetData.get(STATUS_CODE);
		Optional<String> packetStatusCode = PacketStatus.getStatusCode(statusCode, env);
		return packetStatusCode;
	}

	private Optional<String> getTransactionTypeCode(Map<String, Object> packetData) {
		String transactionTypeCode = (String) packetData.get(TRANSACTION_TYPE_CODE);
		Optional<String> typeCode = TransactionStage.getTypeCode(transactionTypeCode, env);
		return typeCode;
	}

	public String getJson(String configServerFileStorageURL, String uri) {
        if (StringUtils.isBlank(regProcessorIdentityJson)) {
            return residentRestTemplate.getForObject(configServerFileStorageURL + uri, String.class);
        }
        return regProcessorIdentityJson;
    }

	public String retrieveIdrepoJsonStatus(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {
		String response = null;
		if (uin != null) {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) residentServiceRestClient.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class);
			if (idResponseDto == null) {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
				return null;
			}
			if (!idResponseDto.getErrors().isEmpty()) {
				List<ServiceError> error = idResponseDto.getErrors();
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
				throw new IdRepoAppException(error.get(0).getErrorCode(), error.get(0).getMessage());
			}

			response = idResponseDto.getResponse().getStatus();

			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
		}

		return response;
	}

	public IdResponseDTO1 retrieveIdRepoJsonIdResponseDto(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {
		IdResponseDTO1 response = null;
		if (uin != null) {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) residentServiceRestClient.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class);
			if (idResponseDto == null) {
				logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
				return null;
			}
			if (!idResponseDto.getErrors().isEmpty()) {
				List<ServiceError> error = idResponseDto.getErrors();
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
						"Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
				throw new IdRepoAppException(error.get(0).getErrorCode(), error.get(0).getMessage());
			}

			response = idResponseDto;

			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
		}

		return response;
	}

	public String getDefaultSource() {
		String[] strs = provider.split(",");
		List<String> strList = Lists.newArrayList(strs);
		Optional<String> optional = strList.stream().filter(s -> s.contains(sourceStr)).findAny();
		String source = optional.isPresent() ? optional.get().replace(sourceStr + ":", "") : null;
		return source;
	}

	public List<Map<String, String>> generateAudit(String rid) {
		// Getting Host IP Address and Name
		String hostIP = null;
		String hostName = null;
		try {
			hostIP = InetAddress.getLocalHost().getHostAddress();
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException unknownHostException) {
			hostIP = ServerUtil.getServerUtilInstance().getServerIp();
			hostName = ServerUtil.getServerUtilInstance().getServerName();
		}

		List<Map<String, String>> mapList = new ArrayList<>();

		Map<String, String> auditDtos = new HashMap<>();
		auditDtos.put("uuid", UUID.randomUUID().toString());
		String timestamp = DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime());
		auditDtos.put("createdAt", timestamp);
		auditDtos.put("eventId", "RPR_405");
		auditDtos.put("eventName", "packet uploaded");
		auditDtos.put("eventType", "USER");
		auditDtos.put("actionTimeStamp", timestamp);
		auditDtos.put("hostName", hostName);
		auditDtos.put("hostIp", hostIP);
		auditDtos.put("applicationId", env.getProperty(RegistrationConstants.APP_NAME));
		auditDtos.put("applicationName", env.getProperty(RegistrationConstants.APP_NAME));
		auditDtos.put("sessionUserId", "mosip");
		auditDtos.put("sessionUserName", "Registration");
		auditDtos.put("id", rid);
		auditDtos.put("idType", "REGISTRATION_ID");
		auditDtos.put("createdBy", "Packet_Generator");
		auditDtos.put("moduleName", "REQUEST_HANDLER_SERVICE");
		auditDtos.put("moduleId", "REG - MOD - 119");
		auditDtos.put("description", "Packet uploaded successfully");

		mapList.add(auditDtos);

		return mapList;
	}

	public String getLanguageCode() {
		String langCode=null;
		String mandatoryLanguages = env.getProperty("mosip.mandatory-languages");
		if (mandatoryLanguages!=null && !StringUtils.isBlank(mandatoryLanguages)) {
			String[] lanaguages = mandatoryLanguages.split(",");
			langCode = lanaguages[0];
		} else {
			String optionalLanguages = env.getProperty("mosip.optional-languages");
			if (optionalLanguages!= null && !StringUtils.isBlank(optionalLanguages)) {
				String[] lanaguages = optionalLanguages.split(",");
				langCode = lanaguages[0];
			}
		}
		return langCode;
	}
	
	    
    public String getPhoneAttribute() throws ResidentServiceCheckedException {
    	return getIdMappingAttributeForKey(MappingJsonConstants.PHONE);
    }
    
    public String getEmailAttribute() throws ResidentServiceCheckedException {
    	return getIdMappingAttributeForKey(MappingJsonConstants.EMAIL);
    }

	private String getIdMappingAttributeForKey(String attributeKey) throws ResidentServiceCheckedException {
		try {
			JSONObject regProcessorIdentityJson = getRegistrationProcessorMappingJson();
			String phoneAttribute = JsonUtil.getJSONValue(
			        JsonUtil.getJSONObject(regProcessorIdentityJson, attributeKey),
			        MappingJsonConstants.VALUE);
			return phoneAttribute;
		} catch (IOException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
					ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
		}
	}

	public int getTotalNumberOfPageInPdf(ByteArrayOutputStream outputStream) throws IOException {
		PdfReader pdfReader = new PdfReader(outputStream.toByteArray());
		return pdfReader.getNumberOfPages();
	}

	@PostConstruct
	public void initializeSecureRandomInstance(){
		secureRandom = new SecureRandom();
	}

	public SecureRandom getSecureRandom(){
		return secureRandom;
	}

	@Cacheable(value = "amr-acr-mapping")
	public Map<String, String> getAmrAcrMapping() throws ResidentServiceCheckedException {
		String amrAcrJson = residentRestTemplate.getForObject(configServerFileStorageURL + amrAcrJsonFile,
				String.class);
		Map<String, Object> amrAcrMap = Map.of();
		try {
			if (amrAcrJson != null) {
				amrAcrMap = objMapper.readValue(amrAcrJson.getBytes(UTF_8), Map.class);
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
}