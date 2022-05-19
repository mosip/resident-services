package io.mosip.resident.dto;

import lombok.Data;

@Data
public class RetrievePartnerDetailsResponse {

    private String partnerID;
    private String status;
    private String organizationName;
    private String contactNumber;
    private String emailId;
    private String address;
    private String partnerType;
    private String policyGroup;
    private Boolean isActive;
}
