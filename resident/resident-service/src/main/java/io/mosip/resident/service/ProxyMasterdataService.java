package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface ProxyMasterdataService {

	public ResponseWrapper<?> getValidDocumentByLangCode(String langCode) throws ResidentServiceCheckedException;

}
