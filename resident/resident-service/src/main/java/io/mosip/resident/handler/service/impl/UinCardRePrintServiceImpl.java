/*
package io.mosip.resident.handler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.CardType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.PacketMetaInfoConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.DemographicDTO;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegistrationDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.UinCardRePrintRequestDto;
import io.mosip.resident.dto.VidRequestDto1;
import io.mosip.resident.dto.VidResponseDTO1;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.handler.service.PacketCreationService;
import io.mosip.resident.handler.service.SyncUploadEncryptionService;
import io.mosip.resident.handler.validator.RequestHandlerRequestValidator;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilities;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

*/
/**
 * The Class ResidentServiceRePrintServiceImpl.
 *//*
*/
/**//*

@Service
public class UinCardRePrintServiceImpl {

	*/
/** The env. *//*

	@Autowired
	private Environment env;

	@Value("${IDSchema.Version}")
	private String idschemaVersion;

	*/
/** The rest client service. *//*

	@Autowired
	private ResidentServiceRestClient restClientService;

	*/
/** The packet creation service. *//*

	@Autowired
	private PacketCreationService packetCreationService;

	*/
/** The sync upload encryption service. *//*

	@Autowired
	SyncUploadEncryptionService syncUploadEncryptionService;

	*/
/** The validator. *//*

	@Autowired
	private RequestHandlerRequestValidator validator;

	@Autowired
	private TokenGenerator tokenGenerator;

	*/
/** The utilities. *//*

	@Autowired
	Utilities utilities;

	*/
/** The vid type. *//*

	@Value("${registration.processor.id.repo.vidType}")
	private String vidType;

	*/
/** The Constant VID_CREATE_ID. *//*

	public static final String VID_CREATE_ID = "registration.processor.id.repo.generate";

	*/
/** The Constant REG_PROC_APPLICATION_VERSION. *//*

	public static final String REG_PROC_APPLICATION_VERSION = "registration.processor.id.repo.vidVersion";

	*/
/** The Constant DATETIME_PATTERN. *//*

	public static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	*/
/** The Constant UIN. *//*

	public static final String UIN = "UIN";

	*/
/** The Constant VID. *//*

	public static final String VID = "VID";

	*/
/** The reg proc logger. *//*

	private final Logger logger = LoggerConfiguration.logConfig(UinCardRePrintServiceImpl.class);

	public static final String VID_TYPE = "registration.processor.id.repo.vidType";

	*/
/**
	 * Creates the packet.
	 *
	 * @param uinCardRePrintRequestDto the uin card re print request dto
	 * @return the packet generator res dto
	 * @throws BaseCheckedException the reg base checked exception
	 * @throws IOException             Signals that an I/O exception has occurred.
	 *//*

	@SuppressWarnings("unchecked")
	public PacketGeneratorResDto createPacket(UinCardRePrintRequestDto uinCardRePrintRequestDto)
			throws BaseCheckedException, IOException {
		boolean isTransactional = false;
		String uin = null;
		String vid = null;
		byte[] packetZipBytes = null;
		PacketGeneratorResDto packetGeneratorResDto = new PacketGeneratorResDto();
		validator.validate(uinCardRePrintRequestDto.getRequesttime(), uinCardRePrintRequestDto.getId(),
				uinCardRePrintRequestDto.getVersion());
		try {
			if (validator.isValidCenter(uinCardRePrintRequestDto.getRequest().getCenterId())
					&& validator.isValidMachine(uinCardRePrintRequestDto.getRequest().getMachineId())
					&& validator
							.isValidRePrintRegistrationType(uinCardRePrintRequestDto.getRequest().getRegistrationType())
					&& validator.isValidIdType(uinCardRePrintRequestDto.getRequest().getIdType())
					&& validator.isValidCardType(uinCardRePrintRequestDto.getRequest().getCardType())
					&& isValidUinVID(uinCardRePrintRequestDto)) {
				String cardType = uinCardRePrintRequestDto.getRequest().getCardType();
				String regType = uinCardRePrintRequestDto.getRequest().getRegistrationType();

				if (uinCardRePrintRequestDto.getRequest().getIdType().equalsIgnoreCase(UIN))
					uin = uinCardRePrintRequestDto.getRequest().getId();
				else
					vid = uinCardRePrintRequestDto.getRequest().getId();

				if (cardType.equalsIgnoreCase(CardType.MASKED_UIN.toString()) && vid == null) {

					VidRequestDto1 vidRequestDto = new VidRequestDto1();
					RequestWrapper<VidRequestDto1> request = new RequestWrapper<>();
					VidResponseDTO1 response;
					vidRequestDto.setUIN(uin);
					vidRequestDto.setVidType(env.getProperty(VID_TYPE));
					request.setId(env.getProperty(VID_CREATE_ID));
					request.setRequest(vidRequestDto);
					DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
					LocalDateTime localdatetime = LocalDateTime
							.parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);
					request.setRequesttime(DateUtils.getUTCCurrentDateTimeString());
					request.setVersion(env.getProperty(REG_PROC_APPLICATION_VERSION));

					logger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), "",
							"UinCardRePrintServiceImpl::createPacket():: post CREATEVID service call started with request data : "
									+ JsonUtil.objectMapperObjectToJson(vidRequestDto));

					response = (VidResponseDTO1) restClientService.postApi(env.getProperty(ApiName.CREATEVID.name()), MediaType.APPLICATION_JSON, request,
							VidResponseDto.class, tokenGenerator.getToken());

					logger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), "",
							"UinCardRePrintServiceImpl::createPacket():: post CREATEVID service call ended successfully");

					if (!response.getErrors().isEmpty()) {
						throw new VidCreationException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage());

					} else {
						vid = response.getResponse().getVid();
					}

				}
				if (uin == null) {
					uin = utilities.getUinByVid(vid);
				}

				RegistrationDTO registrationDTO = createRegistrationDTOObject(uin,
						uinCardRePrintRequestDto.getRequest().getRegistrationType(),
						uinCardRePrintRequestDto.getRequest().getCenterId(),
						uinCardRePrintRequestDto.getRequest().getMachineId(), vid, cardType);
				packetZipBytes = packetCreationService.create(null,
						uinCardRePrintRequestDto.getRequest().getCenterId(), uinCardRePrintRequestDto.getRequest().getMachineId());
				String rid = registrationDTO.getRegistrationId();
				String packetCreatedDateTime = rid.substring(rid.length() - 14);
				String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
						+ packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);
				LocalDateTime ldt = LocalDateTime.parse(formattedDate,
						DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
				String creationTime = ldt.toString() + ".000Z";

				if (utilities.linkRegIdWrtUin(rid, uin))
					packetGeneratorResDto = syncUploadEncryptionService.uploadUinPacket(
							registrationDTO.getRegistrationId(), creationTime, regType, packetZipBytes);
				else
					logger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), rid,
							"UinCardRePrintServiceImpl::createPacket():: RID link to UIN failed");

			}
			isTransactional = true;
			return packetGeneratorResDto;
		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(), ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(), e);
		} catch (VidCreationException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorCode(), ResidentErrorCode.VID_CREATION_EXCEPTION.getErrorMessage(), e);
		} catch (IdObjectValidationFailedException | IdObjectIOException | ParseException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"",
					ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage() + ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
		}
	}

	*/
