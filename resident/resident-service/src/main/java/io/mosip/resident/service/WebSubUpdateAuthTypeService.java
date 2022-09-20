package io.mosip.resident.service;

import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

@Service
public interface WebSubUpdateAuthTypeService {
    public void updateAuthTypeStatus(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException;
}
