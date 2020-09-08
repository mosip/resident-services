package io.mosip.resident.handler.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.PacketGeneratorResDto;

import java.io.IOException;

/**
 * The Interface PacketGeneratorService.
 * 
 * @author Sowmya
 */
public interface PacketGeneratorService<T> {

	/**
	 * Creates the packet.
	 *
	 * @param request
	 *            the request
	 * @return the packer generator res dto
	 */
	public PacketGeneratorResDto createPacket(T request) throws BaseCheckedException,IOException;

}
