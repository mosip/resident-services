package io.mosip.resident.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VidResponseDTO1 extends BaseRestResponseDTO implements Serializable{
	
	private static final long serialVersionUID = -3604571018699722626L;

	private String str;
	
	private String metadata;
	
	private VidResDTO response;
	
	private List<ErrorDTO> errors;

}
