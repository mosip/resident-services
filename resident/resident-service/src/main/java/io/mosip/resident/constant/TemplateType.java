package io.mosip.resident.constant;

public enum TemplateType {

	REQUEST_RECEIVED("request-received"),
	SUCCESS("success"),
	FAILURE("failure");

	private String type;

	TemplateType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}