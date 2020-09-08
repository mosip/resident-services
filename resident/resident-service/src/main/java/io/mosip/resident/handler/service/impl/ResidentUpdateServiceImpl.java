package io.mosip.resident.handler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.PacketMetaInfoConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.FieldValue;
import io.mosip.resident.dto.PackerGeneratorFailureDto;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.RegistrationType;
import io.mosip.resident.dto.ResidentUpdateDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.handler.service.PacketCreationService;
import io.mosip.resident.handler.service.PacketGeneratorService;
import io.mosip.resident.handler.service.SyncUploadEncryptionService;
import io.mosip.resident.handler.validator.RequestHandlerRequestValidator;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.PacketWriterService;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilities;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Qualifier("residentUpdateService")
public class ResidentUpdateServiceImpl implements PacketGeneratorService<ResidentUpdateDto> {

	private final Logger logger = LoggerConfiguration.logConfig(ResidentUpdateServiceImpl.class);
	@Autowired
	private ResidentServiceRestClient restClientService;

	@Autowired
	RequestHandlerRequestValidator validator;

	@Autowired
	private PacketCreationService packetCreationService;

	@Autowired
	SyncUploadEncryptionService syncUploadEncryptionService;

	@Autowired
	private PacketWriterService packetWriterService;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	private Utilities utilities;

	private static final String PROOF_OF_ADDRESS = "proofOfAddress";
	private static final String PROOF_OF_DOB = "proofOfDOB";
	private static final String PROOF_OF_RELATIONSHIP = "proofOfRelationship";
	private static final String PROOF_OF_IDENTITY = "proofOfIdentity";
	private static final String IDENTITY = "identity";
	private static final String FORMAT = "format";
	private static final String TYPE = "type";
	private static final String VALUE = "value";

	@Override
	public PacketGeneratorResDto createPacket(ResidentUpdateDto request) throws BaseCheckedException, IOException {
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(),
				request.getIdValue(), "ResidentUpdateServiceImpl::createPacket()");
		byte[] packetZipBytes = null;
		PackerGeneratorFailureDto dto = new PackerGeneratorFailureDto();
		boolean isTransactional = false;
		if (true/*validator.isValidCenter(request.getCenterId()) && validator.isValidMachine(request.getMachineId())
				&& request.getIdType().equals(ResidentIndividialIDType.UIN)
						? validator.isValidRegistrationTypeAndUin(RegistrationType.RES_UPDATE.toString(),
								request.getIdValue())
						: validator.isValidVid(request.getIdValue())*/) {

			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(),
					request.getIdValue(),
					"ResidentUpdateServiceImpl::createPacket()::validations for UIN,TYPE,CENTER,MACHINE are successful");

			PacketDto packetDto = new PacketDto();

