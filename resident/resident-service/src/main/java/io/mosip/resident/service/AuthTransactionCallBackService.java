package io.mosip.resident.service;

import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

@Service
public interface AuthTransactionCallBackService {
    public void updateAuthTransactionCallBackService(EventModel eventModel, String partnerId) throws ResidentServiceCheckedException;
}
