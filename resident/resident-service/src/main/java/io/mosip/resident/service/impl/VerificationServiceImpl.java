package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;

@Component
public class VerificationServiceImpl implements VerificationService {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private IdentityServiceImpl identityServiceImpl;

    @Autowired
    private ResidentTransactionRepository residentTransactionRepository;

    private static final Logger logger = LoggerConfiguration.logConfig(ProxyMasterdataServiceImpl.class);

    @Override
    public VerificationResponseDTO checkChannelVerificationStatus(String channel, String individualId) throws ResidentServiceCheckedException, NoSuchAlgorithmException {
        logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::Start");
        VerificationResponseDTO verificationResponseDTO = new VerificationResponseDTO();
        boolean verificationStatus = false;
        IdentityDTO identityDTO = identityServiceImpl.getIdentity(individualId);

        String uin ="";
        String email ="";
        String phone ="";

        if (identityDTO != null) {
            uin = identityDTO.getUIN();
            email = identityDTO.getEmail();
            phone = identityDTO.getPhone();
        }
        
        String id = getIdForResidentTransaction(uin, email, phone);
		
        byte[] idBytes = id.getBytes();
        String hash = HMACUtils2.digestAsPlainText(idBytes);
        ResidentTransactionEntity residentTransactionEntity = residentTransactionRepository.findByAid(hash);
        if (residentTransactionEntity != null) {
            if(residentTransactionEntity.getStatusCode().equalsIgnoreCase("OTP_VERIFIED")){
                verificationStatus = true;
            }
        }
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId("mosip.resident.channel.verification.status");
        verificationResponseDTO.setVersion("v1");
        verificationResponseDTO.setResponseTime(DateTime.now().toString());

        return verificationResponseDTO;
    }

	private String getIdForResidentTransaction(String uin, String email, String phone) throws ResidentServiceCheckedException {
		String idaToken= identityServiceImpl.getIDAToken(uin);
		String id;
		if(email != null) {
			id= email+idaToken;
		} else if(phone != null) {
			id= phone+idaToken;
		} else {
			throw new ResidentServiceCheckedException(ResidentErrorCode.NO_CHANNEL_IN_IDENTITY);
		}
		return id;
	}
}