			try {
				Map<String, String> idMap = new HashMap<>();
				String demoJsonString = new String(CryptoUtil.decodeBase64(request.getIdentityJson()));
				JSONObject demoJsonObject = JsonUtil.objectMapperReadValue(demoJsonString, JSONObject.class);
				LinkedHashMap<String, String> fields = (LinkedHashMap<String, String>) demoJsonObject.get(IDENTITY);

				fields.keySet().forEach(key -> idMap.put(key, fields.get(key)));


				// set demographic documents
				Map<String, Document> map = new HashMap<>();
				if (request.getProofOfAddress() != null && !request.getProofOfAddress().isEmpty())
					setDemographicDocuments(request.getProofOfAddress(), demoJsonObject, PROOF_OF_ADDRESS, map);
				if (request.getProofOfDateOfBirth() != null && !request.getProofOfDateOfBirth().isEmpty())
					setDemographicDocuments(request.getProofOfAddress(), demoJsonObject, PROOF_OF_DOB, map);
				if (request.getProofOfRelationship() != null && !request.getProofOfRelationship().isEmpty())
					setDemographicDocuments(request.getProofOfAddress(), demoJsonObject, PROOF_OF_RELATIONSHIP,
							map);
				if (request.getProofOfIdentity() != null && !request.getProofOfIdentity().isEmpty())
					setDemographicDocuments(request.getProofOfAddress(), demoJsonObject, PROOF_OF_IDENTITY, map);


				packetDto.setFields(idMap);
				packetDto.setDocuments(map);
				packetDto.setMetaInfo(getRegistrationMetaData(request.getIdValue(),
						request.getRequestType().toString(), request.getCenterId(), request.getMachineId()));
				packetDto.setId(generateRegistrationId(request.getCenterId(), request.getMachineId()));
				packetDto.setProcess(RegistrationType.RES_UPDATE.toString());
				packetDto.setSource(utilities.getDefaultSource());

				/*RegistrationDTO registrationDTO = createRegistrationDTOObject(request.getIdValue(),
						request.getRequestType().toString(), request.getCenterId(), request.getMachineId());

				registrationDTO.setRegType(RegistrationType.RES_UPDATE.toString());
				registrationDTO.setDemographicDTO(demographicDTO);*/

				packetZipBytes = packetCreationService.create(packetDto, request.getCenterId(), request.getMachineId());

				String rid = packetDto.getId();
				String packetCreatedDateTime = rid.substring(rid.length() - 14);
				String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
						+ packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);
				LocalDateTime ldt = LocalDateTime.parse(formattedDate,
						DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
				String creationTime = ldt.toString() + ".000Z";

				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), packetDto.getId(),
						"ResidentUpdateServiceImpl::createPacket()::packet created and sent for sync service");

				PacketGeneratorResDto packerGeneratorResDto = syncUploadEncryptionService.uploadUinPacket(
						packetDto.getId(), creationTime, request.getRequestType().toString(),
						packetZipBytes);

				logger.debug(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), packetDto.getId(),
						"ResidentUpdateServiceImpl::createPacket()::packet synched and uploaded");
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
				throw new BaseCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorCode(),
						ResidentErrorCode.UNKNOWN_EXCEPTION.getErrorMessage(), e);

			}

		} else
			return dto;
	}

	private void setDemographicDocuments(String documentBytes, JSONObject demoJsonObject, String documentName,
			Map<String, Document> map) {
		JSONObject identityJson = JsonUtil.getJSONObject(demoJsonObject, IDENTITY);
		JSONObject documentJson = JsonUtil.getJSONObject(identityJson, documentName);
		if (documentJson == null)
			return;
		Document docDetailsDto = new Document();
		docDetailsDto.setDocument(CryptoUtil.decodeBase64(documentBytes));
		docDetailsDto.setFormat((String) JsonUtil.getJSONValue(documentJson, FORMAT));
		docDetailsDto.setValue((String) JsonUtil.getJSONValue(documentJson, VALUE));
		docDetailsDto.setType((String) JsonUtil.getJSONValue(documentJson, TYPE));
		map.put(documentName, docDetailsDto);
	}

	/*private RegistrationDTO createRegistrationDTOObject(String uin, String registrationType, String centerId,
			String machineId) throws BaseCheckedException {
		RegistrationDTO registrationDTO = new RegistrationDTO();
		Map<String, String> metadata = getRegistrationMetaData(registrationType, uin, centerId,
				machineId);
		String registrationId = generateRegistrationId(centerId,
				machineId);
		registrationDTO.setRegistrationId(registrationId);
		registrationDTO.setMetadata(metadata);
		return registrationDTO;

	}*/

	private Map<String, String> getRegistrationMetaData(String registrationType, String uin, String centerId,
															String machineId) throws JsonProcessingException {

		Map<String, String> metadata = new HashMap<>();

		FieldValue[] fieldValues = new FieldValue[4];

		fieldValues[0].setLabel(PacketMetaInfoConstants.CENTERID);
		fieldValues[0].setValue(centerId);

		fieldValues[1].setLabel(PacketMetaInfoConstants.MACHINEID);
		fieldValues[1].setValue(machineId);

		fieldValues[2].setLabel(PacketMetaInfoConstants.REGISTRATION_TYPE);
		fieldValues[2].setValue(registrationType);

		fieldValues[3].setLabel(PacketMetaInfoConstants.UIN);
		fieldValues[3].setValue(uin);

		metadata.put("metadata", JsonUtils.javaObjectToJsonString(fieldValues));
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
								+ JsonUtil.objectMapperObjectToJson(ridJson));
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
