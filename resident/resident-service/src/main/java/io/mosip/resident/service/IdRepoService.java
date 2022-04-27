package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface IdRepoService {
    public Map getIdentity(String individualId);
}
