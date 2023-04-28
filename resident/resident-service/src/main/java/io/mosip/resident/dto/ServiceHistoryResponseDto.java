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
    private String eventId;
    private String description;
    private String eventStatus;
    private String timeStamp;
    private String serviceType;
    private String requestType;
    private boolean pinnedStatus;
    private boolean readStatus;
    private int serialNumber; 
}
