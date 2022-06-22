package io.mosip.resident.service;

import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;

@Service
public interface AuthTransactionCallBackService {
    public void updateAuthTransactionCallBackService(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException;
}
