package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.AvailableClaimUtility;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.HashMap;
import java.util.stream.Collectors;

import static io.mosip.resident.constant.ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION;

@Component
public class GetRemainingUpdateCountByIndividualId {

    private static final Logger logger = LoggerConfiguration.logConfig(GetRemainingUpdateCountByIndividualId.class);

    @Autowired
    private AvailableClaimUtility availableClaimUtility;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    public ResponseWrapper<?> getRemainingUpdateCountByIndividualId(List<String> attributeList)
            throws ResidentServiceCheckedException {
        try {
            logger.debug("GetRemainingUpdateCountByIndividualId::getRemainingUpdateCountByIndividualId()::entry");
            String individualId= availableClaimUtility.getResidentIndvidualIdFromSession();
            Map<String, Object> pathsegements = new HashMap<String, Object>();
            pathsegements.put("individualId", individualId);

            List<String> queryParamName = new ArrayList<String>();
            queryParamName.add("attribute_list");

            List<Object> queryParamValue = new ArrayList<>();
            queryParamValue.add(Objects.isNull(attributeList) ? "" : attributeList.stream().collect(Collectors.joining(",")));

            ResponseWrapper<?> responseWrapper = residentServiceRestClient.getApi(ApiName.IDREPO_IDENTITY_UPDATE_COUNT,
                    pathsegements, queryParamName, queryParamValue, ResponseWrapper.class);
            if (responseWrapper.getErrors() != null && !responseWrapper.getErrors().isEmpty()){
                if(responseWrapper.getErrors().get(ResidentConstants.ZERO) != null && !responseWrapper.getErrors().get(ResidentConstants.ZERO).toString().isEmpty() &&
                        responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode() != null &&
                        !responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().isEmpty() &&
                        responseWrapper.getErrors().get(ResidentConstants.ZERO).getErrorCode().equalsIgnoreCase(ProxyIdRepoServiceImpl.NO_RECORDS_FOUND_ID_REPO_ERROR_CODE)) {
                    throw new ResidentServiceCheckedException(ResidentErrorCode.NO_RECORDS_FOUND);
                }else {
                    throw new ResidentServiceCheckedException(ResidentErrorCode.UNKNOWN_EXCEPTION);
                }
            }
            logger.debug("GetRemainingUpdateCountByIndividualId::getRemainingUpdateCountByIndividualId()::exit");
            return responseWrapper;

        } catch (ApisResourceAccessException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            throw new ResidentServiceCheckedException(API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        }
    }
}
