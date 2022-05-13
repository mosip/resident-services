package io.mosip.resident.service;

import org.springframework.stereotype.Service;

@Service
public interface WebSubUpdateAuthTypeService {
    public void updateAuthTypeStatus(String tokenId, String authTypeStatusList) ;
}
