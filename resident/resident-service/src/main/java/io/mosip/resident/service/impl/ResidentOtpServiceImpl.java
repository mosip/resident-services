package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class ResidentOtpServiceImpl implements ResidentOtpService {

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

	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO) {
		OtpResponseDTO responseDto = null;
		try {
			responseDto = residentServiceRestClient.postApi(
					env.getProperty(ApiName.OTP_GEN_URL.name()), MediaType.APPLICATION_JSON, otpRequestDTO,
					OtpResponseDTO.class);
			insertData(otpRequestDTO);
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
	public void insertData(OtpRequestDTO otpRequestDTO) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		ResidentTransactionEntity residentTransactionEntity = new ResidentTransactionEntity();

		IdentityDTO identityDTO = identityServiceImpl.getIdentity(otpRequestDTO.getIndividualId());

		String uin = identityDTO.getUIN();
		String email = identityDTO.getEmail();
		String phone = identityDTO.getPhone();

		String id = getIdForResidentTransaction(uin, email, phone);

		byte[] idBytes = id.getBytes();
		String hash = HMACUtils2.digestAsPlainText(idBytes);
		residentTransactionEntity.setAid(hash);
		residentTransactionEntity.setRequestDtimes(LocalDateTime.now());
		residentTransactionEntity.setResponseDtime(LocalDateTime.now());
		residentTransactionEntity.setRequestTrnId(otpRequestDTO.getTransactionID());
		residentTransactionEntity.setRequestTypeCode("OTP");
		residentTransactionEntity.setRequestSummary("OTP Generated");
		residentTransactionEntity.setStatusCode("OTP_REQUESTED");
		residentTransactionEntity.setStatusComment("OTP_REQUESTED");
		residentTransactionEntity.setLangCode("eng");
		residentTransactionEntity.setRefIdType("UIN");
		residentTransactionEntity.setRefId(uin);
		residentTransactionEntity.setTokenId("");
		residentTransactionEntity.setCrBy("mosip");
		residentTransactionEntity.setCrDtimes(LocalDateTime.now());

		residentTransactionRepository.save(residentTransactionEntity);
	}

	private String getIdForResidentTransaction(String uin, String email, String phone) throws ResidentServiceCheckedException {
		String idaToken= identityServiceImpl.getIDAToken(uin);
		String id;
		if(email != null) {
			id= email+idaToken;
		} else if(phone != null) {
			id= phone+idaToken;
		} else {
			throw new ResidentServiceCheckedException(ResidentErrorCode.NO_CHANNEL_IN_IDENTITY);
		}
		return id;
	}

}
