package io.mosip.resident.constant;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enum to store the event status
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatus {
    FAILED, SUCCESS, IN_PROGRESS, ALL;
	
	
	public static Optional<EventStatus> getEventStatusForText(String status) {
		return Stream.of(values())
			.filter(event -> event.name()
				.equalsIgnoreCase(status.trim()))
			.findAny();
	}
}
