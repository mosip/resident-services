package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.constant.ServiceType;
import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class ResidentOtpServiceImpl implements ResidentOtpService {

	public static final String EMAIL_CHANNEL = "EMAIL";
	public static final String PHONE_CHANNEL = "PHONE";

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	private static final Logger logger = LoggerConfiguration.logConfig(ResidentOtpServiceImpl.class);

	@Autowired
	Environment env;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;
	
	@Autowired
	private ResidentServiceImpl residentServiceImpl;

	@Autowired
	private Utility utility;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO) throws NoSuchAlgorithmException, ResidentServiceCheckedException {
		OtpResponseDTO responseDto = null;
		try {
			responseDto = residentServiceRestClient.postApi(
					env.getProperty(ApiName.OTP_GEN_URL.name()), MediaType.APPLICATION_JSON, otpRequestDTO,
					OtpResponseDTO.class);
			if((responseDto.getErrors() ==null || responseDto.getErrors().isEmpty() )&& responseDto.getResponse()!= null) {
				{
					insertData(otpRequestDTO);
				}
			}
		} catch (ApisResourceAccessException e) {
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException e) {
			logger.error(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		}
		return responseDto;
	}

	@Override
	public void insertData(OtpRequestDTO otpRequestDTO) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
		residentTransactionEntity.setResponseDtime(LocalDateTime.now());
		residentTransactionEntity.setRequestTrnId(otpRequestDTO.getTransactionID());
		residentTransactionEntity.setRequestTypeCode(RequestType.SEND_OTP.name());
		String attributeList = otpRequestDTO.getOtpChannel().stream().collect(Collectors.joining(", "));
		residentTransactionEntity.setAttributeList(attributeList);
		residentTransactionEntity.setAuthTypeCode(attributeList);
		residentTransactionEntity.setRequestSummary("OTP Generated");
		residentTransactionEntity.setStatusCode("OTP_REQUESTED");
		residentTransactionEntity.setStatusComment("OTP_REQUESTED");
		residentTransactionEntity.setLangCode("eng");
		residentTransactionEntity.setRefIdType("UIN");
		if( otpRequestDTO.getOtpChannel()!=null && otpRequestDTO.getOtpChannel().size()==1){
			residentTransactionEntity.setRefId(utility.getIdForResidentTransaction(otpRequestDTO.getIndividualId(), otpRequestDTO.getOtpChannel()));
		} else{
			residentTransactionEntity.setRefId(utility.getRefIdHash(otpRequestDTO.getIndividualId()));
		}
		residentTransactionEntity.setTokenId(identityServiceImpl.getIDAToken(otpRequestDTO.getIndividualId()));
		residentTransactionEntity.setCrBy("mosip");
		residentTransactionEntity.setCrDtimes(LocalDateTime.now());
		residentTransactionEntity.setPurpose(String.join(ResidentConstants.COMMA, otpRequestDTO.getOtpChannel()));
		residentTransactionRepository.save(residentTransactionEntity);
	}

	@Override
	public IndividualIdResponseDto generateOtpForIndividualId(IndividualIdOtpRequestDTO individualIdRequestDto)
			throws NoSuchAlgorithmException, ResidentServiceCheckedException, ApisResourceAccessException {
		String individualId;
		try {
			individualId = identityServiceImpl.getIndividualIdForAid(individualIdRequestDto.getIndividualId());
			individualIdRequestDto.setIndividualId(individualId);
			OtpRequestDTO otpRequestDTO = objectMapper.convertValue(individualIdRequestDto, OtpRequestDTO.class);
			otpRequestDTO.setTransactionID(individualIdRequestDto.getTransactionId());
			OtpResponseDTO otpResponseDTO = generateOtp(otpRequestDTO);
			IndividualIdResponseDto individualIdResponseDto = objectMapper.convertValue(otpResponseDTO, IndividualIdResponseDto.class);
			individualIdResponseDto.setTransactionId(otpResponseDTO.getTransactionID());
			return individualIdResponseDto;
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.AID_STATUS_IS_NOT_READY);
		}
	}


}
