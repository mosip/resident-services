package io.mosip.resident.util;

import java.util.Map;

/**
 * @author Ritik Jain
 */
public interface ObjectWithMetadata {
	
	public Map<String, Object> getMetadata();

	public void setMetadata(Map<String, Object> metadata);

}
