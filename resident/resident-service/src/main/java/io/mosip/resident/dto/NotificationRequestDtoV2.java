package io.mosip.resident.dto;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)

public class NotificationRequestDtoV2 extends NotificationRequestDto {

	private static final long serialVersionUID = -7927715532425021119L;
	private TemplateType templateType;
	private RequestType requestType;
	private String eventId;

	private String otp;

	public NotificationRequestDtoV2(TemplateType templateType, RequestType requestType, String eventId) {
		this.templateType = templateType;
		this.requestType = requestType;
		this.eventId = eventId;
	}

	public NotificationRequestDtoV2(TemplateType templateType, RequestType requestType, String eventId, String otp) {
		this.templateType = templateType;
		this.requestType = requestType;
		this.eventId = eventId;
		this.otp = otp;
	}



}