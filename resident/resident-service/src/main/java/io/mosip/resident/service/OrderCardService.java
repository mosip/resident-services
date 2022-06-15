package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;

/**
 * Order card service class.
 * 
 * @author Ritik Jain
 */
@Service
public interface OrderCardService {

	/**
	 * Send a physical card.
	 * 
	 * @param requestDto
	 * @return responseWrapper<ResidentCredentialResponseDto> object
	 * @throws ResidentServiceCheckedException
	 */
	public ResidentCredentialResponseDto sendPhysicalCard(ResidentCredentialRequestDto requestDto)
			throws ResidentServiceCheckedException;

	/**
	 * Check order status.
	 * 
	 * @param transactionId
	 * @param individualId
	 * @throws ResidentServiceCheckedException
	 */
	public void checkOrderStatus(String transactionId, String individualId) throws ResidentServiceCheckedException;

}
