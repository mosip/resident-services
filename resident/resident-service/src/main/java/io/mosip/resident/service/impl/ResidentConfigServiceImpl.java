package io.mosip.resident.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.util.IOUtils;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;

/**
 * The Class ResidentConfigServiceImpl.
 * @author Loganathan.S
 */
@Component
public class ResidentConfigServiceImpl implements ResidentConfigService {
	
	/** The Constant logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentConfigServiceImpl.class);
	
	/** The prop keys. */
	@Value("${resident.ui.propertyKeys:}")
	private String[] propKeys;
	
	/** The env. */
	@Autowired
	private Environment env;

	
	/** The resident ui schema json file. */
	@Value("${resident-ui-schema-file-source}")
	private Resource residentUiSchemaJsonFile;
	
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

	/**
	 * Gets the UI schema.
	 *
	 * @return the UI schema
	 */
	@Override
	public String getUISchema() {
		return readResourceContent(residentUiSchemaJsonFile);
	}

	/**
	 * Read resource content.
	 *
	 * @param resFile the res file
	 * @return the string
	 */
	private String readResourceContent(Resource resFile) {
		try {
			return IOUtils.readInputStreamToString(resFile.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error(e.getMessage());
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION, e);
		}
	}

}
