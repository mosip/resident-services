package io.mosip.resident.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import io.mosip.kernel.core.exception.ServiceError;
import lombok.Data;

@Data
public class ResponseWrapper<T> {
	private String id;
	private String version;
	private String responsetime;
	@NotNull
	@Valid
	private T response;

	private List<ServiceError> errors = new ArrayList<>();

}
