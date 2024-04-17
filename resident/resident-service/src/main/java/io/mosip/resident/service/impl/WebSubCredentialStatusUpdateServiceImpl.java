package io.mosip.resident.service.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

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
	public void updateCredentialStatus(Map<String, Object> eventModel)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("Inside WebSubCredentialStatusUpdateServiceImpl.updateCredentialStatus");
		logger.debug("event: " + eventModel);
		Map<String, String> credentialTransactionDetails = Optional.ofNullable(eventModel.get(ResidentConstants.EVENT))
				.filter(obj -> obj instanceof Map)
				.map(obj -> (Map<String, Object>) obj)
				.map(map -> map.entrySet()
						.stream()
						.filter(entry -> entry.getValue() != null)
						.collect(Collectors.toMap(Entry::getKey, entry -> String.valueOf(entry.getValue()))))
				.orElseGet(() -> Map.of());
		Object requestIdObj = credentialTransactionDetails.get(ResidentConstants.REQUEST_ID);
		if(requestIdObj instanceof String) {
			String requestId = (String) requestIdObj;
			logger.info(String.format("Updating the status of credential request ID: %s", requestId));
			Optional<ResidentTransactionEntity> entityOpt = repo.findOneByCredentialRequestId(requestId);
			if(entityOpt.isPresent()) {
				credentialStatusUpdateHelper.updateStatus(entityOpt.get(), credentialTransactionDetails);
			} else {
				logger.debug(String.format("Could not find the resident transaction with credential request ID: %s ; ignoring..",
						requestId));
			}
		}
		logger.debug("Exiting WebSubCredentialStatusUpdateServiceImpl.updateCredentialStatus");
	}

}
