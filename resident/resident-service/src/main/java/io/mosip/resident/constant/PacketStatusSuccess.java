package io.mosip.resident.constant;

/**
Enum to store the packet status success
@author Kamesh Shekhar Prasad
 */
public enum PacketStatusSuccess {
    PROCESSED,
    SUCCESS,
    UIN_GENERATED;

    public static boolean containsStatus(String status) {
        for (PacketStatusSuccess packetStatusSuccess : PacketStatusSuccess.values()) {
            if (packetStatusSuccess.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
