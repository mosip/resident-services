package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.service.IdRepoService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class IdRepoServiceImpl implements IdRepoService {

    private static final Logger logger = LoggerConfiguration.logConfig(IdRepoServiceImpl.class);

    @Override
    public Map getIdentity(String individualId){
        logger.info("getIdentity method of IdRepoServiceImpl class");
        logger.info("individualId : "+individualId);
        Map<String, String> identity = new HashMap<>();
        identity.put("UIN", individualId);
        identity.put("email", "k@g.com ");
        identity.put("phone", "1234567890");
        return identity;
    }
}
