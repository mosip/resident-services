package io.mosip.resident.constant;

/**
 * Enum to store the event status Failure
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatusFailure {
    AUTHENTICATION_FAILED,
    FAILED,
    PAYMENT_FAILED,
    REJECTED,
    REPROCESS_FAILED,
    OTP_VERIFICATION_FAILED;
	
    public static boolean containsStatus(String status) {
        for (EventStatusFailure eventStatusFailure : EventStatusFailure.values()) {
            if (eventStatusFailure.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
