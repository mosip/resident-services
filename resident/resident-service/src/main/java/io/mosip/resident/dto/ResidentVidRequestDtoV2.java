package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ResidentVidRequestDtoV2 extends BaseRequestDTO implements IVidRequestDto<VidRequestDtoV2> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1864072225994404946L;
	private VidRequestDtoV2 request;
    
}
