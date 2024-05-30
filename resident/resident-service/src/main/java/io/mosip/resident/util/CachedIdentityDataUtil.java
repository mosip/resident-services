package io.mosip.resident.util;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ApisResourceAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CachedIdentityDataUtil {

	private static final String RETRIEVE_IDENTITY_PARAM_TYPE_DEMO = "demo";

	@Autowired
	@Qualifier("restClientWithSelfTOkenRestTemplate")
	private ResidentServiceRestClient restClientWithSelfTOkenRestTemplate;

    public  <T> T getIdentityData(String id, Class<?> responseType) throws ApisResourceAccessException {
		Map<String, String> pathSegments = new HashMap<String, String>();
		pathSegments.put("id", id);

		List<String> queryParamName = new ArrayList<String>();
		queryParamName.add("type");

		List<Object> queryParamValue = new ArrayList<>();
		queryParamValue.add(RETRIEVE_IDENTITY_PARAM_TYPE_DEMO);
		return restClientWithSelfTOkenRestTemplate.getApi(ApiName.IDREPO_IDENTITY_URL,
				pathSegments, queryParamName, queryParamValue, responseType);
	}

    @Cacheable(value = "identityMapCache", key = "#accessToken")
    public  <T> T getCachedIdentityData(String id, String accessToken, Class<?> responseType) throws ApisResourceAccessException {
        return getIdentityData(id, responseType);
    }
}
