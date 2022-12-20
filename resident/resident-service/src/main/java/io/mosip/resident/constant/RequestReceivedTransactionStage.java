package io.mosip.resident.constant;

/**
Enum to store the RequestReceivedTransactionStage
@author Kamesh Shekhar Prasad
 */
public enum RequestReceivedTransactionStage {
    PACKET_RECEIVER,
    UPLOAD_PACKET,
    VALIDATE_PACKET,
    PACKET_CLASSIFICATION;

    public static boolean containsStatus(String status) {
        for (RequestReceivedTransactionStage requestReceived : RequestReceivedTransactionStage.values()) {
            if (requestReceived.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
