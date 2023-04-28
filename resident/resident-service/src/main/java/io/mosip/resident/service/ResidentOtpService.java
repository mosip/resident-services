package io.mosip.resident.service;

import java.security.NoSuchAlgorithmException;

import io.mosip.resident.dto.IndividualIdOtpRequestDTO;
import io.mosip.resident.dto.IndividualIdResponseDto;
import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;


public interface ResidentOtpService {

	/**
	 * Generate otp.
	 *
	 * @param otpRequestDTO OtpRequestDTO request.
	 * @return OtpResponseDTO object return.
	 * @throws IdAuthenticationBusinessException exception
	 */

	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO) throws NoSuchAlgorithmException, ResidentServiceCheckedException;

	public void insertData(OtpRequestDTO otpRequestDTO) throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException;

	public IndividualIdResponseDto generateOtpForIndividualId(IndividualIdOtpRequestDTO otpRequestDto) throws NoSuchAlgorithmException, ResidentServiceCheckedException, ApisResourceAccessException;

}
