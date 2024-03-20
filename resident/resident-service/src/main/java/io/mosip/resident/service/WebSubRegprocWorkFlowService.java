package io.mosip.resident.service;

import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service to update the resident transaction status from the credential status
 * update in the websub event.
 * 
 * @author Kamesh Shekhar Prasad
 *
 */
@Service
public interface WebSubRegprocWorkFlowService {
    public void updateResidentStatus(WorkflowCompletedEventDTO workflowCompletedEventDTO) throws ResidentServiceCheckedException, ApisResourceAccessException;
}
