package io.mosip.resident.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * This class is used to test the TemplateUtil class
 * @author Kamesh Shekhar Prasad
 */

@Data
@Getter
@Setter
public class EventStatusResponseDTO {
    private String eventId;
    private String eventType;
    private String eventStatus;
    private String individualId;
    private String summary;
    private String timestamp;
    private Map<String, String> info;
}
