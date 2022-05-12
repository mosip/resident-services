package io.mosip.resident.handler.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseWrapper;

/**
 * The Interface ResidentConfigService.
 * 
 * @author Loganathan.S
 */
public interface ResidentConfigService {
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	ResponseWrapper<?> getUIProperties();

	/**
	 * Gets the UI schema.
	 *
	 * @return the UI schema
	 */
	String getUISchema();

	/**
	 * Gets the ui schema filtered input attributes.
	 *
	 * @return the ui schema filtered input attributes
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	List<String> getUiSchemaFilteredInputAttributes() throws JsonParseException, JsonMappingException, IOException;

}
