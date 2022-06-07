package io.mosip.resident.dto;

import lombok.Data;

@Data
public class AuthTypeStatusDto {

	private String authType;
	private boolean locked;
	private String unlockForSeconds;
	public String uin;

	public boolean getLocked() {
		return locked;
	}
}
