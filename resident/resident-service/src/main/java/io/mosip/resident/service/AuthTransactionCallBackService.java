package io.mosip.resident.service;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface AuthTransactionCallBackService {
    public void updateAuthTransactionCallBackService(Map<String, Object> eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException, NoSuchAlgorithmException;
}
