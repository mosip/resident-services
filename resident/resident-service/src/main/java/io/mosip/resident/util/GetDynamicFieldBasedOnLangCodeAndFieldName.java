package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetDynamicFieldBasedOnLangCodeAndFieldName {

    private static final Logger logger = LoggerConfiguration.logConfig(GetDynamicFieldBasedOnLangCodeAndFieldName.class);

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    /**
     * Get gender types by language code.
     *
     * @param fieldName
     * @param langCode
     * @param withValue
     * @return ResponseWrapper object
     * @throws ResidentServiceCheckedException
     */

    public ResponseWrapper<?> getDynamicFieldBasedOnLangCodeAndFieldName(String fieldName, String langCode,
                                                                         boolean withValue) throws ResidentServiceCheckedException {
        logger.debug("GetDynamicFieldBasedOnLangCodeAndFieldName::getDynamicFieldBasedOnLangCodeAndFieldName()::entry");
        ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
        Map<String, String> pathsegments = new HashMap<String, String>();
        pathsegments.put("langcode", langCode);
        pathsegments.put("fieldName", fieldName);
        List<String> queryParamName = new ArrayList<String>();
        queryParamName.add("withValue");

        List<Object> queryParamValue = new ArrayList<>();
        queryParamValue.add(withValue);
        try {
            responseWrapper = residentServiceRestClient.getApi(ApiName.DYNAMIC_FIELD_BASED_ON_LANG_CODE_AND_FIELD_NAME, pathsegments, queryParamName,
                    queryParamValue, ResponseWrapper.class);
            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
                logger.error(responseWrapper.getErrors().get(0).toString());
                throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
                        responseWrapper.getErrors().get(0).getMessage());
            }
        } catch (ApisResourceAccessException e) {
            logger.error("Error occured in accessing dynamic data %s", e.getMessage());
            throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        }
        logger.debug("GetDynamicFieldBasedOnLangCodeAndFieldName::getDynamicFieldBasedOnLangCodeAndFieldName()::exit");
        return responseWrapper;
    }
}
