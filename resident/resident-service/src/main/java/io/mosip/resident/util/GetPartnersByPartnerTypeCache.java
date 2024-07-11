package io.mosip.resident.util;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GetPartnersByPartnerTypeCache {

    @Autowired
    GetPartnersByPartnerType getPartnersByPartnerType;

    @Cacheable(value = "partnerListCache", key = "#partnerType + '_' + #apiUrl")
    public ResponseWrapper<?> getPartnersByPartnerType(String partnerType, ApiName apiUrl) throws ResidentServiceCheckedException {
        return getPartnersByPartnerType.getPartnersByPartnerType(StringUtils.isBlank(partnerType) ? Optional.empty() : Optional.of(partnerType), apiUrl);
    }
}
