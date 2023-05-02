package io.mosip.resident.dto;

public interface IVidRequestDto<T extends BaseVidRequestDto> {
	T getRequest();

	String getRequesttime();

	String getId();

	String getVersion();
}
