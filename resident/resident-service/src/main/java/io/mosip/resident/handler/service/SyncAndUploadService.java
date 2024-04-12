package io.mosip.resident.handler.service;

import static io.mosip.kernel.core.util.JsonUtils.javaObjectToJsonString;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.RegistrationConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.PacketGeneratorResDto;
import io.mosip.resident.dto.PacketReceiverResponseDTO;
import io.mosip.resident.dto.RegSyncResponseDTO;
import io.mosip.resident.dto.RegistrationSyncRequestDTO;
import io.mosip.resident.dto.SupervisorStatus;
import io.mosip.resident.dto.SyncRegistrationDto;
import io.mosip.resident.dto.SyncResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EncryptorUtil;
import io.mosip.resident.util.AuditEnum;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * The Class SyncUploadEncryptionServiceImpl.
 * 
 * @author Rishabh Keshari
 */
@Service
public class SyncAndUploadService {

	private static final String PACKET_RECEIVED = "PACKET_RECEIVED";

	private static final String SUCCESS = "SUCCESS";

	private static final String FAILURE = "FAILURE";

	/** The reg proc logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(SyncAndUploadService.class);

	/** The rest client service. */
	@Autowired
	private ResidentServiceRestClient restClientService;

	/** The encryptor util. */
	@Autowired
	EncryptorUtil encryptorUtil;

	/** The center id length. */
	@Value("${mosip.kernel.registrationcenterid.length}")
	private int centerIdLength;

	/** The center id length. */
	@Value("${mosip.kernel.machineid.length}")
	private int machineIdLength;
	/** The gson. */
	Gson gson = new GsonBuilder().serializeNulls().create();

	@Autowired
	private Environment env;

