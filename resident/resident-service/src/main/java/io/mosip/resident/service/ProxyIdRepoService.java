package io.mosip.resident.service;

import java.util.List;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.dto.DraftResidentResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ProxyIdRepoService {

	ResponseWrapper<?> getRemainingUpdateCountByIndividualId(List<String> attributeList)
			throws ResidentServiceCheckedException;

    ResponseWrapper<DraftResidentResponseDto> getPendingDrafts() throws ResidentServiceCheckedException;

    String discardDraft(String eid) throws ResidentServiceCheckedException;
}
