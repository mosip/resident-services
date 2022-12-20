package io.mosip.resident.constant;

/**
Enum to store the VerificationStageTransactionStage
@author Kamesh Shekhar Prasad
 */
public enum CardReadyToDownloadTransactionStage {
    UIN_GENERATOR,
    BIOMETRIC_EXTRACTION,
    NOTIFICATION;

    public static boolean containsStatus(String status) {
        for (CardReadyToDownloadTransactionStage uinGenerationStageTransactionStage :
                CardReadyToDownloadTransactionStage.values()) {
            if (uinGenerationStageTransactionStage.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
