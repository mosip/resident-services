package io.mosip.resident.constant;

/**
 * Enum to store the event status Failure
 * @author Kamesh Shekhar Prasad
 */
public enum EventStatusFailure {
    AUTHENTICATION_FAILED,
    VID_GENERATION_FAILED,
    VID_REVOCATION_FAILED,
    AUTHENTICATION_TYPE_LOCK_FAILED,
    AUTHENTICATION_TYPE_UNLOCK_FAILED,
    DATA_UPDATE_FAILED,
    CARD_DOWNLOAD_FAILED,
    CARD_DELIVERY_FAILED,
    DATA_SHARE_FAILED,
    EMAIL_VERIFICATION_FAILED,
    PHONE_VERIFICATION_FAILED;
    public static boolean containsStatus(String status) {
        for (EventStatusFailure eventStatusFailure : EventStatusFailure.values()) {
            if (eventStatusFailure.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
