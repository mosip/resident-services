package io.mosip.resident.handler.service;

import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * The Interface ResidentConfigService.
 * @author Loganathan.S
 */
public interface ResidentConfigService {
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	ResponseWrapper<?> getUIProperties();

}
