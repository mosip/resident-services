package io.mosip.resident.service;

import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;

import java.security.NoSuchAlgorithmException;


public interface ResidentOtpService {

	/**
	 * Generate otp.
	 *
	 * @param otpRequestDTO OtpRequestDTO request.
	 * @return OtpResponseDTO object return.
	 * @throws IdAuthenticationBusinessException exception
	 */

	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO);

	public void insertData(OtpRequestDTO otpRequestDTO) throws ResidentServiceCheckedException, NoSuchAlgorithmException;
}
