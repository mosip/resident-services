package io.mosip.resident.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.idrepository.core.dto.VidPolicy;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateEnum;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.constant.TemplateVariablesConstants;
import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.GenerateVidResponseDto;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.RevokeVidResponseDto;
import io.mosip.resident.dto.VidGeneratorRequestDto;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidRequestDtoV2;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeRequestDTOV2;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
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
import io.mosip.resident.util.Utility;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentConstants.VID_POLICIES;
import static io.mosip.resident.constant.ResidentConstants.VID_POLICY;

@Component
public class ResidentVidServiceImpl implements ResidentVidService {

	private static final String GENRATED_ON_TIMESTAMP = "genratedOnTimestamp";
	
	private static final String EXPIRY_TIMESTAMP = "expiryTimestamp";

	private static final String TRANSACTIONS_LEFT_COUNT = "transactionsLeftCount";

	private static final String TRANSACTION_LIMIT = "transactionLimit";

	private static final String MASKED_VID = "maskedVid";

	private static final String HASH_ATTRIBUTES = "hashAttributes";

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentVidServiceImpl.class);

	private static final String VID_ALREADY_EXISTS_ERROR_CODE = "IDR-VID-003";
	
	private static final String VID = "vid";
	private static final String VID_TYPE = "vidType";

	@Value("${resident.vid.id}")
	private String id;
	
	@Value("${resident.vid.id.generate}")
	private String generateId;

	@Value("${resident.vid.version}")
	private String version;

	@Value("${resident.vid.version.new}")
	private String newVersion;

	@Value("${vid.create.id}")
	private String vidCreateId;

	@Value("${vid.revoke.id}")
	private String vidRevokeId;

	@Value("${resident.revokevid.id}")
	private String revokeVidId;
	
	@Value("${mosip.resident.revokevid.id}")
	private String revokeVidIdNew;

	@Value("${mosip.resident.vid-policy-url}")
	private String vidPolicyUrl;
	
	@Value("${resident.vid.get.id}")
	private String residentVidGetId;
	
	@Value("${mosip.resident.create.vid.version}")
	private String residentCreateVidVersion;

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
	private Utility utility;
	
	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;

	private String vidPolicy;

	@Value("${perpatual.vid-type:PERPETUAL}")
	private String perpatualVidType;
	
	@Override
	public ResponseWrapper<VidResponseDto> generateVid(BaseVidRequestDto requestDto,
			String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException {
		return generateVidV2(requestDto, individualId).getT1();
	}
	
	@Override
	public Tuple2<ResponseWrapper<VidResponseDto>, String> generateVidV2(BaseVidRequestDto requestDto,
			String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException {
		boolean isV2Request = requestDto instanceof VidRequestDtoV2;
		ResponseWrapper<VidResponseDto> responseDto = new ResponseWrapper<>();
		NotificationRequestDto notificationRequestDto = isV2Request? new NotificationRequestDtoV2() : new NotificationRequestDto();
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
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity=null;
		try {
			if(Utility.isSecureSession()){
				residentTransactionEntity = createResidentTransactionEntity(requestDto);
				validateVidFromSession(individualId, requestDto.getVidType());
				if (residentTransactionEntity != null) {
	    			eventId = residentTransactionEntity.getEventId();
	    		}
			}
			String uin = identityDTO.getUIN();
			// generate vid
			VidGeneratorResponseDto vidResponse = vidGenerator(requestDto, uin);
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATED, requestDto.getTransactionID()));
			// send notification
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(TemplateEnum.VID.name(), vidResponse.getVID());
			notificationRequestDto.setAdditionalAttributes(additionalAttributes);

			NotificationResponseDTO notificationResponseDTO;
			if(isV2Request) {
				VidRequestDtoV2 vidRequestDtoV2 = (VidRequestDtoV2) requestDto;
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.SUCCESS);
				notificationRequestDtoV2.setRequestType(RequestType.GENERATE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationResponseDTO=notificationService
						.sendNotification(notificationRequestDto, vidRequestDtoV2.getChannels(), email, phone);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_SUCCESS);
				notificationResponseDTO = notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					requestDto.getTransactionID(), "Request to generate VID"));
			// create response dto
			VidResponseDto vidResponseDto;
			if(notificationResponseDTO.getMaskedEmail() != null || notificationResponseDTO.getMaskedPhone() != null) {
				GenerateVidResponseDto generateVidResponseDto = new GenerateVidResponseDto();
				vidResponseDto = generateVidResponseDto;
				generateVidResponseDto.setMaskedEmail(notificationResponseDTO.getMaskedEmail());
				generateVidResponseDto.setMaskedPhone(notificationResponseDTO.getMaskedPhone());
				generateVidResponseDto.setStatus(ResidentConstants.SUCCESS);
			} else {
				vidResponseDto = new VidResponseDto();
			}
			vidResponseDto.setVid(vidResponse.getVID());
			vidResponseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setResponse(vidResponseDto);

			if(Utility.isSecureSession()) {
				residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(vidResponseDto.getVid()));
				residentTransactionEntity.setStatusCode(EventStatusSuccess.VID_GENERATED.name());
				residentTransactionEntity.setStatusComment(EventStatusSuccess.VID_GENERATED.name());
			}
			
		} catch (JsonProcessingException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,
					requestDto.getTransactionID(), "Request to generate VID"));
			if(isV2Request) {
				VidRequestDtoV2 vidRequestDtoV2 = (VidRequestDtoV2) requestDto;
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.GENERATE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto, vidRequestDtoV2.getChannels(), email, phone);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidCreationException(e.getErrorText(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new VidCreationException(e.getErrorText());
			}
		} catch (IOException | ApisResourceAccessException | VidCreationException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATION_FAILURE, requestDto.getTransactionID()));
			if(isV2Request) {
				VidRequestDtoV2 vidRequestDtoV2 = (VidRequestDtoV2) requestDto;
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.GENERATE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto, vidRequestDtoV2.getChannels(), email, phone);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidCreationException(e.getMessage(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new VidCreationException(e.getMessage());
			}
		} catch (VidAlreadyPresentException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_ALREADY_EXISTS, requestDto.getTransactionID()));
			if(isV2Request) {
				VidRequestDtoV2 vidRequestDtoV2 = (VidRequestDtoV2) requestDto;
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.GENERATE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto, vidRequestDtoV2.getChannels(), email, phone);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to generate VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidAlreadyPresentException(e.getMessage(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw e;
			}
		} finally {
			if (Utility.isSecureSession() && residentTransactionEntity != null) {
				//if the status code will come as null, it will set it as failed.
				if(residentTransactionEntity.getStatusCode()==null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary("failed");
				}
				residentTransactionRepository.save(residentTransactionEntity);
			}
		}
		if (isV2Request)
		{
			responseDto.setId(generateId);
			responseDto.setVersion(newVersion);
		}
		else
		{
			responseDto.setId(id);
			responseDto.setVersion(version);
		}
		responseDto.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));
		return Tuples.of(responseDto, eventId);
	}

	private void validateVidFromSession(String individualId, String vidType) {
		try {
			String idType = identityServiceImpl.getIndividualIdType(individualId);
			String uin = identityServiceImpl.getUinForIndividualId(individualId);
			Tuple2<Integer, String> numberOfPerpetualVidTuple = getNumberOfPerpetualVidFromUin(uin);
			/**
			 * Check If id type is VID.
			 */
			if (idType.equalsIgnoreCase(IdType.VID.name())) {
				/**
				 * Checks if VID type is Perpetual VID.
				 */
				if(vidType!=null && vidType.equalsIgnoreCase(ResidentConstants.PERPETUAL)){
					int numberOfPerpetualVid = numberOfPerpetualVidTuple.getT1();
					VidPolicy vidPolicy = getVidPolicyAsVidPolicyDto();
					/**
					 * Checks if VID Policy allowed instance is greater than number of existing Perpetual VID.
					 */
					if(vidPolicy.getAllowedInstances() >= numberOfPerpetualVid
							/**
							 * Checks if VID Policy auto restore allowed is true.
							 */
							&& vidPolicy.getAutoRestoreAllowed()
							/**
							 * Checks if VID Policy restore on action is not ACTIVE.
							 */
							&& !vidPolicy.getRestoreOnAction().
									equalsIgnoreCase(env.getProperty(ResidentConstants.VID_ACTIVE_STATUS))
							/**
							 * Checks if first Perpetual Vid is equal to logged in vid.
							 */
					&& numberOfPerpetualVidTuple.getT2().equals(individualId)){
						throw new ResidentServiceException(ResidentErrorCode.VID_CREATION_FAILED_WITH_REVOCATION,
									ResidentErrorCode.VID_CREATION_FAILED_WITH_REVOCATION.getErrorMessage());
					}
				}
			}
		}catch (ApisResourceAccessException | ResidentServiceCheckedException |
				com.fasterxml.jackson.core.JsonProcessingException | ResidentServiceException e) {
			audit.setAuditRequestDto(EventEnum.VID_GENERATION_FAILURE);
			logger.error(EventEnum.VID_GENERATION_FAILURE.getDescription() + e);
			throw new ResidentServiceException(ResidentErrorCode.VID_CREATION_FAILED_WITH_REVOCATION.getErrorCode(),
					ResidentErrorCode.VID_CREATION_FAILED_WITH_REVOCATION.getErrorMessage(), e);
		}
	}

	private VidPolicy getVidPolicyAsVidPolicyDto() throws ResidentServiceCheckedException, com.fasterxml.jackson.core.JsonProcessingException {
		String vidPolicy = getVidPolicy();
		VidPolicy vidPolicyDto = new VidPolicy();
		Map<Object, Object> vidPolicyMap = mapper.readValue(vidPolicy, Map.class);
		Object vidPolicyMapValue = vidPolicyMap.get(VID_POLICIES);
		List<Map<String, String>> vidList = (List<Map<String, String>>)vidPolicyMapValue;
		if(vidList!=null){
			for(Map<String, String> vid:vidList){
				if(vid.get(TemplateVariablesConstants.VID_TYPE).equalsIgnoreCase(ResidentConstants.PERPETUAL)){
					vidPolicyDto = mapper.convertValue(vid.get(VID_POLICY), VidPolicy.class);
				}
			}
		}
		return vidPolicyDto;
	}

	private Tuple2<Integer, String> getNumberOfPerpetualVidFromUin(String individualId) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<List<Map<String,?>>> vids = retrieveVids(individualId , ResidentConstants.UTC_TIMEZONE_OFFSET);
		List<Map<String, ?>> vidList = vids.getResponse().stream().filter(map -> map.containsKey(TemplateVariablesConstants.VID_TYPE)
		&& String.valueOf(map.get(TemplateVariablesConstants.VID_TYPE)).equalsIgnoreCase((ResidentConstants.PERPETUAL)))
				.collect(Collectors.toList());
		if(vidList.isEmpty()){
			return Tuples.of(0, "");
		}
		return Tuples.of(vidList.size(), vidList.get(0).get(TemplateVariablesConstants.VID).toString());
	}

	private ResidentTransactionEntity createResidentTransactionEntity(BaseVidRequestDto requestDto) throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity=utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.GENERATE_VID.name());
		residentTransactionEntity.setIndividualId(identityServiceImpl.getResidentIndvidualId());
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setRequestSummary(EventStatusSuccess.VID_GENERATED.name());
		residentTransactionEntity.setRefIdType(requestDto.getVidType().toUpperCase());
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
		request.setVersion(residentCreateVidVersion);
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
		return revokeVidV2(requestDto, vid, indivudalId).getT1();
	}

	@Override
	public Tuple2<ResponseWrapper<VidRevokeResponseDTO>, String> revokeVidV2(BaseVidRevokeRequestDTO requestDto, String vid, String indivudalId)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException {
		boolean isV2Request = requestDto instanceof VidRevokeRequestDTOV2;
		ResponseWrapper<VidRevokeResponseDTO> responseDto = new ResponseWrapper<>();
		NotificationRequestDto notificationRequestDto = isV2Request? new NotificationRequestDtoV2() : new NotificationRequestDto();
		
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
		String eventId = ResidentConstants.NOT_AVAILABLE;
		ResidentTransactionEntity residentTransactionEntity = null;
		if(Utility.isSecureSession()) {
			residentTransactionEntity = createResidentTransEntity(vid, indivudalId);
			if (residentTransactionEntity != null) {
				eventId = residentTransactionEntity.getEventId();
			}
		}
		IdentityDTO identityDTO = new IdentityDTO();
		String uin="";
		if(isV2Request) {
			try {
				identityDTO = identityServiceImpl.getIdentity(indivudalId);
			}catch (Exception e){
				throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_SESSION,
						Map.of(ResidentConstants.EVENT_ID, eventId));
			}
			uin = identityDTO.getUIN();
			notificationRequestDto.setId(uin);
			String idaTokenForIndividualId = identityServiceImpl.getResidentIdaToken();
			String idaTokenForVid = identityServiceImpl.getIDATokenForIndividualId(vid);
			if(idaTokenForVid == null || !idaTokenForIndividualId.equalsIgnoreCase(idaTokenForVid)) {
				if(Utility.isSecureSession()) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionRepository.save(residentTransactionEntity);
					throw new ResidentServiceCheckedException(ResidentErrorCode.VID_NOT_BELONG_TO_SESSION,
							Map.of(ResidentConstants.EVENT_ID, eventId));
				}
			}
		} else {
			String uinForVid;
			try {
				uinForVid = identityServiceImpl.getUinForIndividualId(vid);
			}catch (Exception exception){
				logger.error(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode()+exception);
				throw new ApisResourceAccessException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
						ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), exception);
			}
			identityDTO = identityServiceImpl.getIdentity(indivudalId);
			uin = identityDTO.getUIN();
			notificationRequestDto.setId(uin);
			if(uinForVid == null || !uinForVid.equalsIgnoreCase(uin)) {
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

			NotificationResponseDTO notificationResponseDTO;
			if(isV2Request) {
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.SUCCESS);
				notificationRequestDtoV2.setRequestType(RequestType.REVOKE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationResponseDTO=notificationService.sendNotification(notificationRequestDto);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_SUCCESS);
				notificationResponseDTO = notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,
					requestDto.getTransactionID(), "Request to revoke VID"));
			// create response dto
			VidRevokeResponseDTO vidRevokeResponseDto;
			if(isV2Request) {
				RevokeVidResponseDto revokeVidResponseDto = new RevokeVidResponseDto();
				vidRevokeResponseDto = revokeVidResponseDto;
				revokeVidResponseDto.setStatus(ResidentConstants.SUCCESS);
			} else {
				vidRevokeResponseDto = new VidRevokeResponseDTO();
			}
			vidRevokeResponseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setResponse(vidRevokeResponseDto);
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusSuccess.VID_REVOKED.name());
				residentTransactionEntity.setStatusComment(EventStatusSuccess.VID_REVOKED.name());
			}
		} catch (JsonProcessingException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,
					requestDto.getTransactionID(), "Request to revoke VID"));
			if(isV2Request) {
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.REVOKE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidRevocationException(e.getErrorText(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new VidRevocationException(e.getErrorText());
			}
		} catch (IOException | ApisResourceAccessException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION, requestDto.getTransactionID()));
			if(isV2Request) {
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.REVOKE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidRevocationException(e.getMessage(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw new VidRevocationException(e.getMessage());
			}
		} catch (VidRevocationException e) {
			audit.setAuditRequestDto(
					EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION, requestDto.getTransactionID()));
			if(isV2Request) {
				NotificationRequestDtoV2 notificationRequestDtoV2=(NotificationRequestDtoV2) notificationRequestDto;
				notificationRequestDtoV2.setTemplateType(TemplateType.FAILURE);
				notificationRequestDtoV2.setRequestType(RequestType.REVOKE_VID);
				notificationRequestDtoV2.setEventId(eventId);

				notificationService.sendNotification(notificationRequestDto);
			} else {
				notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
				notificationService.sendNotification(notificationRequestDto);
			}
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,
					requestDto.getTransactionID(), "Request to revoke VID"));
			if(Utility.isSecureSession()) {
				residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
				throw new VidRevocationException(e.getMessage(), e, Map.of(ResidentConstants.EVENT_ID, eventId));
			} else {
				throw e;
			}
		} finally {
			if (Utility.isSecureSession() && residentTransactionEntity != null) {
				//if the status code will come as null, it will set it as failed.
				if(residentTransactionEntity.getStatusCode()==null) {
					residentTransactionEntity.setStatusCode(EventStatusFailure.FAILED.name());
					residentTransactionEntity.setRequestSummary("failed");
				}
				residentTransactionRepository.save(residentTransactionEntity);
			}
		}

		if (isV2Request) {
			responseDto.setId(revokeVidIdNew);
			responseDto.setVersion(newVersion);
		}
		else
		{
			responseDto.setId(revokeVidId);
			responseDto.setVersion(version);
		}
		responseDto.setResponsetime(DateUtils.formatToISOString(DateUtils.getUTCCurrentDateTime()));

		return Tuples.of(responseDto, eventId);
	}

	private ResidentTransactionEntity createResidentTransEntity(String vid, String indivudalId) throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity=utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.REVOKE_VID.name());
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(vid));
		residentTransactionEntity.setIndividualId(identityServiceImpl.getResidentIndvidualId());
		try {
			residentTransactionEntity.setRefIdType(getVidTypeFromVid(vid, indivudalId));
		} catch (Exception exception){
			residentTransactionEntity.setRefIdType("");
		}
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setRequestSummary(EventStatusSuccess.VID_REVOKED.name());
		return residentTransactionEntity;
	}

	private String getVidTypeFromVid(String vid, String indivudalId) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<List<Map<String,?>>> vids = retrieveVids(indivudalId, ResidentConstants.UTC_TIMEZONE_OFFSET);
		return vids.getResponse().stream()
				.filter(map -> ((String)map.get(TemplateVariablesConstants.VID)).equals(vid))
				.map(map -> (String)map.get(TemplateVariablesConstants.VID_TYPE))
				.findAny()
				.orElse(null);
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
		
		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				"",
				"ResidentVidServiceImpl::vidDeactivator():: post REVOKEVID service call started with request data : "
						+ JsonUtils.javaObjectToJsonString(request));

		try {
			response = (ResponseWrapper) residentServiceRestClient.patchApi(apiUrl, MediaType.APPLICATION_JSON, request,
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
	public ResponseWrapper<List<Map<String,?>>> retrieveVids(String residentIndividualId, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException {
		IdentityDTO identityDTO = identityServiceImpl.getIdentity(residentIndividualId);
		String uin = identityDTO.getUIN();
		return retrieveVidsfromUin(uin, timeZoneOffset);
		}

	@Override
	public ResponseWrapper<List<Map<String,?>>> retrieveVidsfromUin(String uin, int timeZoneOffset) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper response;
		try {
			response = (ResponseWrapper) residentServiceRestClient.getApi(
					env.getProperty(ApiName.RETRIEVE_VIDS.name()) + uin, ResponseWrapper.class);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					uin, ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to retrieve VID : " + e.getMessage());
		}
		
		List<Map<String, ?>> filteredList = ((List<Map<String, ?>>) response.getResponse()).stream()
				.map(map -> {
					LinkedHashMap<String, Object> lhm = new LinkedHashMap<String, Object>(map);
					getMaskedVid(lhm);
					getRefIdHash(lhm);
					normalizeTime(EXPIRY_TIMESTAMP, lhm, timeZoneOffset);
					normalizeTime(GENRATED_ON_TIMESTAMP, lhm, timeZoneOffset);
					return lhm;
				})
				.collect(Collectors.toList());
		ResponseWrapper<List<Map<String, ?>>> res = new ResponseWrapper<List<Map<String, ?>>>();
		res.setId(residentVidGetId);
		res.setVersion(newVersion);
		res.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
		res.setResponse(filteredList);
		return res;
		
	}
	
	private void normalizeTime(String attributeName, LinkedHashMap<String, Object> lhm, int timeZoneOffset) {
		Object timeObject = lhm.get(attributeName);
		if(timeObject instanceof String) {
			String timeStr = String.valueOf(timeObject);
			LocalDateTime localDateTime = mapper.convertValue(timeStr, LocalDateTime.class);
			//For the big expiry time, assume no expiry time, so set to null
			if(localDateTime.getYear() >= 9999) {
				lhm.put(attributeName, null);
			} else {
				lhm.put(attributeName, utility.formatWithOffsetForUI(timeZoneOffset, localDateTime)) ;
			}
		}
	}

	private Map<String, Object> getMaskedVid(Map<String, Object> map) {
		String maskedvid = utility.convertToMaskDataFormat(map.get(VID).toString());
		map.put(MASKED_VID, maskedvid);
		return map;
	}

	private Map<String, Object> getRefIdHash(Map<String, Object> map) {
		try {
			String hashrefid = HMACUtils2.digestAsPlainText(map.get(VID).toString().getBytes());
			int countdb = residentTransactionRepository.findByrefIdandauthtype(hashrefid);
			if(map.get(TRANSACTION_LIMIT) != null) {
				int limitCount =  (int) map.get(TRANSACTION_LIMIT);
				int leftcount = limitCount - countdb;
			    map.put(TRANSACTIONS_LEFT_COUNT, leftcount);
			    if(leftcount < 0) {
			    	map.put(TRANSACTIONS_LEFT_COUNT, 0);
			    }
			}else  {
				map.put(TRANSACTIONS_LEFT_COUNT, map.get(TRANSACTION_LIMIT));	
			}
			map.remove(HASH_ATTRIBUTES);
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException", ExceptionUtils.getStackTrace(e));
			logger.error("In getRefIdHash method of ResidentVidServiceImpl class", e.getMessage());
		}
		return map;
	}	
	
	public Optional<String> getPerpatualVid(String uin) throws ResidentServiceCheckedException, ApisResourceAccessException {
		ResponseWrapper<List<Map<String, ?>>> vidResp = retrieveVidsfromUin(uin, ResidentConstants.UTC_TIMEZONE_OFFSET);
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
