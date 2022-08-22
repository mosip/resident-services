package io.mosip.resident.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UnreadServiceNotificationDto {

	private String eventId;

	private String summary;

	private String eventStatus;

	private LocalDateTime timeStamp;

	private String requestType;

}
