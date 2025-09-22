package io.mosip.resident.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.helper.CredentialStatusUpdateHelper;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.WebSubCredentialStatusUpdateService;

/**
 * Service Implementation to update the resident transaction status from the credential status
 * update in the websub event.
 * 
 * @author Loganathan S
 *
 */
@Service
public class WebSubCredentialStatusUpdateServiceImpl implements WebSubCredentialStatusUpdateService {

	private static final Logger logger = LoggerConfiguration.logConfig(WebSubCredentialStatusUpdateServiceImpl.class);
    
    @Autowired
    private CredentialStatusUpdateHelper credentialStatusUpdateHelper;

    @Autowired
	private ResidentTransactionRepository repo;

    @Override
    public void updateCredentialStatus(Map<String, Object> eventModel) throws ResidentServiceCheckedException, ApisResourceAccessException {
        // Log debug only if necessary
        logger.debug("Inside WebSubCredentialStatusUpdateServiceImpl.updateCredentialStatus, event: {}", eventModel);

        // Extract event map directly
        Map<String, Object> event = (Map<String, Object>) eventModel.get(ResidentConstants.EVENT);
        if (event == null) {
            logger.warn("Event map is null, skipping update");
            return;
        }

        // Extract requestId and validate
        Object requestIdObj = event.get(ResidentConstants.REQUEST_ID);
        if (!(requestIdObj instanceof String requestId)) {
            logger.warn("Invalid or missing requestId, skipping update");
            return;
        }

        // Convert Map<String, Object> to Map<String, String>
        Map<String, String> credentialTransactionDetails = new HashMap<>();
        for (Map.Entry<String, Object> entry : event.entrySet()) {
            if (entry.getValue() != null) {
                credentialTransactionDetails.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        // Database query
        Optional<ResidentTransactionEntity> entityOpt = repo.findOneByCredentialRequestId(requestId);
        if (entityOpt.isPresent()) {
            logger.info("Updating status for credential request ID: {}", requestId);
            credentialStatusUpdateHelper.updateStatus(entityOpt.get(), credentialTransactionDetails);
        } else {
            logger.debug("No resident transaction found for credential request ID: {}, ignoring", requestId);
        }

        logger.debug("Exiting WebSubCredentialStatusUpdateServiceImpl.updateCredentialStatus");
    }

}
