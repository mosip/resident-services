package io.mosip.resident.constant;

import java.util.List;

/**
Enum to store the TransactionStage
@author Kamesh Shekhar Prasad
 */
public enum TransactionStage {
    REQUEST_RECEIVED("Request received", List.of("PACKET_RECEIVER", "UPLOAD_PACKET", "VALIDATE_PACKET", "PACKET_CLASSIFICATION")),
    VALIDATION_STAGE("Validation stage", List.of("CMD_VALIDATION", "OPERATOR_VALIDATION", "SUPERVISOR_VALIDATION",
            "INTRODUCER_VALIDATION", "EXTERNAL_INTEGRATION")),
    VERIFICATION_STAGE("Verification stage", List.of("DEMOGRAPHIC_VERIFICATION", "MANUAL_ADJUDICATION",
            "VERIFICATION", "BIOGRAPHIC_VERIFICATION")),
    UIN_GENERATION_STAGE("UIN generation stage", List.of("UIN_GENERATOR", "BIOMETRIC_EXTRACTION", "NOTIFICATION")),
    CARD_READY_TO_DOWNLOAD("Card ready to download", List.of("PRINT_SERVICE", "PRINT_POSTAL_SERVICE", "PRINT"));
    private List<String> listOfName;
    private String name;
    TransactionStage(String name, List<String> listOfName) {
        this.name = name;
        this.listOfName = listOfName;
    }

    public static String getTypeCode(String transactionTypeCode){
        for (TransactionStage transactionStage : values()) {
            if(transactionStage.listOfName.contains(transactionTypeCode)){
                return transactionStage.name;
            }
        }
        return "";
    }

    public static boolean containsStatus(String status) {
        for (TransactionStage transactionStage :
                TransactionStage.values()) {
            if (transactionStage.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
