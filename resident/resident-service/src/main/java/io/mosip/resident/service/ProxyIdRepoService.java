package io.mosip.resident.service;

import io.mosip.resident.exception.ResidentServiceCheckedException;

public interface ProxyIdRepoService {

    String discardDraft(String eid) throws ResidentServiceCheckedException;
}
