package io.mosip.resident.service;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ProxyIdRepoService {

	ResponseWrapper<Map<String, Integer>> getRemainingUpdateCountByIndividualId(String individualId, String idType,
			List<String> attributeList) throws ResidentServiceCheckedException;

}
