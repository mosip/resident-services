package io.mosip.resident.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is used to store the service history response.
 *
 * @author Kamesh Shekhar Prasad
 **/

@Data
@Getter
@Setter
public class ServiceHistoryResponseDto {
    private String applicationId;
    private String additionalInformation;
    private String TimeStamp;
    private String status;
}