/**
	 * Creates the registration DTO object.
	 *
	 * @param uin              the uin
	 * @param registrationType the registration type
	 * @param centerId         the center id
	 * @param machineId        the machine id
	 * @param vid              the vid
	 * @param cardType         the card type
	 * @return the registration DTO
	 * @throws BaseCheckedException the reg base checked exception
	 *//*

	private RegistrationDTO createRegistrationDTOObject(String uin, String registrationType, String centerId,
														String machineId, String vid, String cardType) throws BaseCheckedException, IOException {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		registrationDTO.setDemographicDTO(getDemographicDTO(uin));
		Map<String, String> metadata = getRegistrationMetaData(uin, registrationType, centerId,
				machineId, vid, cardType);
		String registrationId = generateRegistrationId(centerId,
				machineId);
		registrationDTO.setRegistrationId(registrationId);
		registrationDTO.setMetadata(metadata);
		return registrationDTO;

	}

	*/
/**
	 * Gets the registration meta data DTO.
	 *
	 * @param uin              the uin
	 * @param registrationType the registration type
	 * @param centerId         the center id
	 * @param machineId        the machine id
	 * @param vid              the vid
	 * @param cardType         the card type
	 * @return the registration meta data DTO
	 *//*

	private Map<String, String> getRegistrationMetaData(String uin, String registrationType, String centerId,
														String machineId, String vid, String cardType) {
		Map<String, String> metadata = new HashMap<>();

		metadata.put(PacketMetaInfoConstants.CENTERID, centerId);
		metadata.put(PacketMetaInfoConstants.MACHINEID, machineId);
		metadata.put(PacketMetaInfoConstants.REGISTRATION_TYPE, registrationType);
		metadata.put(PacketMetaInfoConstants.UIN, uin);
		metadata.put(PacketMetaInfoConstants.VID, vid);
		metadata.put(PacketMetaInfoConstants.CARD_TYPE, cardType);
		return metadata;
	}

	*/
/**
	 * Gets the demographic DTO.
	 *
	 * @param uin the uin
	 * @return the demographic DTO
	 *//*

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

	*/
/**
	 * Generate registration id.
	 *
	 * @param centerId  the center id
	 * @param machineId the machine id
	 * @return the string
	 * @throws BaseCheckedException the reg base checked exception
	 *//*

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
					"", "UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call started");
			responseWrapper = (ResponseWrapper<?>) restClientService.getApi(ApiName.RIDGENERATION, pathsegments, "", "",
					ResponseWrapper.class, tokenGenerator.getToken());
			if (responseWrapper.getErrors() == null) {
				ridJson = mapper.readValue(mapper.writeValueAsString(responseWrapper.getResponse()), JSONObject.class);
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
								+ JsonUtil.objectMapperObjectToJson(ridJson));
				rid = (String) ridJson.get("rid");

			} else {
				List<ServiceError> error = responseWrapper.getErrors();
				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), "",
						"\"UinCardRePrintServiceImpl::generateRegistrationId():: RIDgeneration Api call  ended with response data : "
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

	*/
/**
	 * Checks if is valid uin VID.
	 *
	 * @param uinCardRePrintRequestDto the uin card re print request dto
	 * @return true, if is valid uin VID
	 * @throws BaseCheckedException the reg base checked exception
	 *//*

	public boolean isValidUinVID(UinCardRePrintRequestDto uinCardRePrintRequestDto) throws BaseCheckedException, IOException {
		boolean isValid = false;
		if (uinCardRePrintRequestDto.getRequest().getIdType().equalsIgnoreCase(UIN)) {
			isValid = validator.isValidUin(uinCardRePrintRequestDto.getRequest().getId());
		} else if (uinCardRePrintRequestDto.getRequest().getIdType().equalsIgnoreCase(VID)) {
			isValid = validator.isValidVid(uinCardRePrintRequestDto.getRequest().getId());
		}
		return isValid;
	}
}
*/
