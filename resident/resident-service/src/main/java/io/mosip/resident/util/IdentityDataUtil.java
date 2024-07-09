package io.mosip.resident.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.*;
import io.mosip.resident.dto.IdResponseDTO1;
import io.mosip.resident.dto.NotificationRequestDtoV2;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.IdRepoAppException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.NotificationService;
import io.mosip.resident.service.ProxyMasterdataService;
import io.mosip.resident.service.ProxyPartnerManagementService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 @author Kamesh Shekhar Prasad
 **/

@Component
public class IdentityDataUtil {

    private final Logger logger = LoggerConfiguration.logConfig(IdentityDataUtil.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProxyMasterdataService proxyMasterdataService;

    @Autowired
    private GetAcrMappingUtil getAcrMappingUtil;

    @Autowired
    @Lazy
    private ProxyPartnerManagementService proxyPartnerManagementService;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Autowired
    private CachedIdentityDataUtil cachedIdentityDataUtil;

    @Autowired
    private GetAccessTokenUtility getAccessToken;

    public void sendNotification(String eventId, String individualId, TemplateType templateType) {
        try {
            NotificationRequestDtoV2 notificationRequestDtoV2 = new NotificationRequestDtoV2();
            notificationRequestDtoV2.setTemplateType(templateType);
            notificationRequestDtoV2.setRequestType(RequestType.UPDATE_MY_UIN);
            notificationRequestDtoV2.setEventId(eventId);
            notificationRequestDtoV2.setId(individualId);
            notificationService.sendNotification(notificationRequestDtoV2, null);
        }catch (ResidentServiceCheckedException exception){
            logger.error("Error while sending notification:- "+ exception);
        }
    }

    @Cacheable(value = "getValidDocumentByLangCode", key = "#langCode")
    public  ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException {
        return proxyMasterdataService.getValidDocumentByLangCode(langCode);
    }

    @Cacheable(value = "partnerListCache", key = "#partnerType + '_' + #apiUrl")
    public ResponseWrapper<?> getPartnersByPartnerType(String partnerType, ApiName apiUrl) throws ResidentServiceCheckedException {
        return proxyPartnerManagementService.getPartnersByPartnerType(StringUtils.isBlank(partnerType) ? Optional.empty() : Optional.of(partnerType), apiUrl);
    }

    public Tuple3<JSONObject, String, IdResponseDTO1> getIdentityDataFromIndividualID(String individualId) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException {
        IdResponseDTO1 idResponseDto = retrieveIdRepoJsonIdResponseDto(individualId);
        JSONObject idRepoJson = convertIdResponseIdentityObjectToJsonObject(idResponseDto.getResponse().getIdentity());
        String schemaJson = getSchemaJsonFromIdRepoJson(idRepoJson);
        return Tuples.of(idRepoJson, schemaJson, idResponseDto);
    }

    public IdResponseDTO1 retrieveIdRepoJsonIdResponseDto(String uin) throws ApisResourceAccessException, IdRepoAppException, IOException {
        IdResponseDTO1 response = null;
        if (uin != null) {
            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                    "Utilities::retrieveIdrepoJson()::entry");
            List<String> pathSegments = new ArrayList<>();
            pathSegments.add(uin);
            IdResponseDTO1 idResponseDto;

            idResponseDto = (IdResponseDTO1) cachedIdentityDataUtil.getCachedIdentityData(uin, getAccessToken.getAccessToken(), IdResponseDTO1.class);
            if (idResponseDto == null) {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                        "Utilities::retrieveIdrepoJson()::exit idResponseDto is null");
                return null;
            }
            if (!idResponseDto.getErrors().isEmpty()) {
                List<ServiceError> error = idResponseDto.getErrors();
                logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                        "Utilities::retrieveIdrepoJson():: error with error message " + error.get(0).getMessage());
                throw new IdRepoAppException(error.get(0).getErrorCode(), error.get(0).getMessage());
            }

            response = idResponseDto;

            logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                    "Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
        }

        return response;
    }

    public String getSchemaJsonFromIdRepoJson(JSONObject idRepoJson) throws ResidentServiceCheckedException {
        String idSchemaVersionStr = String.valueOf(idRepoJson.get(ResidentConstants.ID_SCHEMA_VERSION));
        Double idSchemaVersion = Double.parseDouble(idSchemaVersionStr);
        ResponseWrapper<?> idSchemaResponse = proxyMasterdataService.getLatestIdSchema(idSchemaVersion, null, null);
        Object idSchema = idSchemaResponse.getResponse();
        Map<String, ?> map = objectMapper.convertValue(idSchema, Map.class);
        return ((String) map.get("schemaJson"));
    }

    public JSONObject convertIdResponseIdentityObjectToJsonObject(Object identityObject) throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(identityObject);
        logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                "Utilities::retrieveIdrepoJson():: IDREPOGETIDBYUIN GET service call ended Successfully");
        try {
            return (JSONObject) new JSONParser().parse(response);
        } catch (org.json.simple.parser.ParseException e) {
            logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.UIN.toString(), "",
                    ExceptionUtils.getStackTrace(e));
            throw new IdRepoAppException(ResidentErrorCode.RESIDENT_SYS_EXCEPTION.getErrorCode(), "Error while parsing string to JSONObject",e);
        }
    }

    public String getAuthTypeCodefromkey(String reqTypeCode) throws ResidentServiceCheckedException {
        Map<String, String> map = getAcrMappingUtil.getAmrAcrMapping();
        return map.get(reqTypeCode);
    }

}
