package io.mosip.resident.service.impl;

import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.preregistration.application.constant.PreRegLoginConstant;
import io.mosip.preregistration.core.util.GenericUtil;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.ExceptionJSONInfoDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.OtpManager;
import io.mosip.resident.service.ProxyOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.Utility;
import io.mosip.resident.validator.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author  Kamesh Shekhar Prasad
 * This class is used to implement opt service impl class.
 */
@Service
public class ProxyOtpServiceImpl implements ProxyOtpService {

    private Logger log = LoggerConfiguration.logConfig(ProxyOtpServiceImpl.class);

    private List<String> otpChannel;

    @Autowired
    private OtpManager otpManager;

    @Autowired
    private AuditUtil audit;

    @Autowired
    RequestValidator requestValidator;
    
    @Autowired
	private IdentityServiceImpl identityServiceImpl;
    
    @Autowired
	private Utility utility;
    
    @Autowired
	private ResidentTransactionRepository residentTransactionRepository;

    @Value("${mosip.mandatory-languages}")
    private String mandatoryLanguage;

    @Override
    public ResponseEntity<MainResponseDTO<AuthNResponse>> sendOtp(MainRequestDTO<OtpRequestDTOV2> userOtpRequest) {
        MainResponseDTO<AuthNResponse> response = new MainResponseDTO<>();
        String userid = null;
        boolean isSuccess = false;
        String language = mandatoryLanguage;
        log.info("In callsendOtp method of login service  with userID: {} and langCode",
                userOtpRequest.getRequest().getUserId(), language);

        try {
            response = (MainResponseDTO<AuthNResponse>) getMainResponseDto(userOtpRequest);
            log.info("Response after loginCommonUtil {}", response);

            userid = userOtpRequest.getRequest().getUserId();
            otpChannel = requestValidator.validateUserIdAndTransactionId(userid, userOtpRequest.getRequest().getTransactionId());
            boolean otpSent = otpManager.sendOtp(userOtpRequest, otpChannel.get(0), language);
            AuthNResponse authNResponse = null;
            if (otpSent) {
                if (otpChannel.get(0).equalsIgnoreCase(PreRegLoginConstant.PHONE_NUMBER))
                    authNResponse = new AuthNResponse(PreRegLoginConstant.SMS_SUCCESS, PreRegLoginConstant.SUCCESS);
                else
                    authNResponse = new AuthNResponse(PreRegLoginConstant.EMAIL_SUCCESS, PreRegLoginConstant.SUCCESS);
                response.setResponse(authNResponse);
                isSuccess = true;
            } else
                isSuccess = false;

            response.setResponsetime(DateUtils.getUTCCurrentDateTimeString());
        } catch (HttpServerErrorException | HttpClientErrorException ex) {
            log.error("In callsendOtp method of login service- ", ex.getResponseBodyAsString());
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE,
                    userid, "Send OTP"));
            if(ex instanceof HttpServerErrorException || ex instanceof HttpClientErrorException){
                throw new ResidentServiceException(ResidentErrorCode.CONFIG_FILE_NOT_FOUND_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.CONFIG_FILE_NOT_FOUND_EXCEPTION.getErrorMessage());
            }
        }
        catch (Exception ex) {
            log.error("In callsendOtp method of login service- ", ex);
            audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE,
                    userid, "Send OTP"));
            throw new ResidentServiceException(ResidentErrorCode.SEND_OTP_FAILED.getErrorCode(),
                    ResidentErrorCode.SEND_OTP_FAILED.getErrorMessage(), ex);
        } finally {
            if (isSuccess) {
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_SUCCESS,
                        userid, "Send OTP"));
            } else {

                ExceptionJSONInfoDTO errors = new ExceptionJSONInfoDTO(ResidentErrorCode.SEND_OTP_FAILED.getErrorCode(),
                        ResidentErrorCode.SEND_OTP_FAILED.getErrorMessage());
                List<ExceptionJSONInfoDTO> lst = new ArrayList<>();
                lst.add(errors);
                response.setErrors(lst);
                response.setResponse(null);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.SEND_OTP_FAILURE,
                        userid, "Send OTP"));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Override
    public Tuple2<MainResponseDTO<AuthNResponse>, String> validateWithUserIdOtp(MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest) {
        log.info("In calluserIdOtp method of login service ");
        MainResponseDTO<AuthNResponse> response = null;
        response = (MainResponseDTO<AuthNResponse>) getMainResponseDto(userIdOtpRequest);
        String userid = null;
        boolean isSuccess = false;
        String eventId = ResidentConstants.NOT_AVAILABLE;

        try {
            OtpRequestDTOV3 user = userIdOtpRequest.getRequest();
            userid = user.getUserId();
            String transactionId = user.getTransactionId();
			boolean validated = otpManager.validateOtp(user.getOtp(), userid, transactionId);
            AuthNResponse authresponse = new AuthNResponse();
            if (validated) {
                Tuple2<Object, String> updateResult = otpManager.updateUserId(userid, transactionId);
                eventId = updateResult.getT2();
                authresponse.setMessage(PreRegLoginConstant.VALIDATION_SUCCESS);
                authresponse.setStatus(PreRegLoginConstant.SUCCESS);
            } else {
				throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED,
						Map.of(ResidentConstants.EVENT_ID, eventId));
            }
            response.setResponse(authresponse);
            isSuccess = true;
        } catch (ResidentServiceException ex) {
            log.error("In calluserIdOtp method of login service- ", ex);
            ex.setMetadata(Map.of(ResidentConstants.EVENT_ID, eventId));
			throw ex;
        } catch (RuntimeException ex) {
            log.error("In calluserIdOtp method of login service- ", ex);
            throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED, ex,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } catch (ResidentServiceCheckedException e) {
        	throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } catch (ApisResourceAccessException e) {
            throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION, e,
					Map.of(ResidentConstants.EVENT_ID, eventId));
        } finally {
            response.setResponsetime(GenericUtil.getCurrentResponseTime());

            if (isSuccess) {
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.VALIDATE_OTP_SUCCESS,
                        userid, "Validate OTP Success"));
            } else {
                ExceptionJSONInfoDTO errors = new ExceptionJSONInfoDTO(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
                List<ExceptionJSONInfoDTO> lst = new ArrayList<>();
                lst.add(errors);
                response.setErrors(lst);
                response.setResponse(null);
                audit.setAuditRequestDto(EventEnum.getEventEnumWithValue(EventEnum.OTP_VALIDATION_FAILED,
                        userid, "Validate OTP Failed"));
            }

        }
        return Tuples.of(response, eventId);
    }

	private ResidentTransactionEntity createResidentTransactionEntity(String userId)
			throws ApisResourceAccessException, ResidentServiceCheckedException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTypeCode(RequestType.UPDATE_MY_UIN.name());
        residentTransactionEntity.setAuthTypeCode(identityServiceImpl.getResidentAuthenticationMode());
		residentTransactionEntity.setStatusCode(EventStatusSuccess.DATA_UPDATED.name());
        residentTransactionEntity.setAttributeList(userId);
        String individualId = identityServiceImpl.getResidentIndvidualId();
		residentTransactionEntity.setRefId(utility.convertToMaskDataFormat(individualId));
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransactionEntity.setRequestSummary(EventStatusSuccess.DATA_UPDATED.name());
		if (requestValidator.phoneValidator(userId)) {
			residentTransactionEntity.setStatusComment("Update phone as " + userId);
		} else if (requestValidator.emailValidator(userId)) {
			residentTransactionEntity.setStatusComment("Update email as " + userId);
		}
		return residentTransactionEntity;
	}

	/**
     * This method will return the MainResponseDTO with id and version
     *
     * @param mainRequestDto
     * @return MainResponseDTO<?>
     */
    public MainResponseDTO<?> getMainResponseDto(MainRequestDTO<?> mainRequestDto) {
        log.info("In getMainResponseDTO method of ProxyOtpServiceImpl");
        MainResponseDTO<?> response = new MainResponseDTO<>();
        response.setId(mainRequestDto.getId());
        response.setVersion(mainRequestDto.getVersion());
        return response;
    }

}
