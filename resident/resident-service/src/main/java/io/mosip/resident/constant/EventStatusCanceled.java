package io.mosip.resident.constant;

/**
 * Enum to store the event status canceled
 * 
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatusCanceled {
	CANCELED;
	
	public static boolean containsStatus(String status) {
        for (EventStatusCanceled eventStatusCanceled : EventStatusCanceled.values()) {
            if (eventStatusCanceled.name().equals(status)) {
                return true;
            }
        }
        return false;
    }

}
