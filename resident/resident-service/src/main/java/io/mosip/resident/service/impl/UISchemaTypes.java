package io.mosip.resident.service.impl;

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

}
