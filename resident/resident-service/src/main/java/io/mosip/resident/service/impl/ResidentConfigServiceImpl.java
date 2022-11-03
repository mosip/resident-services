package io.mosip.resident.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilitiy;

/**
 * The Class ResidentConfigServiceImpl.
 * @author Loganathan.S
 */
@Component
public class ResidentConfigServiceImpl implements ResidentConfigService {
	
	private static final String ID = "id";

	private static final String CONTROL_TYPE = "controlType";

	private static final String FILEUPLOAD = "fileupload";

	private static final String INPUT_REQUIRED = "inputRequired";

	private static final String IDENTITY = "identity";

	/** The Constant logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentConfigServiceImpl.class);
	
	/** The prop keys. */
	@Value("${resident.ui.propertyKeys:}")
	private String[] propKeys;
	
	/** The env. */
	@Autowired
	private Environment env;

	/** The audit util. */
	@Autowired
	private AuditUtil auditUtil;
	
	/** The resident ui schema json file. */
	@Value("${resident-ui-schema-file-source-prefix}")
	private String residentUiSchemaJsonFilePrefix;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
	/** The identity mapping json file. */
	@Value("${identity-mapping-file-source}")
	private Resource identityMappingJsonFile;
	
	private String identityMapping;
	
	@Autowired
	private ObjectMapper objectMapper;

	private List<String> uiSchemaFilteredInputAttributes;
	
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
	@Cacheable("ui-schema")
	public String getUISchema(String schemaType) {
		String uiSchema;
		Resource residentUiSchemaJsonFileRes = resourceLoader
				.getResource(String.format("%s-%s-schema.json", residentUiSchemaJsonFilePrefix, schemaType));
		if (residentUiSchemaJsonFileRes.exists()) {
			uiSchema = Utilitiy.readResourceContent(residentUiSchemaJsonFileRes);
		} else {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE);
		}
		return uiSchema;
	}

	@Override
	public List<String> getUiSchemaFilteredInputAttributes(String schemaType) throws JsonParseException, JsonMappingException, IOException {
		if(uiSchemaFilteredInputAttributes == null) {
			uiSchemaFilteredInputAttributes = doGetUiSchemaFilteredInputAttributes(schemaType);
		}
		return uiSchemaFilteredInputAttributes;
		
	}
	
	private List<String> doGetUiSchemaFilteredInputAttributes(String schemaType) throws JsonParseException, JsonMappingException, IOException {
		String uiSchema = getUISchema(schemaType);
		Map<String, Object> schemaMap = objectMapper.readValue(uiSchema.getBytes(StandardCharsets.UTF_8), Map.class);
		Object identityObj = schemaMap.get(IDENTITY);
		if(identityObj instanceof List) {
			List<Map<String, Object>> identityList = (List<Map<String, Object>>) identityObj;
			List<String> uiSchemaFilteredInputAttributesList = identityList.stream()
						.filter(map -> Boolean.valueOf(String.valueOf(map.get(INPUT_REQUIRED))))
						.filter(map -> !FILEUPLOAD.equals(map.get(CONTROL_TYPE)))
						.map(map -> (String)map.get(ID))
						.collect(Collectors.toList());
			return uiSchemaFilteredInputAttributesList;
		}
		return null;
		
	}

	@Override
	public String getIdentityMapping() throws ResidentServiceCheckedException {
		if(identityMapping==null) {
			identityMapping=Utilitiy.readResourceContent(identityMappingJsonFile);
		}
		return identityMapping;
	}

}
