package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum UISchemaTypes {
	UPDATE_DEMOGRAPHICS("update-demographics"),
	PERSONALIZED_CARD("personalized-card"),
	SHARE_CREDENTIAL("share-credential");
	;
	
	private String fileIdentifier;

	private UISchemaTypes(String fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}
	
	public String getFileIdentifier() {
		return fileIdentifier;
	}

	public static Optional<UISchemaTypes> getUISchemaTypeFromString(String schemaTypeString) {
        for (UISchemaTypes uiSchemaType : values()) {
            if (uiSchemaType.getFileIdentifier().equals(schemaTypeString)) {
                return Optional.of(uiSchemaType);
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
