package io.mosip.resident.dto;

import lombok.Data;

@Data
public class AuthTypeStatusDto {

	private String authType;
	private boolean locked;
	private Long unlockForSeconds;
	public boolean getLocked() {
		return locked;
	}
}
