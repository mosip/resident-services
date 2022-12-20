package io.mosip.resident.constant;

/**
Enum to store the VerificationStageTransactionStage
@author Kamesh Shekhar Prasad
 */
public enum VerificationStageTransactionStage {
    DEMOGRAPHIC_VERIFICATION,
    MANUAL_ADJUDICATION,
    VERIFICATION,
    BIOGRAPHIC_VERIFICATION;

    public static boolean containsStatus(String status) {
        for (VerificationStageTransactionStage verificationStageTransactionStage : VerificationStageTransactionStage.values()) {
            if (verificationStageTransactionStage.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
