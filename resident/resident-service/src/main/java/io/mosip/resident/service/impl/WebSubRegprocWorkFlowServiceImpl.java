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

import java.util.List;

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

        String resultCode = workflowCompletedEventDTO.getResultCode();
        String instanceId = workflowCompletedEventDTO.getInstanceId();
        String maskedAid = maskTrailing(instanceId);
        logger.info("regproc workflow callback received: resultCode=" + resultCode + " aid=" + maskedAid);

        // Diagnostic gates for MOSIP-40519: every silent fall-through here used to
        // leave the row stuck at IN_PROGRESS / NEW with no audit trail.
        if (resultCode == null) {
            logger.warn("regproc workflow callback dropped: resultCode is null (aid=" + maskedAid + ")");
            return;
        }
        if (instanceId == null) {
            logger.warn("regproc workflow callback dropped: instanceId is null (resultCode=" + resultCode + ")");
            return;
        }

        ResidentTransactionEntity residentTransactionEntity =
                residentTransactionRepository.findTopByAidOrderByCrDtimesDesc(instanceId);
        if (residentTransactionEntity == null) {
            logger.warn("regproc workflow callback dropped: no ResidentTransactionEntity found for incoming aid="
                    + maskedAid + " (resultCode=" + resultCode + ")");
            return;
        }

        String eventId = residentTransactionEntity.getEventId();
        String individualId = residentTransactionEntity.getIndividualId();

        List<String> failureList = PacketStatus.getStatusCodeList(PacketStatus.FAILURE, environment);
        List<String> successList = PacketStatus.getStatusCodeList(PacketStatus.SUCCESS, environment);

        if (failureList.contains(resultCode)) {
            utility.updateEntity(EventStatusFailure.FAILED.name(),
                    RequestType.UPDATE_MY_UIN.name() + " - " + ResidentConstants.FAILED,
                    false,
                    "Packet Failed in Regproc with status code-" + resultCode,
                    residentTransactionEntity);
            identityDataUtil.sendNotification(eventId, individualId, TemplateType.REGPROC_FAILED);
            logger.info("regproc workflow callback applied: status=FAILED eventId=" + eventId
                    + " resultCode=" + resultCode);
        } else if (successList.contains(resultCode)) {
            utility.updateEntity(EventStatusInProgress.IDENTITY_UPDATED.name(),
                    EventStatusInProgress.IDENTITY_UPDATED.name(),
                    false,
                    "Packet processed in Regproc with status code-" + resultCode,
                    residentTransactionEntity);
            identityDataUtil.sendNotification(eventId, individualId, TemplateType.REGPROC_SUCCESS);
            logger.info("regproc workflow callback applied: status=IDENTITY_UPDATED eventId=" + eventId
                    + " resultCode=" + resultCode);
        } else {
            // The unmatched-resultCode silent drop. Surface enough context to tell apart
            // (a) regproc emitting a code outside the configured lists vs (b) the lists
            // themselves being empty/misconfigured on the deployed environment.
            logger.warn("regproc workflow callback dropped: resultCode=" + resultCode
                    + " not in configured SUCCESS list (size=" + successList.size()
                    + ") or FAILURE list (size=" + failureList.size()
                    + "); eventId=" + eventId
                    + ". Check resident.success.packet-status-code.list and resident.failure.packet-status-code.list properties.");
        }

        logger.debug("WebSubRegprocWorkFlowServiceImpl:updateResidentStatus exit");
    }

    /**
     * Mask all but the last 4 chars of an identifier so it can appear in logs
     * without leaking the full value. Used for AIDs in callback diagnostics.
     */
    private static String maskTrailing(String value) {
        if (value == null) {
            return "null";
        }
        int len = value.length();
        if (len <= 4) {
            return "****";
        }
        return "****" + value.substring(len - 4);
    }

}
