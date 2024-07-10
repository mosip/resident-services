package io.mosip.resident.validator;

import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ValidateOtpCharLimit {

    @Value("${mosip.kernel.otp.default-length}")
    private int otpLength;

    public void validateOtpCharLimit(String otp) {
        if (otp.length() > otpLength) {
            throw new ResidentServiceException(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorCode(),
                    String.format(ResidentErrorCode.CHAR_LIMIT_EXCEEDS.getErrorMessage(),otpLength,otp));
        }
    }
}
