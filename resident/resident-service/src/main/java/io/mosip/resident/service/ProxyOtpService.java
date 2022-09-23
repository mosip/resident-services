package io.mosip.resident.service;

import io.mosip.kernel.core.authmanager.model.AuthNResponse;

import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import org.springframework.http.ResponseEntity;

/**
 * @Author Kamesh Shekhar Prasad
 * This class is used to do operations on otp.
 */
public interface ProxyOtpService {
    ResponseEntity<MainResponseDTO<AuthNResponse>> sendOtp(MainRequestDTO<OtpRequestDTOV2> userOtpRequest);

    ResponseEntity<MainResponseDTO<AuthNResponse>> validateWithUserIdOtp(MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest);
}
