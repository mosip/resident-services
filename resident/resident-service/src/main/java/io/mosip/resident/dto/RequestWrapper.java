package io.mosip.resident.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class RequestWrapper<T> {
	private String id;
	private String version;
	private String requesttime;

	@NotNull(message = "request should not be null")
	@Valid
	private T request;
}
