package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface WebSubUpdateAuthTypeService {
    public void updateAuthTypeStatus(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException;
}
