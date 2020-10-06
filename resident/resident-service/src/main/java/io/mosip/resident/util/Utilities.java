package io.mosip.resident.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.RegistrationConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdRequestDto;
import io.mosip.resident.dto.IdResponseDTO;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.RequestDto1;
import io.mosip.resident.dto.VidResponseDTO1;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.VidCreationException;
import lombok.Data;
import org.assertj.core.util.Lists;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

	@Value("${provider.packetreader.mosip}")
	private String provider;

	@Autowired
	private RestTemplate residentRestTemplate;

	@Autowired
	private ObjectMapper objMapper;

	@Autowired
	private Environment env;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private TokenGenerator tokenGenerator;

	/** The config server file storage URL. */
	@Value("${config.server.file.storage.uri}")
	private String configServerFileStorageURL;

	/** The get reg processor identity json. */
	@Value("${resident.identityjson}")
	private String getRegProcessorIdentityJson;

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

	public JSONObject retrieveIdrepoJson(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {

		if (uin != null) {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
					"Utilities::retrieveIdrepoJson()::entry");
			List<String> pathSegments = new ArrayList<>();
			pathSegments.add(uin);
			IdResponseDTO1 idResponseDto;

			idResponseDto = (IdResponseDTO1) residentServiceRestClient.getApi(ApiName.IDREPOGETIDBYUIN, pathSegments, "", "",
					IdResponseDTO1.class, tokenGenerator.getToken());
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
			String response = objMapper.writeValueAsString(idResponseDto.getResponse().getIdentity());
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
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::retrieveIdrepoJson()::exit UIN is null");
		return null;
	}

	public JSONObject getRegistrationProcessorMappingJson() throws IOException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::entry");

		mappingJsonString = (mappingJsonString != null && !mappingJsonString.isEmpty()) ?
				mappingJsonString : getJson(configServerFileStorageURL, getRegProcessorIdentityJson);
		ObjectMapper mapIdentityJsonStringToObject = new ObjectMapper();
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"Utilities::getRegistrationProcessorMappingJson()::exit");
		return JsonUtil.getJSONObject(mapIdentityJsonStringToObject.readValue(mappingJsonString, JSONObject.class), MappingJsonConstants.IDENTITY);

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
				VidResponseDTO1.class, tokenGenerator.getToken());
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
				"Utilities::getUinByVid():: RETRIEVEIUINBYVID GET service call ended successfully");

		if (!response.getErrors().isEmpty()) {
			throw new VidCreationException("VID creation exception");

		} else {
			uin = response.getResponse().getUin();
		}
		return uin;
	}

	public boolean linkRegIdWrtUin(String registrationID, String uin) throws ApisResourceAccessException, IOException {

		IdResponseDTO idResponse = null;
		RequestDto1 requestDto = new RequestDto1();
		if (uin != null) {

			JSONObject identityObject = new JSONObject();
			identityObject.put(UIN, uin);
			addSchemaVersion(identityObject);

			requestDto.setRegistrationId(registrationID);
			requestDto.setIdentity(identityObject);

			IdRequestDto idRequestDTO = new IdRequestDto();
			idRequestDTO.setId(idRepoUpdate);
			idRequestDTO.setRequest(requestDto);
			idRequestDTO.setMetadata(null);
			idRequestDTO.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
			idRequestDTO.setVersion(vidVersion);

			idResponse = (IdResponseDTO) residentServiceRestClient.patchApi(env.getProperty(ApiName.IDREPOSITORY.name()), MediaType.APPLICATION_JSON, idRequestDTO,
					IdResponseDTO.class, tokenGenerator.getRegprocToken());

			if (idResponse != null && idResponse.getResponse() != null) {

				logger.info(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registrationID, " UIN Linked with the RegID");

				return true;
			} else {

				logger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registrationID,
						" UIN not Linked with the RegID ");
				return false;
			}

		} else {

			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationID, " UIN is null ");
		}

		return false;
	}

	public String getJson(String configServerFileStorageURL, String uri) {
		return residentRestTemplate.getForObject(configServerFileStorageURL + uri, String.class);
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
					IdResponseDTO1.class, tokenGenerator.getToken());
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

	private void addSchemaVersion(JSONObject identityObject) throws IOException {

		JSONObject regProcessorIdentityJson = getRegistrationProcessorMappingJson();
		String schemaVersion = JsonUtil.getJSONValue(
				JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.IDSCHEMA_VERSION),
				MappingJsonConstants.VALUE);

		identityObject.put(schemaVersion, Float.valueOf(idschemaVersion));

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
		auditDtos.put("createdAt", DateUtils.getUTCCurrentDateTimeString());
		auditDtos.put("eventId", "RPR_405");
		auditDtos.put("eventName", "packet uploaded");
		auditDtos.put("eventType", "USER");
		auditDtos.put("actionTimeStamp", DateUtils.getUTCCurrentDateTimeString());
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


}
