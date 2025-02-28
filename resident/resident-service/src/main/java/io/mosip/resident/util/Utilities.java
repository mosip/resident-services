package io.mosip.resident.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.Data;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.mosip.resident.constant.RegistrationConstants.DATETIME_PATTERN;
import static io.mosip.resident.constant.ResidentConstants.AID_STATUS;
import static io.mosip.resident.constant.ResidentConstants.STATUS_CODE;
import static io.mosip.resident.constant.ResidentConstants.TRANSACTION_TYPE_CODE;


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
	private static final String CREATE_DATE_TIMES = "createdDateTimes";
	private final Logger logger = LoggerConfiguration.logConfig(Utilities.class);
	/** The reg proc logger. */
	private static final String sourceStr = "source";

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = "\\";

	private static final long SIZE_THRESHOLD = 10 * 1024 * 1024; // 10MB threshold

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

	/** The config server file storage URL. */
	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	/** The get reg processor identity json. */
	@Value("${registration.processor.identityjson}")
	private String residentIdentityJson;

	private String mappingJsonString = null;

	private static String regProcessorIdentityJson = "";
	private SecureRandom secureRandom;

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
			throw new IndividualIdNotFoundException(String.format("%s: %s", ResidentErrorCode.INVALID_INDIVIDUAL_ID.getErrorMessage(), vid));

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
		return sortedRegprocStageList(objectArrayList);
	}

	public ArrayList<?> sortedRegprocStageList(ArrayList<?> objectArrayList) {
		if (objectArrayList.isEmpty() || !(objectArrayList.get(0) instanceof Map)) {
			throw new IllegalArgumentException("Input ArrayList must contain Map objects.");
		}
		ArrayList<Map<String, String>> arrayListOfMaps = (ArrayList<Map<String, String>>) objectArrayList;
		arrayListOfMaps.sort((map1, map2) -> {
			SimpleDateFormat dateFormat = new SimpleDateFormat(Objects.requireNonNull(env.getProperty(DATETIME_PATTERN)));
			String dateTime1 = map1.get(CREATE_DATE_TIMES);
			String dateTime2 = map2.get(CREATE_DATE_TIMES);

			try {
				Date date1 = dateFormat.parse(dateTime1);
				Date date2 = dateFormat.parse(dateTime2);
				return date2.compareTo(date1);
			} catch (ParseException e) {
				throw new IllegalArgumentException("Date parsing error: " + e.getMessage());
			}
		});

		return arrayListOfMaps;
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
		byte[] pdfBytes = outputStream.toByteArray();
		return getPageCountWithPDDocument(pdfBytes);
	}

	private static int getPageCountWithPDDocument(byte[] pdfBytes) throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
			 PDDocument document = PDDocument.load(inputStream, MemoryUsageSetting.setupTempFileOnly())) {
			return document.getNumberOfPages();
		}
	}
	@PostConstruct
	public void initializeSecureRandomInstance(){
		secureRandom = new SecureRandom();
	}

	public SecureRandom getSecureRandom(){
		return secureRandom;
	}

}