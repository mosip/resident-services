package io.mosip.resident.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.mosip.resident.constant.RequestType;

public enum UISchemaTypes {
	UPDATE_DEMOGRAPHICS("update-demographics", RequestType.UPDATE_MY_UIN),
	PERSONALIZED_CARD("personalized-card", RequestType.DOWNLOAD_PERSONALIZED_CARD),
	SHARE_CREDENTIAL("share-credential", RequestType.SHARE_CRED_WITH_PARTNER);
	;

	private String fileIdentifier;
	private RequestType requestType;

	private UISchemaTypes(String fileIdentifier, RequestType requestType) {
		this.fileIdentifier = fileIdentifier;
		this.requestType = requestType;
	}

	public String getFileIdentifier() {
		return fileIdentifier;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public static Optional<UISchemaTypes> getUISchemaTypeFromString(String schemaTypeString) {
		for (UISchemaTypes uiSchemaType : values()) {
			if (uiSchemaType.getFileIdentifier().equals(schemaTypeString)) {
				return Optional.of(uiSchemaType);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getUISchemaTypeFromRequestTypeCode(String requestTypeString) {
		for (UISchemaTypes uiSchemaType : values()) {
			if (uiSchemaType.getRequestType().name().equals(requestTypeString)) {
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
