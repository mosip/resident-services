package io.mosip.resident.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class VidRequestDtoV2 extends BaseVidRequestDto {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -886400737912892865L;
	private List<String> channels;

}
