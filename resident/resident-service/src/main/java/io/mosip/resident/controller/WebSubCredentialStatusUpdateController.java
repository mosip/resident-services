package io.mosip.resident.controller;

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.WebSubCredentialStatusUpdateService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * API to update the resident transaction status from the credential status
 * update in the websub event.
 * 
 * @author Loganathan S
 *
 */
@RestController
@Tag(name="WebSubCredentialStatusUpdateController", description="WebSubCredentialStatusUpdateController")
public class WebSubCredentialStatusUpdateController {

    private static Logger logger = LoggerConfiguration.logConfig(WebSubCredentialStatusUpdateController.class);

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Autowired
    private WebSubCredentialStatusUpdateService webSubCredentialStatusUpdateService;

    @Autowired
    private AuditUtil auditUtil;

    @PostMapping(value = "/callback/credentialStatusUpdate", consumes = "application/json")
    @Operation(summary = "WebSubCredentialStatusUpdateController", description = "WebSubCredentialStatusUpdateController",
            tags = {"WebSubCredentialStatusUpdateController"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})

    @PreAuthenticateContentAndVerifyIntent(secret = "${resident.websub.credential-status.secret}", callback = "${resident.websub.callback.credential-status.relative.url}", topic = "${resident.websub.credential-status.topic}")
	public void credentialStatusUpdateCallback(@RequestBody Map<String, Object> eventModel) {

		logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				LoggerFileConstant.APPLICATIONID.toString(),
				"WebSubCredentialStatusUpdateController :: credentialStatusUpdateCallback() :: Start");

		try {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubCredentialStatusUpdateController :: credentialStatusUpdateCallback() :: Start");
			auditUtil.setAuditRequestDto(EventEnum.CREDENTIAL_STATUS_UPDATE_CALL_BACK);
			webSubCredentialStatusUpdateService.updateCredentialStatus(eventModel);
			auditUtil.setAuditRequestDto(EventEnum.CREDENTIAL_STATUS_UPDATE_CALL_BACK_SUCCESS);
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorCode()
							+ ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			auditUtil.setAuditRequestDto(EventEnum.CREDENTIAL_STATUS_UPDATE_CALL_BACK_FAILURE);
			throw new ResidentServiceException(ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorCode(),
					ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorMessage(), e);
		}
	}
}
