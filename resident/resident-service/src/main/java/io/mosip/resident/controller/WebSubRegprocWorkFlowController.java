package io.mosip.resident.controller;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.WorkflowCompletedEventDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.WebSubRegprocWorkFlowService;
import io.mosip.resident.util.AuditEnum;
import io.mosip.resident.util.AuditUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

/**
 * API to update the resident transaction status from the regproc workflow event status
 * update in the websub event.
 * 
 * @author Kamesh Shekhar Prasad
 *
 */
@RestController
@Tag(name="WebSubRegprocWorkFlowController", description="WebSubRegprocWorkFlowController")
public class WebSubRegprocWorkFlowController {

    private static Logger logger = LoggerConfiguration.logConfig(WebSubRegprocWorkFlowController.class);

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Autowired
    private WebSubRegprocWorkFlowService webSubRegprocWorkFlowService;

    @Autowired
    private AuditUtil auditUtil;

    @PostMapping(value = "/callback/regprocworkflow", consumes = "application/json")
    @Operation(summary = "WebSubCredentialStatusUpdateController", description = "WebSubCredentialStatusUpdateController",
            tags = {"WebSubCredentialStatusUpdateController"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})

    @Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthenticateContentAndVerifyIntent(secret = "${resident.websub.regproc.workflow.complete.secret}", callback = "${resident.websub.callback.regproc.workflow.complete.relative.url}", topic = "${mosip.regproc.workflow.complete.topic}")
	public void regProcWorkFlowCallback(@RequestBody WorkflowCompletedEventDTO workflowCompletedEventDTO) {
		try {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubRegprocWorkFlowController :: regProcWorkFlowCallback() :: entry");
			webSubRegprocWorkFlowService.updateResidentStatus(workflowCompletedEventDTO);
			auditUtil.setAuditRequestDto(AuditEnum.REG_PROC_WORK_FLOW_CALL_BACK_SUCCESS);
			logger.debug("WebSubRegprocWorkFlowController::regProcWorkFlowCallback()::exit");
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.REG_PROC_WORK_FLOW_CALLBACK_NOT_AVAILABLE.getErrorCode()
							+ ResidentErrorCode.REG_PROC_WORK_FLOW_CALLBACK_NOT_AVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			auditUtil.setAuditRequestDto(AuditEnum.REG_PROC_WORK_FLOW_CALL_BACK_FAILURE);
			throw new ResidentServiceException(ResidentErrorCode.REG_PROC_WORK_FLOW_CALLBACK_NOT_AVAILABLE.getErrorCode(),
					ResidentErrorCode.REG_PROC_WORK_FLOW_CALLBACK_NOT_AVAILABLE.getErrorMessage(), e);
		}
	}

}
