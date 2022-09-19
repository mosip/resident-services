package io.mosip.resident.service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.ResponseDTO;
import org.springframework.stereotype.Service;

/**
 * This class is service class of pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */
@Service
public interface PinUnpinStatusService {
	ResponseWrapper<ResponseDTO> pinStatus(String eventId, boolean status);

}
