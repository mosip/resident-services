package io.mosip.resident.service;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ProxyIdRepoService {

	List<UpdateCountDto> getRemainingUpdateCountByIndividualId(String idType,
			List<String> attributeList) throws ResidentServiceCheckedException;

}
