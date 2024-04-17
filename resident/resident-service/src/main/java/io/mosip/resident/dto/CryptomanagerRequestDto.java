/*
 * 
 * 
 * 
 * 
 */
package io.mosip.resident.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Crypto-Manager-Request model
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Crypto-Manager-Service Request")
public class CryptomanagerRequestDto {
	/**
	 * Application id of decrypting module
	 */

	private String applicationId;
	/**
	 * Refrence Id
	 */

	private String referenceId;
	/**
	 * Timestamp
	 */

	@NotNull
	private LocalDateTime timeStamp;
	/**
	 * Data in BASE64 encoding to encrypt/decrypt
	 */

	private String data;
	
	private Boolean prependThumbprint;

	private String aad;
	
	private String salt;

}
