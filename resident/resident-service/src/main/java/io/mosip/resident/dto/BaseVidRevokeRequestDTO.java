package io.mosip.resident.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class BaseVidRevokeRequestDTO implements Serializable, ObjectWithTransactionID {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 583135967031612906L;
	private String transactionID;
	private String vidStatus;
}
