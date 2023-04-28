package io.mosip.resident.dto;

import lombok.Data;

@Data
public class NotificationResponseDTO {
	private String status;
	private String message;
	private String maskedEmail;
	private String maskedPhone;
}
