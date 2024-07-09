package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.ProxyMasterdataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */
@Component
public class ProxyMasterDataServiceUtility {

    @Autowired
    private ProxyMasterdataService proxyMasterdataService;

    @Cacheable(value = "getDynamicFieldBasedOnLangCodeAndFieldName", key = "{#fieldName, #langCode, #withValue}")
    public ResponseWrapper<?> getDynamicFieldBasedOnLangCodeAndFieldName(String fieldName, String langCode, boolean withValue) throws ResidentServiceCheckedException {
        return proxyMasterdataService.getDynamicFieldBasedOnLangCodeAndFieldName(fieldName, langCode, withValue);
    }
}
