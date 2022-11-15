package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilitiy;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.List;

@Component
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;
    
    @Autowired
	private Utilitiy utility;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

    @Override
	public VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId)
			throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::Start");
        VerificationResponseDTO verificationResponseDTO = new VerificationResponseDTO();
        boolean verificationStatus = false;
        ResidentTransactionEntity residentTransactionEntity =
                residentTransactionRepository.findTopByRefIdAndStatusCodeOrderByCrDtimesDesc
                        (utility.getIdForResidentTransaction(individualId, List.of(channel)), EventStatusInProgress.OTP_VERIFIED.toString());
        if (residentTransactionEntity!=null) {
            verificationStatus = true;
            residentTransactionRepository.save(residentTransactionEntity);
        }
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId("mosip.resident.channel.verification.status");
        verificationResponseDTO.setVersion("v1");
        verificationResponseDTO.setResponseTime(DateTime.now().toString());
        return verificationResponseDTO;
    }
}

