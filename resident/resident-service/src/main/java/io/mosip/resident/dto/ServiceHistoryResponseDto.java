package io.mosip.resident.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ServiceHistoryResponseDto {
    private String applicationId;
    private String additionalInformation;
    private String TimeStamp;
    private String status;
}
