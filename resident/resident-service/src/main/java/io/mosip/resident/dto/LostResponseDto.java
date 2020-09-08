/**
 * 
 */
package io.mosip.resident.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author M1022006
 *
 */
@Data
public class LostResponseDto implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 4000123785627519447L;

	/** The id Value. */
	private String idValue;
}
