package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.service.WebSubUpdateAuthTypeService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name="WebSubUpdateAuthTypeController", description="WebSubUpdateAuthTypeController")
public class WebSubUpdateAuthTypeController {

    private static Logger logger = LoggerConfiguration.logConfig(WebSubUpdateAuthTypeController.class);

    @Autowired
    SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscribe;

    @Autowired
    private WebSubUpdateAuthTypeService webSubUpdateAuthTypeService;

    @Autowired
    private AuditUtil auditUtil;

    @Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PostMapping(value = "/callback/authTypeCallback", consumes = "application/json")
    @Operation(summary = "WebSubUpdateAuthTypeController", description = "WebSubUpdateAuthTypeController",
            tags = {"WebSubUpdateAuthTypeController"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true)))})
    @PreAuthenticateContentAndVerifyIntent(secret = "${resident.websub.authtype-status.secret}", callback = "${resident.websub.callback.authtype-status.relative.url}", topic = "${resident.websub.authtype-status.topic}")
	public void authTypeCallback(@RequestBody Map<String, Object> eventModel) {
		try {
			logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					"WebSubUpdateAuthTypeController :: authTypeCallback() :: entry");
			webSubUpdateAuthTypeService.updateAuthTypeStatus(eventModel);
			auditUtil.setAuditRequestDto(EventEnum.AUTH_TYPE_CALL_BACK_SUCCESS);
			logger.debug("WebSubUpdateAuthTypeController::authTypeCallback()::exit");
		} catch (ResidentServiceCheckedException | ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(),
					ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorCode()
							+ ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorMessage()
							+ ExceptionUtils.getStackTrace(e));
			auditUtil.setAuditRequestDto(EventEnum.AUTH_TYPE_CALL_BACK_FAILURE);
			throw new ResidentServiceException(ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorCode(),
					ResidentErrorCode.AUTH_TYPE_CALLBACK_NOT_AVAILABLE.getErrorMessage(), e);
		}
	}
}
