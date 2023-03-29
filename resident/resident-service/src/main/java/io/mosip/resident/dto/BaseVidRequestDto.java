package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class BaseVidRequestDto implements ObjectWithTransactionID, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8904379407961043321L;
	private String transactionID;
    private String vidType;

}
