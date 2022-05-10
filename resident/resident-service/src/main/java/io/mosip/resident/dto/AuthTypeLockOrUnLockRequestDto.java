/**
 * 
 */
package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Kamesh Shekhar Prasad
 *
 */
@Data
@JsonPropertyOrder({ "transactionID", "individualId", "individualIdType", "authType" })
public class AuthTypeLockOrUnLockRequestDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String transactionID;
	private String individualId;
	private List<String> authType;

}
