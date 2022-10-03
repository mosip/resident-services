package io.mosip.resident.service;

import java.util.List;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.UpdateCountDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ProxyIdRepoService {

	ResponseWrapper<List<UpdateCountDto>> getRemainingUpdateCountByIndividualId(String idType,
			List<String> attributeList) throws ResidentServiceCheckedException;

}
