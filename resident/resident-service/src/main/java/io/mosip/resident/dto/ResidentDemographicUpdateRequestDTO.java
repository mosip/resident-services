package io.mosip.resident.dto;

import lombok.Data;

/**
 * This class is a DTO that is used to update the demographic information of a
 * resident
 * 
 * @author Manoj SP
 */
@Data
public class ResidentDemographicUpdateRequestDTO {

    private String transactionID;

    private String individualId;

    private String identityJson;
}
