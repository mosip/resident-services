package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class GetValidDocumentByLangCodeCache {

    @Autowired
    GetValidDocumentByLangCode getValidDocumentByLangCode;

    @Cacheable(value = "getValidDocumentByLangCode", key = "#langCode")
    public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException {
        return getValidDocumentByLangCode.getValidDocumentByLangCode(langCode);
    }
}
