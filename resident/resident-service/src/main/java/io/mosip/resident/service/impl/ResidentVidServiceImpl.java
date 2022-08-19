package io.mosip.resident.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateEnum;
import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidGeneratorRequestDto;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRequestDtoV2;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidResponseDtoV2;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidAlreadyPresentException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

@Component
public class ResidentVidServiceImpl implements ResidentVidService {

	private static final String HASH_ATTRIBUTES = "hashAttributes";

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentVidServiceImpl.class);

	private static final String VID_ALREADY_EXISTS_ERROR_CODE = "IDR-VID-003";
	
	private static final String VID = "vid";
	private static final String VID_TYPE = "vidType";

	@Value("${resident.vid.id}")
	private String id;

	@Value("${resident.vid.version}")
	private String version;

	@Value("${vid.create.id}")
	private String vidCreateId;

	@Value("${vid.revoke.id}")
	private String vidRevokeId;

	@Value("${resident.revokevid.id}")
	private String revokeVidId;

	@Value("${mosip.resident.vid-policy-url}")
	private String vidPolicyUrl;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private Environment env;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private IdAuthService idAuthService;

	@Autowired
	private AuditUtil audit;
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;
	
	@Autowired
	private Utilitiy utility;
	
	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	private String vidPolicy;

	@Value("${perpatual.vid-type:PERPETUAL}")
	private String perpatualVidType;
	
	@Override
	public ResponseWrapper<VidResponseDto> generateVid(BaseVidRequestDto requestDto,
			String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException {
 
		ResponseWrapper<VidResponseDto> responseDto = new ResponseWrapper<>();
		NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
		notificationRequestDto.setId(individualId);

		if (requestDto instanceof VidRequestDto) {
			VidRequestDto vidRequestDto = (VidRequestDto) requestDto;
			if (Objects.nonNull(vidRequestDto.getOtp())) {
				try {
					boolean isAuthenticated = idAuthService.validateOtp(vidRequestDto.getTransactionID(),
							individualId, vidRequestDto.getOtp());
					if (!isAuthenticated)
						throw new OtpValidationFailedException();
	
				} catch (OtpValidationFailedException e) {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
							requestDto.getTransactionID(), "Request to generate VID"));
					notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
					notificationService.sendNotification(notificationRequestDto);
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
							requestDto.getTransactionID(), "Request to generate VID"));
	
					throw e;
				}
			}
		}

		IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);
		String email = identityDTO.getEmail();
		String phone = identityDTO.getPhone();
		ResidentTransactionEntity residentTransactionEntity=null;
		try {
			residentTransactionEntity = createResidentTransactionEntity(requestDto);
			
			String uin = identityDTO.getUIN();
			// generate vid
			VidGeneratorResponseDto vidResponse = vidGenerator(requestDto, uin);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATED, requestDto.getTransactionID()));
			// send notification
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(TemplateEnum.VID.name(), vidResponse.getVID());
			notificationRequestDto.setAdditionalAttributes(additionalAttributes);
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_SUCCESS);

			NotificationResponseDTO notificationResponseDTO;
			if(requestDto instanceof VidRequestDtoV2) {
				VidRequestDtoV2 vidRequestDtoV2 = (VidRequestDtoV2) requestDto;
				notificationResponseDTO = notificationService
					.sendNotification(notificationRequestDto, vidRequestDtoV2.getChannels(), email, phone);
			} else {
				notificationResponseDTO = notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					requestDto.getTransactionID(), "Request to generate VID"));
			// create response dto
			VidResponseDto vidResponseDto;
			if(notificationResponseDTO.getMaskedEmail() != null || notificationResponseDTO.getMaskedPhone() != null) {
				VidResponseDtoV2 vidResponseDtov2 = new VidResponseDtoV2();
				vidResponseDto = vidResponseDtov2;
				vidResponseDtov2.setMaskedEmail(notificationResponseDTO.getMaskedEmail());
				vidResponseDtov2.setMaskedPhone(notificationResponseDTO.getMaskedPhone());
			} else {
				vidResponseDto = new VidResponseDto();
			}
			vidResponseDto.setVid(vidResponse.getVID());
			vidResponseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setResponse(vidResponseDto);
			
			residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(vidResponseDto.getVid()));
			residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
			
		} catch (JsonProcessingException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,
					requestDto.getTransactionID(), "Request to generate VID"));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
			notificationService.sendNotification(notificationRequestDto, requestDto instanceof VidRequestDtoV2? ((VidRequestDtoV2)requestDto).getChannels(): null, email,  phone);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			throw new VidCreationException(e.getErrorText());
		} catch (IOException | ApisResourceAccessException | VidCreationException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATION_FAILURE, requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			throw new VidCreationException(e.getMessage());
		} catch (VidAlreadyPresentException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_ALREADY_EXISTS, requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			throw e;
		} finally {
			residentTransactionRepository.save(residentTransactionEntity);
		}

		responseDto.setId(id);
		responseDto.setVersion(version);
		responseDto.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));

		return responseDto;
	}

	private ResidentTransactionEntity createResidentTransactionEntity(BaseVidRequestDto requestDto) throws ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity=utility.createEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		residentTransactionEntity.setRequestTypeCode(RequestType.GENERATE_VID.name());
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setRequestSummary("in-progress");
		residentTransactionEntity.setRefIdType(requestDto.getVidType());
		return residentTransactionEntity;
	}

	private VidGeneratorResponseDto vidGenerator(BaseVidRequestDto requestDto, String uin)
			throws JsonProcessingException, IOException, ApisResourceAccessException {
		VidGeneratorRequestDto vidRequestDto = new VidGeneratorRequestDto();
		RequestWrapper<VidGeneratorRequestDto> request = new RequestWrapper<>();
		ResponseWrapper<VidGeneratorResponseDto> response = null;

		vidRequestDto.setUIN(uin);
		vidRequestDto.setVidType(requestDto.getVidType());
		request.setId(vidCreateId);
		request.setVersion(version);
		request.setRequest(vidRequestDto);
		request.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				IdType.UIN.name(),
				"ResidentVidServiceImpl::vidGenerator():: post CREATEVID service call started with request data : "
						+ JsonUtils.javaObjectToJsonString(request));

		try {
			response = (ResponseWrapper) residentServiceRestClient
					.postApi(env.getProperty(ApiName.IDAUTHCREATEVID.name()),
							MediaType.APPLICATION_JSON, request, ResponseWrapper.class);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					IdType.UIN.name(), ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to create vid : " + e.getMessage());
		}

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				IdType.UIN.name(),
				"ResidentVidServiceImpl::vidGenerator():: create Vid response :: "
						+ JsonUtils.javaObjectToJsonString(response));

		if (response.getErrors() != null && !response.getErrors().isEmpty()) {
			List<ServiceError> list = response.getErrors().stream()
					.filter(err -> err.getErrorCode().equalsIgnoreCase(VID_ALREADY_EXISTS_ERROR_CODE))
					.collect(Collectors.toList());
			throw (list.size() == 1)
					? new VidAlreadyPresentException(ResidentErrorCode.VID_ALREADY_PRESENT.getErrorCode(),
							ResidentErrorCode.VID_ALREADY_PRESENT.getErrorMessage())
					: new VidCreationException(response.getErrors().get(0).getMessage());

		}

		VidGeneratorResponseDto vidResponse = mapper.readValue(mapper.writeValueAsString(response.getResponse()),
				VidGeneratorResponseDto.class);

		return vidResponse;
	}

	@Override
	public ResponseWrapper<VidRevokeResponseDTO> revokeVid(BaseVidRevokeRequestDTO requestDto, String vid, String indivudalId)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<VidRevokeResponseDTO> responseDto = new ResponseWrapper<>();
		NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
		
		if(requestDto instanceof VidRevokeRequestDTO) {
			VidRevokeRequestDTO vidRevokeRequestDTO = (VidRevokeRequestDTO) requestDto;
			if (Objects.nonNull(vidRevokeRequestDTO.getOtp())) {
				try {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP,
							requestDto.getTransactionID(), "Request to revoke VID"));
					boolean isAuthenticated = idAuthService.validateOtp(requestDto.getTransactionID(),
							vidRevokeRequestDTO.getIndividualId(), vidRevokeRequestDTO.getOtp());
	
					if (!isAuthenticated)
						throw new OtpValidationFailedException();
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
							requestDto.getTransactionID(), "Request to revoke VID"));
				} catch (OtpValidationFailedException e) {
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
							requestDto.getTransactionID(), "Request to revoke VID"));
					notificationRequestDto.setId(vidRevokeRequestDTO.getIndividualId());
					notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
					notificationService.sendNotification(notificationRequestDto);
					audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
							requestDto.getTransactionID(), "Request to revoke VID"));
					throw e;
				}
			}
		}
		
		ResidentTransactionEntity residentTransactionEntity = createResidentTransEntity(vid);
		
		IdentityDTO identityDTO = identityServiceImpl.getIdentity(indivudalId);
		String uin = identityDTO.getUIN();

		notificationRequestDto.setId(uin);
		
		if(requestDto instanceof VidRevokeRequestDTOV2) {
			String idaTokenForIndividualId = identityServiceImpl.getResidentIdaToken();
			String idaTokenForVid = identityServiceImpl.getIDATokenForIndividualId(vid);
			if(idaTokenForVid == null || !idaTokenForIndividualId.equalsIgnoreCase(idaTokenForVid)) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionRepository.save(residentTransactionEntity);
				
				throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_SESSION);
			}
		} else {
			String uinForVid = identityServiceImpl.getUinForIndividualId(vid);
			if(uinForVid == null || !uinForVid.equalsIgnoreCase(uin)) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				residentTransactionRepository.save(residentTransactionEntity);
				
				throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_INDIVITUAL);
			}
		}
		
		try {

			// revoke vid
			VidGeneratorResponseDto vidResponse = vidDeactivator(requestDto, uin, vid);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.DEACTIVATED_VID, requestDto.getTransactionID()));
			// send notification
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(TemplateEnum.VID.name(), vid);
			notificationRequestDto.setAdditionalAttributes(additionalAttributes);
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_SUCCESS);

			NotificationResponseDTO notificationResponseDTO = notificationService
					.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					requestDto.getTransactionID(), "Request to revoke VID"));
			// create response dto
			VidRevokeResponseDTO vidRevokeResponseDto = new VidRevokeResponseDTO();
			vidRevokeResponseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setResponse(vidRevokeResponseDto);
			
			residentTransactionEntity.setStatusCode(EventStatusInProgress.NEW.name());
			
		} catch (JsonProcessingException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,
					requestDto.getTransactionID(), "Request to revoke VID"));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			throw new VidRevocationException(e.getErrorText());
		} catch (IOException | ApisResourceAccessException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION, requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			throw new VidRevocationException(e.getMessage());
		} catch (VidRevocationException e) {
			residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
			
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION, requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			throw e;
		} finally {
			residentTransactionRepository.save(residentTransactionEntity);
		}

		responseDto.setId(revokeVidId);
		responseDto.setVersion(version);
		responseDto.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));

		return responseDto;
	}

	private ResidentTransactionEntity createResidentTransEntity(String vid) throws ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity=utility.createEntity();
		residentTransactionEntity.setEventId(UUID.randomUUID().toString());
		residentTransactionEntity.setRequestTypeCode(RequestType.REVOKE_VID.name());
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(vid));
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setRequestSummary("in-progress");
		return residentTransactionEntity;
	}

	private VidGeneratorResponseDto vidDeactivator(BaseVidRevokeRequestDTO requestDto, String uin, String vid)
			throws JsonProcessingException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {
		VidGeneratorRequestDto vidRequestDto = new VidGeneratorRequestDto();
		RequestWrapper<VidGeneratorRequestDto> request = new RequestWrapper<>();
		ResponseWrapper<?> response = null;

		vidRequestDto.setUIN(uin);
		vidRequestDto.setVidStatus(requestDto.getVidStatus());
		request.setId(vidRevokeId);
		request.setVersion(version);
		request.setRequest(vidRequestDto);
		request.setRequesttime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		String apiUrl=env.getProperty(ApiName.IDAUTHREVOKEVID.name()) + "/" + vid;
		URI apiUri=URI.create(apiUrl);

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				"",
				"ResidentVidServiceImpl::vidDeactivator():: post REVOKEVID service call started with request data : "
						+ JsonUtils.javaObjectToJsonString(request));

		try {
			response = (ResponseWrapper) residentServiceRestClient.patchApi(apiUri, MediaType.APPLICATION_JSON, request,
					ResponseWrapper.class);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to revoke VID : " + e.getMessage());
		}

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				"", "ResidentVidServiceImpl::vidDeactivator():: revoke Vid response :: "
						+ JsonUtils.javaObjectToJsonString(response));

		if (response == null || response.getErrors() != null && !response.getErrors().isEmpty()) {
			throw new VidRevocationException(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorMessage());

		}

		VidGeneratorResponseDto vidResponse = mapper.convertValue(response.getResponse(), VidGeneratorResponseDto.class);

		return vidResponse;

	}

	/**
	 * The function is used to fetch the VID policy from the URL and store
	 * return it.
	 * 
	 * @return The vidPolicy is being returned.
	 */
	@Override
	@PostConstruct
	public String getVidPolicy() throws ResidentServiceCheckedException {
		if (Objects.isNull(vidPolicy)) {
			try {
				JsonNode policyJson = mapper.readValue(new URL(vidPolicyUrl), JsonNode.class);
				vidPolicy = policyJson.toString();
			} catch (IOException e) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
						"GetVidPolicy",
						ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode() + ExceptionUtils.getStackTrace(e));
				throw new ResidentServiceCheckedException(ResidentErrorCode.POLICY_EXCEPTION.getErrorCode(),
						ResidentErrorCode.POLICY_EXCEPTION.getErrorMessage(), e);
			}
		}
		return vidPolicy;
	}

	@Override
	public ResponseWrapper<List<Map<String,?>>> retrieveVids(String residentIndividualId) throws ResidentServiceCheckedException, ApisResourceAccessException {
		IdentityDTO identityDTO = identityServiceImpl.getIdentity(residentIndividualId);
		String uin = identityDTO.getUIN();
		ResponseWrapper response;
		
		try {
			response = (ResponseWrapper) residentServiceRestClient.getApi(
					env.getProperty(ApiName.RETRIEVE_VIDS.name()) + uin, ResponseWrapper.class);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					residentIndividualId, ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to retrieve VID : " + e.getMessage());
		}
		
		List<Map<String, ?>> filteredList = ((List<Map<String, ?>>) response.getResponse()).stream()
				.map(map -> new LinkedHashMap<String, Object>(map))
				.map(lhm -> {
					lhm.remove(HASH_ATTRIBUTES);
					return lhm;
				}).collect(Collectors.toList());
		ResponseWrapper<List<Map<String, ?>>> res = new ResponseWrapper<List<Map<String, ?>>>();
		res.setResponse(filteredList);
		return res;
		
	}
	
	public Optional<String> getPerpatualVid(String uin) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<List<Map<String, ?>>> vidResp = retrieveVids(uin);
		List<Map<String, ?>> vids = vidResp.getResponse();
		if(vids != null && !vids.isEmpty()) {
			return vids.stream()
				.filter(map -> map.containsKey(VID_TYPE) && 
							perpatualVidType.equalsIgnoreCase(String.valueOf(map.get(VID_TYPE))))
				.map(map -> String.valueOf( map.get(VID)))
				.findAny();
		}
		return Optional.empty();
	}
	
}
