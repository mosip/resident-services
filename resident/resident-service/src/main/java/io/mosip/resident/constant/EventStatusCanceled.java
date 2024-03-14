package io.mosip.resident.constant;

/**
 * Enum to store the event status canceled
 * 
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatusCanceled {
	CANCELED;
	
	public static boolean containsStatus(String status) {
        for (EventStatusCanceled eventStatusInProgress : EventStatusCanceled.values()) {
            if (eventStatusInProgress.name().equals(status)) {
                return true;
            }
        }
        return false;
    }

}
