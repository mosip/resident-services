package io.mosip.resident.util;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Query;

/**
 * class for creating Positional Parameter for nativeQuery in service-history API
 * 
 * @author Aiham Hasan
 */
public class PositionalParams {
	private int count = 0;
	private Map<Integer, Object> paramMap = new LinkedHashMap<>();
	
	public void applyParams(Query nativeQuery ) {
		paramMap.entrySet().forEach(entry->nativeQuery.setParameter(entry.getKey(), entry.getValue()));	
	}

	public String add(Object value) {
		count = count + 1;
		paramMap.put(count, value);
		return "?"+count ;
	}
}