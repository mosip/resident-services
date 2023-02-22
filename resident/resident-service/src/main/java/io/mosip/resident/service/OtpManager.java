package io.mosip.resident.service;

import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.OtpRequestDTOV2;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

import java.io.IOException;

/**
 * @author Kamesh Shekhar Prasad
 * This interface is used to create method declaration for sending and validating otp.
 */
public interface OtpManager {
    public boolean sendOtp(MainRequestDTO<OtpRequestDTOV2> requestDTO, String channelType, String language) throws IOException, ResidentServiceCheckedException, ApisResourceAccessException;
    public boolean validateOtp(String otp, String userId, String transactionId) throws ApisResourceAccessException, ResidentServiceCheckedException;
    public Tuple2<Object, String> updateUserId(String userId, String transactionId) throws ApisResourceAccessException, ResidentServiceCheckedException;
}
