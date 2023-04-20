package io.mosip.resident.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.SharableAttributesDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.handler.service.ResidentConfigService;
import io.mosip.resident.util.Utility;

/**
 * The Class ResidentConfigServiceImpl.
 * @author Loganathan.S
 */
@Component
public class ResidentConfigServiceImpl implements ResidentConfigService {

	private static final String UI_SCHEMA_ATTRIBUTE_NAME = "mosip.resident.schema.attribute-name";

	/** The prop keys. */
	@Value("${resident.ui.propertyKeys:}")
	private String[] propKeys;
	
	/** The env. */
	@Autowired
	private Environment env;

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

	@Value("${resident.ui.properties.id}")
	private String residentUiPropertiesId;
	
	@Value("${resident.ui.properties.version}")
	private String residentUiPropertiesVersion;
	
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
		responseWrapper.setId(residentUiPropertiesId);
		responseWrapper.setVersion(residentUiPropertiesVersion);
		return responseWrapper;
	}

	/**
	 * Gets the UI schema.
	 *
	 * @return the UI schema
	 */
	@Override
	@Cacheable(value="ui-schema", key="#schemaType")
	public String getUISchema(String schemaType) {
		String uiSchema;
		Resource residentUiSchemaJsonFileRes = resourceLoader
				.getResource(String.format("%s-%s-schema.json", residentUiSchemaJsonFilePrefix, schemaType));
		if (residentUiSchemaJsonFileRes.exists()) {
			uiSchema = Utility.readResourceContent(residentUiSchemaJsonFileRes);
		} else {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE);
		}
		return uiSchema;
	}

	@Override
	@Cacheable(value="ui-schema-filtered-attributes", key="#schemaType")
	public List<String> getUiSchemaFilteredInputAttributes(String schemaType) throws JsonParseException, JsonMappingException, IOException {
		return doGetUiSchemaFilteredInputAttributes(schemaType);
	}
	
	private List<String> doGetUiSchemaFilteredInputAttributes(String schemaType) throws JsonParseException, JsonMappingException, IOException {
		String uiSchema = getUISchema(schemaType);
		Map<String, Object> schemaMap = objectMapper.readValue(uiSchema.getBytes(StandardCharsets.UTF_8), Map.class);
		Object identityObj = schemaMap.get(MappingJsonConstants.IDENTITY);
		if(identityObj instanceof List) {
			List<Map<String, Object>> identityList = (List<Map<String, Object>>) identityObj;
			List<String> uiSchemaFilteredInputAttributesList = identityList.stream()
						.flatMap(map -> {
							String attribName = (String)map.get(env.getProperty(UI_SCHEMA_ATTRIBUTE_NAME));
							if(Boolean.valueOf(String.valueOf(map.get(ResidentConstants.MASK_REQUIRED)))) {
								//Include the attribute and its masked attribute
								return Stream.of(attribName, ResidentConstants.MASK_PREFIX + attribName);
							} else {
								return Stream.of(attribName);
							}
						})
						.collect(Collectors.toList());
			return uiSchemaFilteredInputAttributesList;
		}
		return null;
		
	}

	@Override
	public String getIdentityMapping() throws ResidentServiceCheckedException {
		if(identityMapping==null) {
			identityMapping=Utility.readResourceContent(identityMappingJsonFile);
		}
		return identityMapping;
	}
	
	public List<String> getSharableAttributesList(List<SharableAttributesDTO> sharableAttrList, String schemaType)
			throws ResidentServiceCheckedException, JsonParseException, JsonMappingException, IOException {
		
		// identity mapping json
		Map<String, Object> identityMap = getIdentityMappingMap();

		// ui schema share credential json
		String uiSchema = getUISchema(schemaType);
		Map<String, Object> schemaMap = objectMapper.readValue(uiSchema.getBytes(StandardCharsets.UTF_8), Map.class);
		Object identitySchemaObj = schemaMap.get(MappingJsonConstants.IDENTITY);
		List<Map<String, Object>> identityList = (List<Map<String, Object>>) identitySchemaObj;
		List<String> idsListFromUISchema = identityList.stream().map(map -> String.valueOf(map.get(env.getProperty(UI_SCHEMA_ATTRIBUTE_NAME))))
				.collect(Collectors.toList());

		List<String> shareableAttributes = sharableAttrList.stream()
				.flatMap(attribute -> {
					// Get the attributes from the format if specified
					if(attribute.getFormat()!=null && !attribute.getFormat().isEmpty()) {
						return Stream.of(attribute.getFormat().split(","));
					}
					// Get the attributes from the identity mapping
					if(identityMap.containsKey(attribute.getAttributeName())) {
						return Stream.of(String.valueOf(((Map) identityMap.get(attribute.getAttributeName())).get(MappingJsonConstants.VALUE))
								.split(","));
					}
					// Return the attribute name itself
					return Stream.of(attribute.getAttributeName());
				})
				.filter(idsListFromUISchema::contains)
				.collect(Collectors.toList());

		return shareableAttributes;
	}

	public Map<String, Object> getIdentityMappingMap()
			throws ResidentServiceCheckedException, IOException, JsonParseException, JsonMappingException {
		String identityMapping = getIdentityMapping();
		Map<String, Object> identityMappingMap = objectMapper
				.readValue(identityMapping.getBytes(StandardCharsets.UTF_8), Map.class);
		Object identityObj = identityMappingMap.get(MappingJsonConstants.IDENTITY);
		Map<String, Object> identityMap = (Map<String, Object>) identityObj;
		return identityMap;
	}

}