	@Autowired
	AuditUtil audit;
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.packet.upload.service.
	 * SyncUploadEncryptionService#uploadUinPacket(java.io.File, java.lang.String,
	 * java.lang.String)
	 */
	public PacketGeneratorResDto uploadUinPacket(String registartionId, String creationTime, String regType,
												 byte[] packetZipBytes) throws BaseCheckedException {
		PacketGeneratorResDto packerGeneratorResDto = new PacketGeneratorResDto();

		String syncStatus = "";

		try {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", "SyncUploadEncryptionServiceImpl ::uploadUinPacket()::entry");

			ByteArrayResource contentsAsResource = new ByteArrayResource(packetZipBytes) {
				@Override
				public String getFilename() {
					return registartionId + RegistrationConstants.EXTENSION_OF_FILE;
				}
			};
			RegSyncResponseDTO regSyncResponseDTO = packetSync(registartionId, regType, packetZipBytes, creationTime);

			if (regSyncResponseDTO != null) {
				List<SyncResponseDto> synList = regSyncResponseDTO.getResponse();
				if (synList != null) {
					SyncResponseDto syncResponseDto = synList.get(0);
					syncStatus = syncResponseDto.getStatus();
				}
			}
			if (SUCCESS.equalsIgnoreCase(syncStatus)) {
				logger.info(LoggerFileConstant.SESSIONID.toString(),
						LoggerFileConstant.REGISTRATIONID.toString(), registartionId,
						"Packet Generator sync successfull");
				
				PacketReceiverResponseDTO packetReceiverResponseDTO = null;
				LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
				map.add("file", contentsAsResource);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.MULTIPART_FORM_DATA);
				HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
						map, headers);

				String result = restClientService.postApi(env.getProperty(ApiName.PACKETRECEIVER.name()), MediaType.MULTIPART_FORM_DATA, requestEntity,
						String.class);
				if (result != null) {
					packetReceiverResponseDTO = gson.fromJson(result, PacketReceiverResponseDTO.class);
					logger.debug(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), registartionId,
							"SyncUploadEncryptionServiceImpl::uploadUinPacket()::Packet receiver service  call ended with response data : "
									+ JsonUtils.javaObjectToJsonString(packetReceiverResponseDTO));

					String uploadStatus = packetReceiverResponseDTO.getResponse().getStatus();
					packerGeneratorResDto.setRegistrationId(registartionId);
					
					if (uploadStatus.equalsIgnoreCase("PROCESSING")) {
						packerGeneratorResDto.setStatus(uploadStatus);
					} else if (uploadStatus.contains(PACKET_RECEIVED)) {
						packerGeneratorResDto.setStatus(SUCCESS);
						logger.info(LoggerFileConstant.SESSIONID.toString(),
								LoggerFileConstant.REGISTRATIONID.toString(), registartionId,
								"Packet Generator packet created and uploaded::success");
					} else {
						packerGeneratorResDto.setStatus(uploadStatus);
					}
					packerGeneratorResDto.setMessage("Packet created and uploaded");
					logger.info(LoggerFileConstant.SESSIONID.toString(),
							LoggerFileConstant.REGISTRATIONID.toString(), registartionId,
							packerGeneratorResDto.getMessage());
					return packerGeneratorResDto;
				}

			} else {
				packerGeneratorResDto.setRegistrationId(registartionId);
				packerGeneratorResDto.setStatus(FAILURE);
				packerGeneratorResDto.setMessage("Packet sync failure");
				audit.setAuditRequestDto(AuditEnum.PACKET_CREATED_FAILURE);
				return packerGeneratorResDto;

			}

		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId, ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode(), ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorMessage(), e);
		} catch (BaseCheckedException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId,
					ResidentErrorCode.BASE_EXCEPTION.getErrorMessage() + ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registartionId,
					ResidentErrorCode.BASE_EXCEPTION.getErrorMessage() + ExceptionUtils.getStackTrace(e));
			throw new BaseCheckedException(ResidentErrorCode.BASE_EXCEPTION.getErrorCode(), ResidentErrorCode.BASE_EXCEPTION.getErrorMessage(), e);
		}

		return packerGeneratorResDto;

	}

	/**
	 * Packet sync.
	 *
	 * @param regId
	 *            the reg id
	 * @return the reg sync response DTO
	 * @throws ApisResourceAccessException
	 */
	private RegSyncResponseDTO packetSync(String regId, String regType, byte[] enryptedUinZipFile, String creationTime)
			throws BaseCheckedException {
		RegSyncResponseDTO regSyncResponseDTO = null;
		try {
			RegistrationSyncRequestDTO registrationSyncRequestDTO = new RegistrationSyncRequestDTO();
			List<SyncRegistrationDto> syncDtoList = new ArrayList<>();
			SyncRegistrationDto syncDto = new SyncRegistrationDto();

			// Calculate HashSequense for the enryptedUinZipFile file
			// HMACUtils2.update(enryptedUinZipFile);
			String hashSequence = HMACUtils2.digestAsPlainText(enryptedUinZipFile);

			// Prepare RegistrationSyncRequestDTO
			registrationSyncRequestDTO.setId(env.getProperty(RegistrationConstants.REG_SYNC_SERVICE_ID));
			registrationSyncRequestDTO.setVersion(env.getProperty(RegistrationConstants.REG_SYNC_APPLICATION_VERSION));
			registrationSyncRequestDTO
					.setRequesttime(DateUtils.getUTCCurrentDateTimeString(env.getProperty(RegistrationConstants.DATETIME_PATTERN)));

			syncDto.setLangCode("eng");
			syncDto.setRegistrationId(regId);
			syncDto.setSyncType(regType);
			syncDto.setPacketHashValue(hashSequence);
			syncDto.setPacketSize(BigInteger.valueOf(enryptedUinZipFile.length));
			syncDto.setSupervisorStatus(SupervisorStatus.APPROVED.toString());
			syncDto.setSupervisorComment(RegistrationConstants.SYNCSTATUSCOMMENT);

			syncDtoList.add(syncDto);
			registrationSyncRequestDTO.setRequest(syncDtoList);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId,
					"SyncUploadEncryptionServiceImpl::packetSync()::Sync service call started with request data : "
							+ JsonUtils.javaObjectToJsonString(registrationSyncRequestDTO));

			String centerId = regId.substring(0, centerIdLength);
			String machineId = regId.substring(centerIdLength, centerIdLength + machineIdLength);
			String refId = centerId + "_" + machineId;

			String requestObject = encryptorUtil.encrypt(
					JsonUtils.javaObjectToJsonString(registrationSyncRequestDTO).getBytes(), refId);

			LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			headers.add("Center-Machine-RefId", refId);
			headers.add("timestamp", creationTime);

			HttpEntity<Object> requestEntity = new HttpEntity<Object>(javaObjectToJsonString(requestObject).getBytes(), headers);
			String response = (String) restClientService.postApi(env.getProperty(ApiName.SYNCSERVICE.name()), MediaType.APPLICATION_JSON, requestEntity,
					String.class);
			regSyncResponseDTO = gson.fromJson(response, RegSyncResponseDTO.class);
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					regId,
					"SyncUploadEncryptionServiceImpl::packetSync()::Sync service call ended with response data : "
							+ JsonUtils.javaObjectToJsonString(regSyncResponseDTO));

		} catch (JsonProcessingException e) {
			throw new BaseCheckedException(ResidentErrorCode.INVLAID_KEY_EXCEPTION.getErrorCode(), ResidentErrorCode.INVLAID_KEY_EXCEPTION.getErrorMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new BaseCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
					ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
		}
		return regSyncResponseDTO;
	}

}
