package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used a request DTO for grievance tiket API.
 */
@Data
public class GrievanceRequestDTO {
    private String eventId;
    private String name;
    private String emailId;
    private String alternateEmailId;
    private String phoneNo;
    private String alternatePhoneNo;
    private String message;
}