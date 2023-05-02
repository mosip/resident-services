package io.mosip.resident.dto;
import lombok.Data;

/**
 * @author Neha Farheen
 * This class is used a request DTO for trackingId
 */
@Data
public class UrlRedirectRequestDTO {
	private String transactionId;
	private String trackingId;

}
