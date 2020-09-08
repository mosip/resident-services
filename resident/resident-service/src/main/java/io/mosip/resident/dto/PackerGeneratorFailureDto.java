package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Sowmya The Class PackerGeneratorFailureDto.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PackerGeneratorFailureDto extends PacketGeneratorResDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -5397672054150780651L;

	/** The errorCode. */
	private String errorCode;

	/**
	 * Instantiates a new packer generator failure dto.
	 */
	public PackerGeneratorFailureDto() {
		super();

	}

}
