package io.mosip.resident.service.impl;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.dto.RIDOtpRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.DataNotFoundException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.ResidentOtpService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TokenGenerator;
import io.mosip.resident.util.Utilitiy;

@Service
public class ResidentOtpServiceImpl implements ResidentOtpService {

	@Value("${ida.internal.otp.id}")
	private String idaOtpId;

	@Value("${ida.internal.otp.version}")
	private String idaOtpVersion;

	@Autowired
	private ResidentServiceRestClient residentServiceRestClient;

	@Autowired
	private TokenGenerator tokenGenerator;

	@Autowired
	Environment env;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private Utilitiy utilitiy;

	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO) {

		OtpResponseDTO responseDto = null;
		try {
			responseDto = residentServiceRestClient.postApi(
					env.getProperty(ApiName.OTP_GEN_URL.name()), MediaType.APPLICATION_JSON, otpRequestDTO,
					OtpResponseDTO.class, tokenGenerator.getToken());
		} catch (ApisResourceAccessException | IOException e) {
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		}
		return responseDto;
	}

	@Override
	public OtpResponseDTO generateRIDOtp(RIDOtpRequestDTO ridOtpRequestDTO) throws ResidentServiceCheckedException {

		OtpResponseDTO responseDto = null;
		try {
			String uin = getUINForRID(ridOtpRequestDTO.getIndividualId());
			OtpRequestDTO otpRequestDTO = buildOtpRequestDTO(ridOtpRequestDTO, uin);
			responseDto = residentServiceRestClient.postApi(
					env.getProperty(ApiName.OTP_GEN_URL.name()), MediaType.APPLICATION_JSON, otpRequestDTO,
					OtpResponseDTO.class, tokenGenerator.getToken());
		} catch (ApisResourceAccessException | IOException e) {
			audit.setAuditRequestDto(EventEnum.OTP_GEN_EXCEPTION);
			throw new ResidentServiceException(ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorCode(),
					ResidentErrorCode.OTP_GENERATION_EXCEPTION.getErrorMessage(), e);
		}
		return responseDto;
	}

	private String getUINForRID(String rid) throws ResidentServiceCheckedException {
		try {
			JSONObject jsonObject = utilitiy.retrieveIdrepoJson(rid);
			return JsonUtil.getJSONValue(jsonObject, IdType.UIN.name());
		} catch (ResidentServiceCheckedException e) {
			throw new DataNotFoundException(e.getErrorCode(),e.getMessage());
		}
	}

	private OtpRequestDTO buildOtpRequestDTO(RIDOtpRequestDTO ridOtpRequestDTO, String uin) {
		OtpRequestDTO otpRequestDTO = new OtpRequestDTO();
		otpRequestDTO.setIndividualId(uin);
		otpRequestDTO.setIndividualIdType(IdType.UIN.name());
		otpRequestDTO.setId(idaOtpId);
		otpRequestDTO.setVersion(idaOtpVersion);
		otpRequestDTO.setTransactionID(ridOtpRequestDTO.getTransactionID());
		otpRequestDTO.setMetadata(ridOtpRequestDTO.getMetadata());
		otpRequestDTO.setOtpChannel(ridOtpRequestDTO.getOtpChannel());
		otpRequestDTO.setRequestTime(ridOtpRequestDTO.getRequestTime());
		return otpRequestDTO;
	} 

}
