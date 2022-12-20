package io.mosip.resident.constant;

/**
Enum to store the packet status Failure
@author Kamesh Shekhar Prasad
 */
public enum PacketStatusFailure {
    REJECTED,
    FAILURE,
    REPROCESS_FAILED;

    public static boolean containsStatus(String status) {
        for (PacketStatusFailure packetStatusFailure : PacketStatusFailure.values()) {
            if (packetStatusFailure.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
