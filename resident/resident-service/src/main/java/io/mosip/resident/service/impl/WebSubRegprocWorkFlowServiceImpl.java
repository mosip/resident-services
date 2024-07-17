package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatusFailure;
import io.mosip.resident.constant.EventStatusInProgress;
import io.mosip.resident.constant.PacketStatus;
import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.TemplateType;
import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.service.WebSubRegprocWorkFlowService;
import io.mosip.resident.util.IdentityDataUtil;
import io.mosip.resident.util.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class WebSubRegprocWorkFlowServiceImpl implements WebSubRegprocWorkFlowService {

    private static final Logger logger = LoggerConfiguration.logConfig(WebSubRegprocWorkFlowServiceImpl.class);

    @Autowired
    Environment environment;

    @Autowired
    ResidentTransactionRepository residentTransactionRepository;

    @Autowired
    Utility utility;

    @Autowired
    private IdentityDataUtil identityDataUtil;

    @Override
    public void updateResidentStatus(WorkflowCompletedEventDTO workflowCompletedEventDTO) throws ResidentServiceCheckedException {
        logger.debug("WebSubRegprocWorkFlowServiceImpl:updateResidentStatus entry");
        ResidentTransactionEntity residentTransactionEntity = null;
        String individualId = null;
        if (workflowCompletedEventDTO.getResultCode() != null) {
            if (workflowCompletedEventDTO.getInstanceId() != null) {
                residentTransactionEntity =
                        residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(workflowCompletedEventDTO.getInstanceId());
            }
            if (residentTransactionEntity != null) {
                individualId = residentTransactionEntity.getIndividualId();
                if (PacketStatus.getStatusCodeList(PacketStatus.FAILURE, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    utility.updateEntity(EventStatusFailure.FAILED.name(), RequestType.UPDATE_MY_UIN.name() + " - " + ResidentConstants.FAILED,
                            false, "Packet Failed in Regproc with status code-" +
                            workflowCompletedEventDTO.getResultCode(), residentTransactionEntity);
                    identityDataUtil.sendNotification(residentTransactionEntity.getEventId(), individualId, TemplateType.REGPROC_FAILED);
                } else if (PacketStatus.getStatusCodeList(PacketStatus.SUCCESS, environment).contains(workflowCompletedEventDTO.getResultCode())) {
                    utility.updateEntity(EventStatusInProgress.IDENTITY_UPDATED.name(), EventStatusInProgress.IDENTITY_UPDATED.name(), false,
                            "Packet processed in Regproc with status code-" +
                            workflowCompletedEventDTO.getResultCode(), residentTransactionEntity);
                    identityDataUtil.sendNotification(residentTransactionEntity.getEventId(), individualId, TemplateType.REGPROC_SUCCESS);
                }
            }
        }
        logger.debug("WebSubRegprocWorkFlowServiceImpl:updateResidentStatus exit");
    }

}
