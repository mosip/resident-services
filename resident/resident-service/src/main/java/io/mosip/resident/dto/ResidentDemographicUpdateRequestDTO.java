package io.mosip.resident.dto;

import lombok.Data;

@Data
public class ResidentDemographicUpdateRequestDTO {

    private String transactionID;

    private String individualId;

    private String identityJson;
}
