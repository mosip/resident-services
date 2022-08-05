package io.mosip.resident.constant;

/**
Enum to store the event status success
@author Kamesh Shekhar Prasad
 */
public enum EventStatusSuccess {
    AUTHENTICATION_SUCCESSFUL,
    STORED,
    CARD_DOWNLOADED,
    CARD_DELIVERED,
    RECEIVED,
    DATA_SHARED_SUCCESSFULLY,
    LOCKED,
    UNLOCKED,
    AUTHENTICATION_TYPE_LOCKED,
    AUTHENTICATION_TYPE_UNLOCKED,
    PROCESSED,
    DATA_UPDATED,
    VID_GENERATED,
    VID_REVOKED,
    EMAIL_VERIFIED,
    PHONE_VERIFIED;

    public static boolean containsStatus(String status) {
        for (EventStatusSuccess eventStatusSuccess : EventStatusSuccess.values()) {
            if (eventStatusSuccess.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
