package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class ResidentVidRequestDto extends BaseRequestDTO implements IVidRequestDto<VidRequestDto> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3674725539147720447L;
	private VidRequestDto request;
    
}
