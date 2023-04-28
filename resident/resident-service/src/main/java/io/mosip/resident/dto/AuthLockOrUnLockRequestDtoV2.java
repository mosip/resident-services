package io.mosip.resident.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;


@Data
@JsonPropertyOrder({ "authTypes" })
public class AuthLockOrUnLockRequestDtoV2 implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<AuthTypeStatusDtoV2> authTypes;

}