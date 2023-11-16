package io.mosip.resident.service.impl;

import static io.mosip.resident.constant.ResidentConstants.ATTRIBUTE_LIST_DELIMITER;
import static io.mosip.resident.constant.ResidentConstants.OTP;

import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;

import io.mosip.resident.dto.IdentityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
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
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;
import reactor.util.function.Tuple2;

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
	private IdentityServiceImpl identityServiceImpl;

	@Autowired
	private ResidentTransactionRepository residentTransactionRepository;
	
	@Autowired
	private Utility utility;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO) throws NoSuchAlgorithmException, ResidentServiceCheckedException {
		logger.debug("ResidentOtpServiceImpl::generateOtp()::entry");
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
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		} catch (ResidentServiceCheckedException | NoSuchAlgorithmException e) {
			logger.error(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		}
		logger.debug("ResidentOtpServiceImpl::generateOtp()::exit");
		return responseDto;
	}

	@Override
	public void insertData(OtpRequestDTO otpRequestDTO) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		ResidentTransactionEntity residentTransactionEntity = utility.createEntity(RequestType.SEND_OTP);
		String individualId = otpRequestDTO.getIndividualId();
		residentTransactionEntity.setEventId(utility.createEventId());
		residentTransactionEntity.setRequestTrnId(otpRequestDTO.getTransactionID());
		String attributeList = otpRequestDTO.getOtpChannel().stream().map(String::toUpperCase).collect(Collectors.joining(ATTRIBUTE_LIST_DELIMITER));
		residentTransactionEntity.setAttributeList(attributeList);
		residentTransactionEntity.setAuthTypeCode(OTP);
		residentTransactionEntity.setRequestSummary("OTP Generated");
		residentTransactionEntity.setStatusCode(EventStatusInProgress.OTP_REQUESTED.name());
		residentTransactionEntity.setStatusComment("OTP_REQUESTED");
		residentTransactionEntity.setRefIdType(identityServiceImpl.getIndividualIdType(individualId).name());
		IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);
		String idaToken= identityServiceImpl.getIDAToken(identityDTO.getUIN());
		if( otpRequestDTO.getOtpChannel()!=null && otpRequestDTO.getOtpChannel().size()==1){
			residentTransactionEntity.setRefId(utility.getIdForResidentTransaction(otpRequestDTO.getOtpChannel(),
					identityDTO, idaToken));
		} else{
			residentTransactionEntity.setRefId(utility.getRefIdHash(individualId));
		}
		residentTransactionEntity.setIndividualId(individualId);
		residentTransactionEntity.setTokenId(idaToken);
		residentTransactionRepository.save(residentTransactionEntity);
	}

	@Override
	public IndividualIdResponseDto generateOtpForIndividualId(IndividualIdOtpRequestDTO individualIdRequestDto)
			throws NoSuchAlgorithmException, ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("ResidentOtpServiceImpl::generateOtpForIndividualId()::entry");
		try {
			Tuple2<String, IdType> individualIdAndType = identityServiceImpl.getIndividualIdAndTypeForAid(individualIdRequestDto.getIndividualId());
			individualIdRequestDto.setIndividualId(individualIdAndType.getT1());
			OtpRequestDTO otpRequestDTO = objectMapper.convertValue(individualIdRequestDto, OtpRequestDTO.class);
			otpRequestDTO.setTransactionID(individualIdRequestDto.getTransactionId());
			OtpResponseDTO otpResponseDTO = generateOtp(otpRequestDTO);
			IndividualIdResponseDto individualIdResponseDto = objectMapper.convertValue(otpResponseDTO, IndividualIdResponseDto.class);
			if(individualIdResponseDto!=null){
				individualIdResponseDto.setTransactionId(otpResponseDTO.getTransactionID());
			}
			logger.debug("ResidentOtpServiceImpl::generateOtpForIndividualId()::exit");
			return individualIdResponseDto;
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			throw new ResidentServiceCheckedException(ResidentErrorCode.AID_STATUS_IS_NOT_READY);
		}
	}


}
