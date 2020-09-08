package io.mosip.resident.handler.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;

import java.io.IOException;
import java.text.ParseException;

/**
 * Class for creating the Resident Registration
 * 
 * @author Sowmya
 * 
 *
 */
public interface PacketCreationService {

	/**
	 * Creates the packet
	 * 
	 * @param packetDto
	 *            the enrollment data for which packet has to be created
	 * @throws BaseCheckedException
	 * @throws ParseException 
	 * @throws IdObjectIOException 
	 * @throws IdObjectValidationFailedException 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	byte[] create(PacketDto packetDto, String centerId, String machineId) throws BaseCheckedException, ParseException, IdObjectValidationFailedException, IdObjectIOException, JsonParseException, JsonMappingException, IOException;

}
