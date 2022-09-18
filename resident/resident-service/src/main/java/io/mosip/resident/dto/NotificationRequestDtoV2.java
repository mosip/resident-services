package io.mosip.resident.dto;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDtoV2 extends NotificationRequestDto {

	private static final long serialVersionUID = -7927715532425021119L;
	private TemplateType templateType;
	private RequestType requestType;
	private String eventId;
}