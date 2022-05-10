package io.mosip.resident.service;

import io.mosip.resident.dto.OtpRequestDTO;
import io.mosip.resident.dto.OtpResponseDTO;
import io.mosip.resident.dto.RIDOtpRequestDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;


public interface ResidentOtpService {

	/**
	 * Generate otp.
	 *
	 * @param otpRequest OtpRequestDTO request.
	 * @return OtpResponseDTO object return.
	 * @throws IdAuthenticationBusinessException exception
	 */

	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDTO);

	/**
	 * Generate otp using RID.
	 *
	 * @param ridOtpRequestDTO RIDOtpRequestDTO request.
	 * @return OtpResponseDTO object return.
	 * @throws ResidentServiceCheckedException exception
	 */

	public OtpResponseDTO generateRIDOtp(RIDOtpRequestDTO ridOtpRequestDTO) throws ResidentServiceCheckedException;
}
