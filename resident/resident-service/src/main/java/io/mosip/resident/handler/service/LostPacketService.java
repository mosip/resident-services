/**
 * 
 */
package io.mosip.resident.handler.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.LostRequestDto;
import io.mosip.resident.dto.LostResponseDto;

/**
 * The Interface LostPacketService.
 *
 * @author M1022006
 */
public interface LostPacketService {

	/**
	 * Gets the id value.
	 *
	 * @param lostRequestDto
	 *            the lost packet request dto
	 * @return the id value
	 */
	public LostResponseDto getIdValue(LostRequestDto lostRequestDto) throws BaseCheckedException;

}
