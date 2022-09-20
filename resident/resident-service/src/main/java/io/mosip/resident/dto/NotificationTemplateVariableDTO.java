package io.mosip.resident.dto;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationTemplateVariableDTO {

	private String eventId;
	private RequestType requestType;
	private TemplateType templateType;
	private String langCode;

}