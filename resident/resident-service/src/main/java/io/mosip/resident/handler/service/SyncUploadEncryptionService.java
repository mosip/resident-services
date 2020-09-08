package io.mosip.resident.handler.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.PacketGeneratorResDto;

/**
 * The Interface SyncUploadEncryptionService.
 * 
 * @author Sowmya
 */
public interface SyncUploadEncryptionService {

	/**
	 * Upload uin packet.
	 *
	 * @param packetZipBytes
	 *            the uin zip file
	 * @param registrationId
	 *            the registration id
	 * @param creationTime
	 *            the creation time
	 * @return the packer generator res dto
	 */
	PacketGeneratorResDto uploadUinPacket(String registrationId, String creationTime, String regType, byte[] packetZipBytes)
			throws BaseCheckedException;

}