package io.mosip.resident.service;

import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import io.mosip.kernel.core.websub.model.EventModel;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface AuthTransactionCallBackService {
    public void updateAuthTransactionCallBackService(EventModel eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException;
}
