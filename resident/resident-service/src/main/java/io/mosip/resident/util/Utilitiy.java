package io.mosip.resident.util;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.MappingJsonConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.JsonValue;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import org.assertj.core.util.Lists;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author Girish Yarru
 * @version 1.0
 */

@Component
public class Utilitiy {

    private static final Logger logger = LoggerConfiguration.logConfig(Utilitiy.class);

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Value("${config.server.file.storage.uri}")
    private String configServerFileStorageURL;

    @Value("${resident.identityjson}")
    private String getRegProcessorIdentityJson;

    @Value("${mosip.primary-language}")
    private String primaryLang;

    @Value("${mosip.secondary-language}")
    private String secondaryLang;

    @Value("${mosip.notification.language-type}")
    private String languageType;

    @Autowired
    private RestTemplate residentRestTemplate;

    @Autowired
    private Environment env;

    private static final String IDENTITY = "identity";
    private static final String VALUE = "value";
    private static String regProcessorIdentityJson = "";

    @PostConstruct
    private void loadRegProcessorIdentityJson() {
        regProcessorIdentityJson = residentRestTemplate.getForObject(configServerFileStorageURL + getRegProcessorIdentityJson, String.class);
        logger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                LoggerFileConstant.APPLICATIONID.toString(), "loadRegProcessorIdentityJson completed successfully");
    }

    @SuppressWarnings("unchecked")
    public JSONObject retrieveIdrepoJson(String id) throws ResidentServiceCheckedException {
        logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                "Utilitiy::retrieveIdrepoJson()::entry");
        List<String> pathsegments = new ArrayList<>();
        pathsegments.add(id);
        ResponseWrapper<IdRepoResponseDto> response = null;
        try {
            response = (ResponseWrapper<IdRepoResponseDto>) residentServiceRestClient.getApi(
                    ApiName.IDREPOGETIDBYUIN, pathsegments, "", null, ResponseWrapper.class,
                    tokenGenerator.getToken());

        } catch (IOException e) {
            throw new ResidentServiceCheckedException(ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorCode(),
                    ResidentErrorCode.TOKEN_GENERATION_FAILED.getErrorMessage(), e);
        } catch (ApisResourceAccessException e) {
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
                throw new ResidentServiceCheckedException(
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                        httpClientException.getResponseBodyAsString());

            } else if (e.getCause() instanceof HttpServerErrorException) {
                HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
                throw new ResidentServiceCheckedException(
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                        httpServerException.getResponseBodyAsString());
            } else {
                throw new ResidentServiceCheckedException(
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage() + e.getMessage(), e);
            }
        }

        return retrieveErrorCode(response, id);
    }

    public JSONObject retrieveErrorCode(ResponseWrapper<IdRepoResponseDto> response, String id)
            throws ResidentServiceCheckedException {
        ResidentErrorCode errorCode;
        errorCode = ResidentErrorCode.INVALID_ID;
        try {
            logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                    "Utilitiy::retrieveIdrepoJson()::id repo response for given id::"
                            + JsonUtil.writeValueAsString(response));
            if (response == null)
                throw new IdRepoAppException(errorCode.getErrorCode(), errorCode.getErrorMessage(),
                        "In valid response while requesting ID Repositary");
            if (!response.getErrors().isEmpty()) {
                List<ServiceError> error = response.getErrors();
                throw new IdRepoAppException(errorCode.getErrorCode(), errorCode.getErrorMessage(),
                        error.get(0).getMessage());
            }
            String jsonResponse;

            jsonResponse = JsonUtil.writeValueAsString(response.getResponse());
            JSONObject json = JsonUtil.readValue(jsonResponse, JSONObject.class);
            logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                    "Utilitiy::retrieveIdrepoJson()::exit");
            return JsonUtil.getJSONObject(json, "identity");
        } catch (IOException e) {
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMailingAttributes(String id, Set<String> templateLangauges) throws ResidentServiceCheckedException {
        logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                "Utilitiy::getMailingAttributes()::entry");
        Map<String, Object> attributes = new HashMap<>();
        String mappingJsonString = getMappingJson();
        if (mappingJsonString == null || mappingJsonString.trim().isEmpty()) {
            throw new ResidentServiceException(ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.JSON_PROCESSING_EXCEPTION.getErrorMessage());
        }
        JSONObject mappingJsonObject;
        try {
            JSONObject demographicIdentity = retrieveIdrepoJson(id);
            mappingJsonObject = JsonUtil.readValue(mappingJsonString, JSONObject.class);
            JSONObject mapperIdentity = JsonUtil.getJSONObject(mappingJsonObject, IDENTITY);
            List<String> mapperJsonKeys = new ArrayList<>(mapperIdentity.keySet());

            String preferredLanguage = getPreferredLanguage(demographicIdentity);
            if (StringUtils.isEmpty(preferredLanguage)) {
                List<String> defaultTemplateLanguages = getDefaultTemplateLanguages();
                if (CollectionUtils.isEmpty(defaultTemplateLanguages)) {
                    Set<String> dataCapturedLanguages = getDataCapturedLanguages(mapperIdentity, demographicIdentity);
                    templateLangauges.addAll(dataCapturedLanguages);
                } else {
                    templateLangauges.addAll(defaultTemplateLanguages);
                }
            } else {
                templateLangauges.add(preferredLanguage);
            }

            for (String key : mapperJsonKeys) {
                LinkedHashMap<String, String> jsonObject = JsonUtil.getJSONValue(mapperIdentity, key);
                String values = jsonObject.get(VALUE);
                for (String value : values.split(",")) {
                    Object object = demographicIdentity.get(value);
                    if (object instanceof ArrayList) {
                        JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
                        JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
                        for (JsonValue jsonValue : jsonValues) {
                            if (templateLangauges.contains(jsonValue.getLanguage()))
                                attributes.put(value + "_" + jsonValue.getLanguage(), jsonValue.getValue());
                        }
                    } else if (object instanceof LinkedHashMap) {
                        JSONObject json = JsonUtil.getJSONObject(demographicIdentity, value);
                        attributes.put(value, (String) json.get(VALUE));
                    } else {
                        attributes.put(value, String.valueOf(object));
                    }
                }
            }

            logger.info(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                    "Utilitiy::getMailingAttributes()::mailingAttributes::" + attributes);
        } catch (IOException | ReflectiveOperationException e) {
            throw new ResidentServiceCheckedException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorMessage(), e);
        }
        logger.debug(LoggerFileConstant.APPLICATIONID.toString(), LoggerFileConstant.UIN.name(), id,
                "Utilitiy::getMailingAttributes()::exit");
        return attributes;
    }

    private String getPreferredLanguage(JSONObject demographicIdentity) {
        String preferredLang = null;
        String preferredLangAttribute = env.getProperty("mosip.default.user-preferred-language-attribute");
        if (!StringUtils.isEmpty(preferredLangAttribute)) {
            Object object = demographicIdentity.get(preferredLangAttribute);
            if (object != null) {
                preferredLang = String.valueOf(object);
            }
        }
        return preferredLang;
    }

    private Set<String> getDataCapturedLanguages(JSONObject mapperIdentity, JSONObject demographicIdentity)
            throws ReflectiveOperationException {
        Set<String> dataCapturedLangauges = new HashSet<String>();
        LinkedHashMap<String, String> jsonObject = JsonUtil.getJSONValue(mapperIdentity, MappingJsonConstants.NAME);
        String values = jsonObject.get(VALUE);
        for (String value : values.split(",")) {
            Object object = demographicIdentity.get(value);
            if (object instanceof ArrayList) {
                JSONArray node = JsonUtil.getJSONArray(demographicIdentity, value);
                JsonValue[] jsonValues = JsonUtil.mapJsonNodeToJavaObject(JsonValue.class, node);
                for (JsonValue jsonValue : jsonValues) {
                    dataCapturedLangauges.add(jsonValue.getLanguage());
                }
            }
        }
        return dataCapturedLangauges;
    }

    private List<String> getDefaultTemplateLanguages() {
        String defaultLanguages = env.getProperty("mosip.default.template-languages");
        if (!StringUtils.isEmpty(defaultLanguages)) {
            String[] lanaguages = defaultLanguages.split(",");
            List<String> strList = Lists.newArrayList(lanaguages);
            return strList;
        }
        return null;
    }

    public String getMappingJson() {
        if (StringUtils.isEmpty(regProcessorIdentityJson)) {
            return residentRestTemplate.getForObject(configServerFileStorageURL + getRegProcessorIdentityJson, String.class);
        }
        return regProcessorIdentityJson;
    }

}
