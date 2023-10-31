package io.mosip.resident.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
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
	
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentConfigServiceImpl.class);

	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */	
	@Override
	public ResponseWrapper<?> getUIProperties() {
		logger.debug("ResidentConfigServiceImpl::getUIProperties()::entry");
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
		logger.debug("ResidentConfigServiceImpl::getUIProperties()::exit");
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
		logger.debug("ResidentConfigServiceImpl::getUISchema()::entry");
		String uiSchema;
		Resource residentUiSchemaJsonFileRes = resourceLoader
				.getResource(String.format("%s-%s-schema.json", residentUiSchemaJsonFilePrefix, schemaType));
		if (residentUiSchemaJsonFileRes.exists()) {
			uiSchema = Utility.readResourceContent(residentUiSchemaJsonFileRes);
		} else {
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE);
		}
		logger.debug("ResidentConfigServiceImpl::getUISchema()::exit");
		return uiSchema;
	}

	@Override
	@Cacheable(value = "ui-schema-filtered-attributes", key = "#schemaType")
	public List<String> getUiSchemaFilteredInputAttributes(String schemaType) {
		logger.debug("ResidentConfigServiceImpl::getUiSchemaFilteredInputAttributes()::entry");
		List<Map<String, Object>> identityList = getUISchemaData(schemaType);
		List<String> uiSchemaFilteredInputAttributesList = identityList.stream().flatMap(map -> {
			List<String> attributeList = new ArrayList<>();
			attributeList.add((String) map.get(env.getProperty(UI_SCHEMA_ATTRIBUTE_NAME)));
			if (Boolean.valueOf(String.valueOf(map.get(ResidentConstants.MASK_REQUIRED)))) {
				attributeList.add(String.valueOf(map.get(ResidentConstants.MASK_ATTRIBUTE_NAME)));
			}
			if (Boolean.valueOf(String.valueOf(map.get(ResidentConstants.FORMAT_REQUIRED)))) {
				attributeList.addAll(
						((List<Map<String, String>>) ((Map<String, Object>) map.get(ResidentConstants.FORMAT_OPTION))
								.entrySet().stream().findFirst().get().getValue()).stream()
										.map(x -> x.get(ResidentConstants.VALUE)).collect(Collectors.toList()));
			}
			return attributeList.stream();
		}).distinct().collect(Collectors.toList());
		logger.debug("ResidentConfigServiceImpl::getUiSchemaFilteredInputAttributes()::exit");
		return uiSchemaFilteredInputAttributesList;
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
		logger.debug("ResidentConfigServiceImpl::getSharableAttributesList()::entry");
		// identity mapping json
		Map<String, Object> identityMap = getIdentityMappingMap();

		// ui schema share credential json
		List<Map<String, Object>> identityList = getUISchemaData(schemaType);
		List<String> idsListFromUISchema = identityList.stream().map(map -> String.valueOf(map.get(env.getProperty(UI_SCHEMA_ATTRIBUTE_NAME))))
				.collect(Collectors.toList());

		List<String> shareableAttributes = sharableAttrList.stream()
				.flatMap(attribute -> {
					List<String> attributeList = new ArrayList<>();
					// Get the attributes from the format if specified
					if(attribute.getFormat()!=null && !attribute.getFormat().isEmpty()) {
						attributeList.addAll(
								Stream.of(attribute.getFormat().split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER))
										.collect(Collectors.toList()));
					}
					// Get the attributes from the identity mapping
					else if(identityMap.containsKey(attribute.getAttributeName())) {
						attributeList.addAll(Stream.of(String.valueOf(((Map) identityMap.get(attribute.getAttributeName()))
														.get(MappingJsonConstants.VALUE))
												.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER))
										.collect(Collectors.toList()));
					}
					attributeList.add(attribute.getAttributeName());
					// Return the attribute name itself
					return attributeList.stream();
				})
				.filter(idsListFromUISchema::contains)
				.distinct()
				.collect(Collectors.toList());
		logger.debug("ResidentConfigServiceImpl::getSharableAttributesList()::exit");
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

	@Override
	public List<Map<String, Object>> getUISchemaData(String schemaType) {
		try {
			String uiSchema = getUISchema(schemaType);
			Map<String, Object> schemaMap = objectMapper.readValue(uiSchema.getBytes(StandardCharsets.UTF_8), Map.class);
			Object identitySchemaObj = schemaMap.get(MappingJsonConstants.IDENTITY);
			if(identitySchemaObj instanceof List) {
				List<Map<String, Object>> identityList = (List<Map<String, Object>>) identitySchemaObj;
				return identityList;
			} else {
				logger.error("Error occured in accessing ui-schema identity data");
				throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE);
			}
		} catch (IOException e) {
			logger.error("Error occured in getting ui-schema %s", e.getMessage());
			throw new ResidentServiceException(ResidentErrorCode.API_RESOURCE_UNAVAILABLE, e);
		}
	}

	@Override
	@Cacheable(value = "ui-schema-data-map", key = "#schemaType")
	public Map<String, Map<String, Map<String, Object>>> getUISchemaCacheableData(String schemaType) {
		List<Map<String, Object>> uiSchemaDataList = getUISchemaData(schemaType);
		List<String> languages = uiSchemaDataList.stream().map(map -> {
			return ((Map<String, String>) map.get(ResidentConstants.LABEL)).keySet().stream()
					.collect(Collectors.toList());
		}).findAny().orElse(List.of());
		Map<String, Map<String, Map<String, Object>>> schemaDataMap = languages.stream().map(langCode -> {
			return Map.entry(langCode, getUISchemaAttributesData(uiSchemaDataList, langCode));
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return schemaDataMap;
	}

	private Map<String, Map<String, Object>> getUISchemaAttributesData(List<Map<String, Object>> uiSchemaDataList,
			String langCode) {
		Map<String, Map<String, Object>> attributesDataMap = new HashMap<>();
		attributesDataMap = uiSchemaDataList.stream().map(map -> {
			return Map.entry((String) map.get(ResidentConstants.ATTRIBUTE_NAME),
					Map.of(ResidentConstants.LABEL, ((Map<String, String>) map.get(ResidentConstants.LABEL)).get(langCode),
							ResidentConstants.FORMAT_OPTION, getAttributeFormatData(map, langCode)));
		}).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return attributesDataMap;
	}

	private Map<String, String> getAttributeFormatData(Map<String, Object> map, String langCode) {
		return map.entrySet().stream().filter(formatRequired -> formatRequired.getKey().equals(ResidentConstants.FORMAT_REQUIRED))
				.filter(formatCheck -> (boolean) formatCheck.getValue())
				.map(formatData -> {
					return ((List<Map<String, String>>) ((Map<String, Object>) map.get(ResidentConstants.FORMAT_OPTION))
							.get(langCode))
									.stream()
									.map(map1 -> Map.entry(map1.get(ResidentConstants.VALUE),
											map1.get(ResidentConstants.LABEL)))
									.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
				}).findAny().orElse(Map.of());
	}
}
