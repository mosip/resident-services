package io.mosip.resident.constant;

import java.util.List;

import org.springframework.core.env.Environment;

/**
 * Enum to store the TransactionStage
 * 
 * @author Kamesh Shekhar Prasad
 */
public enum TransactionStage {
	REQUEST_RECEIVED("Request received", "resident.REQUEST_RECEIVED.packet-transaction-type-code.list"),
	VALIDATION_STAGE("Validation stage", "resident.VALIDATION_STAGE.packet-transaction-type-code.list"),
	VERIFICATION_STAGE("Verification stage", "resident.VERIFICATION_STAGE.packet-transaction-type-code.list"),
	UIN_GENERATION_STAGE("UIN generation stage", "resident.UIN_GENERATION_STAGE.packet-transaction-type-code.list"),
	CARD_READY_TO_DOWNLOAD("Card ready to download",
			"resident.CARD_READY_TO_DOWNLOAD.packet-transaction-type-code.list");

	private String name;
	private String transactionTypeCodePropertyName;

	TransactionStage(String name, String transactionTypeCodePropertyName) {
		this.name = name;
		this.transactionTypeCodePropertyName = transactionTypeCodePropertyName;
	}

	public String getName() {
		return name;
	}

	public static String getTypeCode(String transactionTypeCode, Environment env) {
		for (TransactionStage transactionStage : values()) {
			if (getTransactionTypeCodeList(transactionStage, env).contains(transactionTypeCode)) {
				return transactionStage.name;
			}
		}
		return "";
	}

	private static List<String> getTransactionTypeCodeList(TransactionStage transactionStage, Environment env) {
		return List.of(env.getProperty(transactionStage.transactionTypeCodePropertyName).split(","));
	}

	public static boolean containsStatus(String status) {
		for (TransactionStage transactionStage : TransactionStage.values()) {
			if (transactionStage.name().equals(status)) {
				return true;
			}
		}
		return false;
	}
}
