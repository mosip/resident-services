package io.mosip.resident.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * This class is service class of pin or unpin status api based on event id.
 * @Author Kamesh Shekhar Prasad
 */
@Service
public interface PinUnpinStatusService {
	ResponseEntity<?> pinStatus(String eventId, boolean status);

}
