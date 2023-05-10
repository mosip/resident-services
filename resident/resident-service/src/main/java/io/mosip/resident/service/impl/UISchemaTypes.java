package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum UISchemaTypes {
	UPDATE_DEMOGRAPHICS("update-demographics", "UPDATE_MY_UIN"),
	PERSONALIZED_CARD("personalized-card", "DOWNLOAD_PERSONALIZED_CARD"),
	SHARE_CREDENTIAL("share-credential", "SHARE_CRED_WITH_PARTNER");
	;
	
	private String fileIdentifier;
	private String requestTypeCode;

	private UISchemaTypes(String fileIdentifier, String requestTypeCode) {
		this.fileIdentifier = fileIdentifier;
		this.requestTypeCode = requestTypeCode;
	}
	
	public String getFileIdentifier() {
		return fileIdentifier;
	}

	public String getRequestTypeCode() {
		return requestTypeCode;
	}

	public static Optional<UISchemaTypes> getUISchemaTypeFromString(String schemaTypeString) {
        for (UISchemaTypes uiSchemaType : values()) {
            if (uiSchemaType.getFileIdentifier().equals(schemaTypeString)) {
                return Optional.of(uiSchemaType);
            }
        }
        return Optional.empty();
    }

	public static Optional<String> getUISchemaTypeFromRequestTypeCode(String requestTypeCodeString) {
        for (UISchemaTypes uiSchemaType : values()) {
            if (uiSchemaType.getRequestTypeCode().equals(requestTypeCodeString)) {
                return Optional.of(uiSchemaType.getFileIdentifier());
            }
        }
        return Optional.empty();
    }

	public static List<String> getUISchemaTypesList() {
		List<String> uiSchemaValues = new ArrayList<>();
		for (UISchemaTypes uiSchemaType : values()) {
			uiSchemaValues.add(uiSchemaType.getFileIdentifier());
		}
		return uiSchemaValues;
	}
}
