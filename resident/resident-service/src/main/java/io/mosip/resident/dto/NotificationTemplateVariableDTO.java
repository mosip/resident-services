package io.mosip.resident.dto;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import lombok.Data;

@Data
public class NotificationTemplateVariableDTO {

	private String eventId;
	private RequestType requestType;
	private TemplateType templateType;
	private String langCode;

	private String otp;

	public NotificationTemplateVariableDTO(String eventId, RequestType requestType, TemplateType templateType, String langCode) {
		this.eventId = eventId;
		this.requestType = requestType;
		this.templateType = templateType;
		this.langCode = langCode;
	}

	public NotificationTemplateVariableDTO(String eventId, RequestType requestType, TemplateType templateType, String langCode, String otp) {
		this.eventId = eventId;
		this.requestType = requestType;
		this.templateType = templateType;
		this.langCode = langCode;
		this.otp = otp;
	}
}