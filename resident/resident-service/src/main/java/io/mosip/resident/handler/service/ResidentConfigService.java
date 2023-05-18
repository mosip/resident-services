package io.mosip.resident.handler.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;

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
	String getUISchema(String schemaType);

	/**
	 * Gets the ui schema filtered input attributes.
	 *
	 * @return the ui schema filtered input attributes
	 * @throws JsonParseException the json parse exception
	 * @throws JsonMappingException the json mapping exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	List<String> getUiSchemaFilteredInputAttributes(String schemaType) throws JsonParseException, JsonMappingException, IOException;
	
	/**
	 * Gets the Identity Mapping json
	 * @return identity-mapping
	 * @throws ResidentServiceCheckedException
	 */
	String getIdentityMapping() throws ResidentServiceCheckedException;

	List<Map<String, Object>> getUISchemaData(String schemaType);

	/**
	 * Gets Cacheable UI Schema data
	 *
	 * @return the UI Schema data.
	 */
	Map<String, Map<String, Map<String, Object>>> getUISchemaCacheableData(String schemaType);

}
