package io.mosip.resident.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This class is DTO class to take input userId to send OTP.
 * @author Kamesh Shekhar Prasad
 */
@Getter
@Setter
@ToString
public class OtpRequestDTOV3 extends OtpRequestDTOV2{
    private  String otp;
}

