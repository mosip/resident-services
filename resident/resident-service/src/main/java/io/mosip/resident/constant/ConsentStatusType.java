package io.mosip.resident.constant;

/**
 * Enum to check the consent status
 * 
 * @author Neha Farheen
 */
public enum ConsentStatusType {
	ACCEPTED, DENIED;

	public static boolean containsStatus(String status) {
		for (ConsentStatusType consentStatus : ConsentStatusType.values()) {
			if (consentStatus.name().equalsIgnoreCase(status)) {
				return true;
			}
		}
		return false;
	}

}
