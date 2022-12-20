package io.mosip.resident.constant;

/**
Enum to store the packet status In Progress
@author Kamesh Shekhar Prasad
 */
public enum PacketStatusInProgress {
    PROCESSING,
    REREGISTER,
    RESEND,
    RECEIVED,
    UPLOAD_PENDING,
    AWAITING_INFORMATION;

    public static boolean containsStatus(String status) {
        for (PacketStatusInProgress statusInProgress : PacketStatusInProgress.values()) {
            if (statusInProgress.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
