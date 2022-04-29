package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

    @Override
    public ResponseWrapper<?> checkChannelVerificationStatus(String channel, String individualId) throws ResidentServiceCheckedException {
        logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::Start");
        ResponseWrapper<?> responseWrapper = new ResponseWrapper<>();
        String verificationStatus = "";
        IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);

        if (identityDTO != null) {

        }
        return null;
    }
}
