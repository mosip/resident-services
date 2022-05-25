package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class BaseVidRequestDto implements Serializable {

    private String transactionID;
    private String vidType;

}
