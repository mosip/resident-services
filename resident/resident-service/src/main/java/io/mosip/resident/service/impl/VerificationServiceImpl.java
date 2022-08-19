package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.dto.VerificationResponseDTO;
import io.mosip.resident.dto.VerificationStatusDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.VerificationService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.Utilitiy;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

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
			throws ResidentServiceCheckedException, NoSuchAlgorithmException, ApisResourceAccessException {
		logger.debug("VerificationServiceImpl::checkChannelVerificationStatus::Start");
        
        ResidentTransactionEntity residentTransEntity = createResidentTransactionEntity(individualId);
        
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
        
        String id = getIdForResidentTransaction(uin, email, phone, residentTransEntity);
        residentTransEntity.setStatusCode(EventStatusFailure.FAILED.name());
        byte[] idBytes = id.getBytes();
        String hash = HMACUtils2.digestAsPlainText(idBytes);
        ResidentTransactionEntity residentTransactionEntity = residentTransactionRepository.findByAid(hash);
        if (residentTransactionEntity != null) {
            if(residentTransactionEntity.getStatusCode().equalsIgnoreCase("OTP_VERIFIED")){
                verificationStatus = true;
                residentTransEntity.setStatusCode(EventStatusInProgress.NEW.name());
            }
        }
        VerificationStatusDTO verificationStatusDTO = new VerificationStatusDTO();
        verificationStatusDTO.setVerificationStatus(verificationStatus);
        verificationResponseDTO.setResponse(verificationStatusDTO);
        verificationResponseDTO.setId("mosip.resident.channel.verification.status");
        verificationResponseDTO.setVersion("v1");
        verificationResponseDTO.setResponseTime(DateTime.now().toString());
        residentTransactionRepository.save(residentTransEntity);

        return verificationResponseDTO;
    }

	private ResidentTransactionEntity createResidentTransactionEntity(String individualId) throws ApisResourceAccessException {
		ResidentTransactionEntity residentTransEntity=utility.createEntity();
        residentTransEntity.setEventId(UUID.randomUUID().toString());
        residentTransEntity.setRequestTypeCode(RequestType.VERIFY_PHONE_EMAIL.name());
        residentTransEntity.setRefId(utility.convertToMaskDataFormat(individualId));
        residentTransEntity.setTokenId(identityServiceImpl.getResidentIdaToken());
		residentTransEntity.setRequestSummary("in-progress");
		return residentTransEntity;
	}

	private String getIdForResidentTransaction(String uin, String email, String phone, ResidentTransactionEntity residentTransEntity) throws ResidentServiceCheckedException {
		String idaToken= identityServiceImpl.getIDAToken(uin);
		String id;
		if(email != null) {
			id= email+idaToken;
		} else if(phone != null) {
			id= phone+idaToken;
		} else {
			residentTransEntity.setStatusCode(EventStatusFailure.FAILED.name());
			residentTransactionRepository.save(residentTransEntity);
			
			throw new ResidentServiceCheckedException(ResidentErrorCode.NO_CHANNEL_IN_IDENTITY);
		}
		return id;
	}
}

