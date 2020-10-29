package io.mosip.resident.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Sowmya
 * 
 */
@Data
public class MachineResponseDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4266319575132486164L;
	private List<MachineDto> machines;
}
