package io.mosip.resident.constant;

/**
Enum to store the ValidationStageTransactionStage
@author Kamesh Shekhar Prasad
 */
public enum ValidationStageTransactionStage {
    CMD_VALIDATION,
    OPERATOR_VALIDATION,
    SUPERVISOR_VALIDATION,
    INTRODUCER_VALIDATION,
    EXTERNAL_INTEGRATION;

    public static boolean containsStatus(String status) {
        for (ValidationStageTransactionStage validationStageTransactionStage : ValidationStageTransactionStage.values()) {
            if (validationStageTransactionStage.name().equals(status)) {
                return true;
            }
        }
        return false;
    }
}
