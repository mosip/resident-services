package io.mosip.resident.handler.service.impl;

import ch.qos.logback.core.status.StatusUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.PacketMetaInfoConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DemographicDTO;
import io.mosip.resident.dto.ErrorDTO;
import io.mosip.resident.dto.PackerGeneratorFailureDto;
import io.mosip.resident.dto.PacketGeneratorDto;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegistrationDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.PacketCreationService;
import io.mosip.resident.handler.service.PacketGeneratorService;
import io.mosip.resident.handler.service.SyncUploadEncryptionService;
import io.mosip.resident.handler.validator.RequestHandlerRequestValidator;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilities;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sowmya The Class PacketGeneratorServiceImpl.
 */
@Service
@Qualifier("packetGeneratorService")
public class PacketGeneratorServiceImpl implements PacketGeneratorService<PacketGeneratorDto> {

	/** The packet creation service. */
	@Autowired
	private PacketCreationService packetCreationService;

	@Autowired
	private TokenGenerator tokenGenerator;

	/** The sync upload encryption service. */
	@Autowired
	SyncUploadEncryptionService syncUploadEncryptionService;

	@Autowired
	private Utilities utilities;

	@Value("${IDSchema.Version}")
	private String idschemaVersion;

	/** The rest client service. */
	@Autowired
	private ResidentServiceRestClient restClientService;

	/** The primary languagecode. */
	@Value("${mosip.primary-language}")
	private String primaryLanguagecode;

	private final Logger logger = LoggerConfiguration.logConfig(PacketGeneratorServiceImpl.class);

	@Autowired
	RequestHandlerRequestValidator validator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.packet.service.PacketGeneratorService#
	 * createPacket(io.mosip.registration.processor.packet.service.dto.
	 * PacketGeneratorDto)
	 */
	@Override
	public PacketGeneratorResDto createPacket(PacketGeneratorDto request) throws BaseCheckedException, IOException {
		boolean isTransactional = false;
		PacketGeneratorResDto packerGeneratorResDto = null;
		PackerGeneratorFailureDto dto = new PackerGeneratorFailureDto();
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"PacketGeneratorServiceImpl ::createPacket()::entry");
		byte[] packetZipBytes = null;
		if (validator.isValidCenter(request.getCenterId()) && validator.isValidMachine(request.getMachineId())
				&& validator.isValidRegistrationTypeAndUin(request.getRegistrationType(), request.getUin())) {
			try {
				logger.info(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"Packet Generator Validation successfull");
				RegistrationDTO registrationDTO = createRegistrationDTOObject(request.getUin(),
						request.getRegistrationType(), request.getCenterId(), request.getMachineId());
				packetZipBytes = null;//packetCreationService.create(registrationDTO, request.getCenterId(), request.getMachineId());
				String rid = registrationDTO.getRegistrationId();
				String packetCreatedDateTime = rid.substring(rid.length() - 14);
				String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
						+ packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);
				LocalDateTime ldt = LocalDateTime.parse(formattedDate,
						DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
				String creationTime = ldt.toString() + ".000Z";

				packerGeneratorResDto = syncUploadEncryptionService.uploadUinPacket(
						registrationDTO.getRegistrationId(), creationTime, request.getRegistrationType(),
						packetZipBytes);
				isTransactional = true;
				return packerGeneratorResDto;
			} catch (Exception e) {
				logger.error(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(),
						ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(),
						ExceptionUtils.getStackTrace(e));
				if (e instanceof BaseCheckedException) {
					throw (BaseCheckedException) e;
				}
				throw new BaseCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(), ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(), e);

			}
		} else
			return dto;
	}

	/**
	 * Creates the registration DTO object.
	 *
	 * @param uin              the uin
	 * @param registrationType the registration type
	 * @param centerId         the center id
	 * @param machineId        the machine id
	 * @return the registration DTO
	 * @throws BaseCheckedException
	 */
	private RegistrationDTO createRegistrationDTOObject(String uin, String registrationType, String centerId,
			String machineId) throws BaseCheckedException, IOException {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setDemographicDTO(getDemographicDTO(uin));
		Map<String, String> metadata = getRegistrationMetaData(registrationType, uin, centerId,
				machineId);
		String registrationId = generateRegistrationId(centerId,
				machineId);
		registrationDTO.setRegistrationId(registrationId);
		registrationDTO.setMetadata(metadata);
		return registrationDTO;

	}

	/**
	 * Gets the demographic DTO.
	 *
	 * @param uin the uin
	 * @return the demographic DTO
	 */
	private DemographicDTO getDemographicDTO(String uin) throws IOException {
		DemographicDTO demographicDTO = new DemographicDTO();
		JSONObject jsonObject = new JSONObject();

		JSONObject regProcessorIdentityJson = utilities.getRegistrationProcessorMappingJson();
		String schemaVersion = JsonUtil.getJSONValue(
				JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.IDSCHEMA_VERSION),
				MappingJsonConstants.VALUE);

		String uinLabel = JsonUtil.getJSONValue(
				JsonUtil.getJSONObject(regProcessorIdentityJson, MappingJsonConstants.UIN),
				MappingJsonConstants.VALUE);

		jsonObject.put(schemaVersion, Float.valueOf(idschemaVersion));
		jsonObject.put(uinLabel, uin);
		demographicDTO.setIdentity(jsonObject);

		return demographicDTO;
	}

	/**
	 * Gets the registration meta data DTO.
	 *
	 * @param registrationType the registration type
	 * @param uin              the uin
	 * @param centerId         the center id
	 * @param machineId        the machine id
	 * @return the registration meta data DTO
	 */
	private Map<String, String> getRegistrationMetaData(String registrationType, String uin, String centerId,
																 String machineId) {
		Map<String, String> metadata = new HashMap<>();
		metadata.put(PacketMetaInfoConstants.CENTERID, centerId);
		metadata.put(PacketMetaInfoConstants.MACHINEID, machineId);
		metadata.put(PacketMetaInfoConstants.REGISTRATION_TYPE, registrationType);
		metadata.put(PacketMetaInfoConstants.UIN, uin);
		return metadata;
	}

	private String generateRegistrationId(String centerId, String machineId) throws BaseCheckedException {

		List<String> pathsegments = new ArrayList<>();
		pathsegments.add(centerId);
		pathsegments.add(machineId);
		String rid = null;
		ResponseWrapper<?> responseWrapper;
		JSONObject ridJson;
		ObjectMapper mapper = new ObjectMapper();
		try {

			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", "PacketGeneratorServiceImpl::generateRegistrationId():: RIDgeneration Api call started");
			responseWrapper = (ResponseWrapper<?>) restClientService.getApi(ApiName.RIDGENERATION, pathsegments, "", "",
					ResponseWrapper.class, tokenGenerator.getToken());
			if (responseWrapper.getErrors() == null) {
				ridJson = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), JSONObject.class);
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"PacketGeneratorServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
								+ JsonUtils.javaObjectToJsonString(ridJson));
				rid = (String) ridJson.get("rid");

			} else {
				List<ServiceError> error = responseWrapper.getErrors();
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"PacketGeneratorServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
								+ error.get(0).getMessage());
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(),
						error.get(0).getMessage(), new Throwable());
			}

		} catch (ApisResourceAccessException e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getMessage(), e);
			}
		} catch (IOException e) {
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), e.getMessage(), e);
		}
		return rid;
	}
}
