package io.mosip.resident.constant;

/**
 * Enum to store the event status in-progress
 * 
 * @author Ritik Jain
 */
public enum EventStatusInProgress {
	NEW,
	ISSUED,
	PAYMENT_CONFIRMED,
	PRINTING,
	IN_TRANSIT,
	PROCESSING,
	PAUSED,
	RESUMABLE,
	REPROCESS,
	PAUSED_FOR_ADDITIONAL_INFO,
	OTP_REQUESTED,
	IDENTITY_UPDATED;
	
	public static boolean containsStatus(String status) {
        for (EventStatusInProgress eventStatusInProgress : EventStatusInProgress.values()) {
            if (eventStatusInProgress.name().equals(status)) {
                return true;
            }
        }
        return false;
    }

}
