package io.mosip.resident.service.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.handler.service.ResidentConfigService;

/**
 * The Class ResidentConfigServiceImpl.
 * @author Loganathan.S
 */
@Component
public class ResidentConfigServiceImpl implements ResidentConfigService {
	
	/** The prop keys. */
	@Value("${resident.ui.propertyKeys:}")
	private String[] propKeys;
	
	/** The env. */
	@Autowired
	private Environment env;

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */	
	@Override
	public ResponseWrapper<?> getUIProperties() {
		ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
		Map<String, Object> properties = Arrays.stream(propKeys)
			.filter(StringUtils::isNotBlank)
			.map(key -> {
				Object property = env.getProperty(key, Object.class);
				if(property != null) {
					return Map.entry(key, env.getProperty(key, Object.class));
				}
				return null;
			})
			.filter(entry -> entry != null && entry.getValue() != null)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		responseWrapper.setResponse(properties);
		return responseWrapper;
	}

}
