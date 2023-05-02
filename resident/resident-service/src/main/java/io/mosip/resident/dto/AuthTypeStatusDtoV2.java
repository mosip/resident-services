package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AuthTypeStatusDtoV2 extends AuthTypeStatusDto {

	private String authSubType;

}