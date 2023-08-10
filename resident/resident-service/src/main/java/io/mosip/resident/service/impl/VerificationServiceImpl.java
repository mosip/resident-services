package io.mosip.resident.service.impl;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.mosip.resident.dto.IdentityDTO;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusSuccess;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.Utility;

@Component
public class VerificationServiceImpl implements VerificationService {

    @Autowired
	private Utility utility;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;
    
    @Value("${resident.channel.verification.status.id}")
    private String residentChannelVerificationStatusId;
    
    @Value("${resident.channel.verification.status.version}")
    private String residentChannelVerificationStatusVersion;

    private static final Logger logger = LoggerConfiguration.logConfig(VerificationServiceImpl.class);

    @Override
	public VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId)
			throws ResidentServiceCheckedException, NoSuchAlgorithmException {
		logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::entry");
        VerificationResponseDTO verificationResponseDTO = new VerificationResponseDTO();
        boolean verificationStatus = false;
        IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);
        String idaToken = identityServiceImpl.getIDAToken(identityDTO.getUIN());
        boolean entityExist =
                residentTransactionRepository.existsByRefIdAndStatusCode
                        (utility.getIdForResidentTransaction(List.of(channel), identityDTO, idaToken), EventStatusSuccess.OTP_VERIFIED.toString());
        if (entityExist) {
            verificationStatus = true;
        }
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId(residentChannelVerificationStatusId);
        verificationResponseDTO.setVersion(residentChannelVerificationStatusVersion);
        verificationResponseDTO.setResponseTime(DateTime.now().toString());
        logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::exit");
        return verificationResponseDTO;
    }
}

