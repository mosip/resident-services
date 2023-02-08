package io.mosip.resident.constant;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enum to store the event status
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatus {
    FAILED("Failed"), SUCCESS("Success"), IN_PROGRESS("In Progress");
	
	private String status;
	
	private EventStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}
	
	public static Optional<EventStatus> getEventStatusForText(String status) {
		return Stream.of(values())
			.filter(event -> event.getStatus()
				.equalsIgnoreCase(status.trim()))
			.findAny();
	}
}
