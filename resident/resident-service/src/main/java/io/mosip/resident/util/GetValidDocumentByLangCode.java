package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.impl.ProxyMasterdataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GetValidDocumentByLangCode {

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    /**
     * Get valid documents by language code.
     *
     * @param langCode
     * @return ResponseWrapper object
     * @throws ResidentServiceCheckedException
     */
    public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException {
        logger.debug("GetValidDocumentByLangCode::getValidDocumentByLangCode()::entry");
        ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
        Map<String, String> pathsegments = new HashMap<String, String>();
        pathsegments.put("langCode", langCode);
        try {
            responseWrapper = residentServiceRestClient.getApi(ApiName.VALID_DOCUMENT_BY_LANGCODE_URL, pathsegments,
                    ResponseWrapper.class);

            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()) {
                logger.error(responseWrapper.getErrors().get(0).toString());
                throw new ResidentServiceCheckedException(ResidentErrorCode.BAD_REQUEST.getErrorCode(),
                        responseWrapper.getErrors().get(0).getMessage());
            }
        } catch (ApisResourceAccessException e) {
            logger.error("Error occured in accessing valid documents %s", e.getMessage());
            throw new ResidentServiceCheckedException(ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        }
        logger.debug("GetValidDocumentByLangCode::getValidDocumentByLangCode()::exit");
        return responseWrapper;
    }
}
