package io.mosip.resident.constant;

import java.util.List;

/**
Enum to store the TransactionStage
@author Kamesh Shekhar Prasad
 */
public enum PacketStatus {
    SUCCESS("SUCCESS", List.of("PROCESSED", "SUCCESS", "UIN_GENERATED")),
    IN_PROGRESS("IN-PROGRESS", List.of("PROCESSING", "REREGISTER", "RESEND", "RECEIVED", "UPLOAD_PENDING",
            "AWAITING_INFORMATION")),
    FAILURE("FAILURE", List.of("REJECTED", "FAILURE", "REPROCESS_FAILED"));
    private List<String> listOfName;
    private String name;
    PacketStatus(String name, List<String> listOfName) {
        this.name = name;
        this.listOfName = listOfName;
    }

    public static String getStatusCode(String statusCode){
        for (PacketStatus packetStatus : values()) {
            if(packetStatus.listOfName.contains(statusCode)){
                return packetStatus.name;
            }
        }
        return "";
    }

}
