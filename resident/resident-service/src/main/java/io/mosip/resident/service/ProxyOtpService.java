package io.mosip.resident.service;

import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.http.ResponseEntity;

import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.MainResponseDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.dto.OtpRequestDTOV3;
import reactor.util.function.Tuple2;

/**
 * @Author Kamesh Shekhar Prasad
 * This class is used to do operations on otp.
 */
public interface ProxyOtpService {
    ResponseEntity<MainResponseDTO<AuthNResponse>> sendOtp(MainRequestDTO<OtpRequestDTOV2> userOtpRequest, IdentityDTO identityDTO) throws ResidentServiceCheckedException;

    Tuple2<MainResponseDTO<AuthNResponse>, String> validateWithUserIdOtp(MainRequestDTO<OtpRequestDTOV3> userIdOtpRequest);
}
