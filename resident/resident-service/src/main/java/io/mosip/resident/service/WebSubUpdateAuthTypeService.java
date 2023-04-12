package io.mosip.resident.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface WebSubUpdateAuthTypeService {
    public void updateAuthTypeStatus(Map<String, Object> eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException;
}
