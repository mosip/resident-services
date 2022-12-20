package io.mosip.resident.constant;

/**
Enum to store the TransactionStage
@author Kamesh Shekhar Prasad
 */
public enum TransactionStage {
    REQUEST_RECEIVED("Request received"),
    VALIDATION_STAGE("Validation stage"),
    VERIFICATION_STAGE("Verification stage"),
    UIN_GENERATION_STAGE("UIN generation stage"),
    CARD_READY_TO_DOWNLOAD("Card ready to download");

    private String name;
    TransactionStage(String name) {
        this.name = name;
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
