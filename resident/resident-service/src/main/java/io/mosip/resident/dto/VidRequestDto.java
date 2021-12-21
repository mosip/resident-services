package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VidRequestDto implements Serializable {

    private String transactionID;
    private String individualId;
    private String otp;
    private String vidType;

}
