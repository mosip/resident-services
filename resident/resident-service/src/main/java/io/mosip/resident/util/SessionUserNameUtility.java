package io.mosip.resident.util;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.service.impl.IdentityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SessionUserNameUtility {

    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
    private Environment environment;

    public String getSessionUserName() {
        String name = null;
        try {
            name = identityService.getAvailableclaimValue(this.environment.getProperty(ResidentConstants.NAME_FROM_PROFILE));
            if (name == null || name.trim().isEmpty()) {
                name = ResidentConstants.UNKNOWN;
            }
        } catch (ApisResourceAccessException e) {
            throw new RuntimeException(e);
        }
        return name;
    }
}
