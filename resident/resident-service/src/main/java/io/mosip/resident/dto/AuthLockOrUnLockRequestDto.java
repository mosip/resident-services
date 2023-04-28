/**
 * 
 */
package io.mosip.resident.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * @author M1022006
 *
 */
@Data
@JsonPropertyOrder({ "transactionID", "individualId", "individualIdType", "otp", "authTypes" })
public class AuthLockOrUnLockRequestDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String transactionID;
	private String individualId;
    private String otp;
	private List<String> authType;

}
