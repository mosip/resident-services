package io.mosip.resident.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.NotificationTemplateCode;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.TemplateEnum;
import io.mosip.resident.dto.NotificationRequestDto;
import io.mosip.resident.dto.NotificationResponseDTO;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidGeneratorRequestDto;
import io.mosip.resident.dto.VidGeneratorResponseDto;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.DataNotFoundException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.InvalidInputException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.VidAlreadyPresentException;
import io.mosip.resident.exception.VidCreationException;
import io.mosip.resident.exception.VidRevocationException;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ResidentVidService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilitiy;

@Component
public class ResidentVidServiceImpl implements ResidentVidService {

    private static final Logger logger = LoggerConfiguration.logConfig(ResidentVidServiceImpl.class);

    private static final String VID_ALREADY_EXISTS_ERROR_CODE = "IDR-VID-003";

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
    private Utilitiy utilitiy;
    
    @Autowired
    private AuditUtil audit;

    @Override
    public ResponseWrapper<VidResponseDto> generateVid(VidRequestDto requestDto) throws OtpValidationFailedException, ResidentServiceCheckedException {

        ResponseWrapper<VidResponseDto> responseDto = new ResponseWrapper<>();
        NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
        notificationRequestDto.setId(requestDto.getIndividualId());

        try {
            boolean isAuthenticated = idAuthService.validateOtp(requestDto.getTransactionID(),
   					requestDto.getIndividualId(), requestDto.getOtp());
            if (!isAuthenticated)
                throw new OtpValidationFailedException();

        } catch (OtpValidationFailedException e) {
        	audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,requestDto.getTransactionID(),"Request to generate VID"));
            notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
            notificationService.sendNotification(notificationRequestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(),"Request to generate VID"));
            
            throw e;
        }

        try {
            // generate vid
            VidGeneratorResponseDto vidResponse = vidGenerator(requestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATED,requestDto.getTransactionID()));
            // send notification
            Map<String, Object> additionalAttributes = new HashMap<>();
            additionalAttributes.put(TemplateEnum.VID.name(), vidResponse.getVID());
            notificationRequestDto.setAdditionalAttributes(additionalAttributes);
            notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_SUCCESS);

            NotificationResponseDTO notificationResponseDTO = notificationService.sendNotification(notificationRequestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,requestDto.getTransactionID(), "Request to generate VID"));
            // create response dto
            VidResponseDto vidResponseDto = new VidResponseDto();
            vidResponseDto.setVid(vidResponse.getVID());
            vidResponseDto.setMessage(notificationResponseDTO.getMessage());
            responseDto.setResponse(vidResponseDto);
        } catch (JsonProcessingException e) {
        	audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,requestDto.getTransactionID(),"Request to generate VID"));
            notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
            notificationService.sendNotification(notificationRequestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(), "Request to generate VID"));
            throw new VidCreationException(e.getErrorText());
        } catch (IOException | ApisResourceAccessException | VidCreationException e) {
        	audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_GENERATION_FAILURE,requestDto.getTransactionID()));
            notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
            notificationService.sendNotification(notificationRequestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(), "Request to generate VID"));
            throw new VidCreationException(e.getMessage());
        } catch (VidAlreadyPresentException e) {
        	audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_ALREADY_EXISTS,requestDto.getTransactionID()));
            notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_GEN_FAILURE);
            notificationService.sendNotification(notificationRequestDto);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(), "Request to generate VID"));
            throw e;
        }

        responseDto.setId(id);
        responseDto.setVersion(version);
        responseDto.setResponsetime(DateUtils.formatToISOString(LocalDateTime.now()));

        return responseDto;
    }

    private VidGeneratorResponseDto vidGenerator(VidRequestDto requestDto)
            throws JsonProcessingException, IOException, ApisResourceAccessException {
        VidGeneratorRequestDto vidRequestDto = new VidGeneratorRequestDto();
        RequestWrapper<VidGeneratorRequestDto> request = new RequestWrapper<>();
        ResponseWrapper<VidGeneratorResponseDto> response = null;

        vidRequestDto.setUIN(requestDto.getIndividualId());
        vidRequestDto.setVidType(requestDto.getVidType());
        request.setId(vidCreateId);
        request.setVersion(version);
        request.setRequest(vidRequestDto);
        request.setRequesttime(DateUtils.formatToISOString(LocalDateTime.now()));

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
                "ResidentVidServiceImpl::vidGenerator():: create Vid response :: " + JsonUtils.javaObjectToJsonString(response));

        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            List<ServiceError> list = response.getErrors().stream().filter(err -> err.getErrorCode().equalsIgnoreCase(VID_ALREADY_EXISTS_ERROR_CODE)).collect(Collectors.toList());
            throw (list.size() == 1) ?
                    new VidAlreadyPresentException(ResidentErrorCode.VID_ALREADY_PRESENT.getErrorCode(),
                            ResidentErrorCode.VID_ALREADY_PRESENT.getErrorMessage())
                    :
                    new VidCreationException(response.getErrors().get(0).getMessage());

        }

        VidGeneratorResponseDto vidResponse = mapper.readValue(mapper.writeValueAsString(response.getResponse()),
                VidGeneratorResponseDto.class);

        return vidResponse;
    }
    
    
    @Override
	public ResponseWrapper<VidRevokeResponseDTO> revokeVid(VidRevokeRequestDTO requestDto, String vid)
			throws OtpValidationFailedException, ResidentServiceCheckedException {

		if (!requestDto.getIndividualId().equals(vid)) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.INPUT_INVALID, "individualId", "Request to revoke VID"));
			throw new InvalidInputException("The VID in the URL and body mismatched");
		}

		ResponseWrapper<VidRevokeResponseDTO> responseDto = new ResponseWrapper<>();

		NotificationRequestDto notificationRequestDto = new NotificationRequestDto();

		try {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP,requestDto.getTransactionID() ,"Request to revoke VID"));
			boolean isAuthenticated = idAuthService.validateOtp(requestDto.getTransactionID(), requestDto.getIndividualId(),
					requestDto.getOtp());

			if (!isAuthenticated)
				throw new OtpValidationFailedException();
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,requestDto.getTransactionID() ,"Request to revoke VID"));
		} catch (OtpValidationFailedException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,requestDto.getTransactionID() ,"Request to revoke VID"));
			notificationRequestDto.setId(requestDto.getIndividualId());
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID() ,"Request to revoke VID"));
			throw e;
		}

		String uin = null;
		try {
			JSONObject jsonObject = utilitiy.retrieveIdrepoJson(requestDto.getIndividualId());
			uin = JsonUtil.getJSONValue(jsonObject, IdType.UIN.name());
		} catch (IdRepoAppException e) {
			throw new DataNotFoundException(e.getErrorCode(), e.getMessage());
		}

		notificationRequestDto.setId(uin);
		
		try {

			// revoke vid
			VidGeneratorResponseDto vidResponse = vidDeactivator(requestDto, uin, vid);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.DEACTIVATED_VID,requestDto.getTransactionID()));
			// send notification
			Map<String, Object> additionalAttributes = new HashMap<>();
			additionalAttributes.put(TemplateEnum.VID.name(), vid);
			notificationRequestDto.setAdditionalAttributes(additionalAttributes);
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_SUCCESS);

			NotificationResponseDTO notificationResponseDTO = notificationService
					.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_SUCCESS,requestDto.getTransactionID(),"Request to revoke VID"));
			// create response dto
			VidRevokeResponseDTO vidRevokeResponseDto = new VidRevokeResponseDTO();
			vidRevokeResponseDto.setMessage(notificationResponseDTO.getMessage());
			responseDto.setResponse(vidRevokeResponseDto);
		} catch (JsonProcessingException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_JSON_PARSING_EXCEPTION,requestDto.getTransactionID(),"Request to revoke VID"));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(),"Request to revoke VID"));
			throw new VidRevocationException(e.getErrorText());
		} catch (IOException | ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION,requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(),"Request to revoke VID"));
			throw new VidRevocationException(e.getMessage());
		} catch ( VidRevocationException e) {
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VID_REVOKE_EXCEPTION,requestDto.getTransactionID()));
			notificationRequestDto.setTemplateTypeCode(NotificationTemplateCode.RS_VIN_REV_FAILURE);
			notificationService.sendNotification(notificationRequestDto);
			audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_NOTIFICATION_FAILURE,requestDto.getTransactionID(),"Request to revoke VID"));
			throw e;
		}

		responseDto.setId(revokeVidId);
		responseDto.setVersion(version);
		responseDto.setResponsetime(DateUtils.formatToISOString(LocalDateTime.now()));

		return responseDto;
	}

	private VidGeneratorResponseDto vidDeactivator(VidRevokeRequestDTO requestDto, String uin, String vid)
			throws JsonProcessingException, IOException, ApisResourceAccessException, ResidentServiceCheckedException {
		VidGeneratorRequestDto vidRequestDto = new VidGeneratorRequestDto();
		RequestWrapper<VidGeneratorRequestDto> request = new RequestWrapper<>();
		ResponseWrapper<VidGeneratorResponseDto> response = null;

		vidRequestDto.setUIN(uin);
		vidRequestDto.setVidStatus(requestDto.getVidStatus());
		request.setId(vidRevokeId);
		request.setVersion(version);
		request.setRequest(vidRequestDto);
		request.setRequesttime(DateUtils.formatToISOString(LocalDateTime.now()));

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				requestDto.getIndividualIdType(),
				"ResidentVidServiceImpl::vidDeactivator():: post REVOKEVID service call started with request data : "
						+ JsonUtils.javaObjectToJsonString(request));

		try {
			response = (ResponseWrapper) residentServiceRestClient.patchApi(
					env.getProperty(ApiName.IDAUTHREVOKEVID.name()) + "/" + vid, MediaType.APPLICATION_JSON, request,
					ResponseWrapper.class);
		} catch (Exception e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					requestDto.getIndividualIdType(), ResidentErrorCode.API_RESOURCE_UNAVAILABLE.getErrorCode()
							+ e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new ApisResourceAccessException("Unable to revoke VID : " + e.getMessage());
		}

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
				requestDto.getIndividualIdType(), "ResidentVidServiceImpl::vidDeactivator():: revoke Vid response :: "
						+ JsonUtils.javaObjectToJsonString(response));

		if (response.getErrors() != null && !response.getErrors().isEmpty()) {
			throw new VidRevocationException(ResidentErrorCode.VID_REVOCATION_EXCEPTION.getErrorMessage());

		}

		VidGeneratorResponseDto vidResponse = mapper.readValue(mapper.writeValueAsString(response.getResponse()),
				VidGeneratorResponseDto.class);

		return vidResponse;

	}
    
}
