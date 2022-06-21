package io.mosip.resident.controller;

import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Order Card Controller class.
 * 
 * @author Ritik Jain
 */
@RestController
@Tag(name = "order-card-controller", description = "Order Card Controller")
public class OrderCardController {

	@Autowired
	private OrderCardService orderCardService;

	@Autowired
	private AuditUtil auditUtil;

	private static final Logger logger = LoggerConfiguration.logConfig(OrderCardController.class);

	/**
	 * Send a physical card.
	 * 
	 * @param requestWrapper
	 * @return responseWrapper<ResidentCredentialResponseDto> object
	 * @throws ResidentServiceCheckedException
	 */
	@ResponseFilter
	@PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getPostSendPhysicalCard()" + ")")
	@PostMapping(value = "/sendCard")
	@Operation(summary = "sendPhysicalCard", description = "sendPhysicalCard", tags = { "order-card-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<ResidentCredentialResponseDto> sendPhysicalCard(
			@RequestBody RequestWrapper<ResidentCredentialRequestDto> requestWrapper)
			throws ResidentServiceCheckedException {
		logger.debug("OrderCardController::sendPhysicalCard()::entry");
		auditUtil.setAuditRequestDto(EventEnum.SEND_PHYSICAL_CARD);
		ResponseWrapper<ResidentCredentialResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(orderCardService.sendPhysicalCard(requestWrapper.getRequest()));
		auditUtil.setAuditRequestDto(EventEnum.SEND_PHYSICAL_CARD_SUCCESS);
		logger.debug("OrderCardController::sendPhysicalCard()::exit");
		return responseWrapper;
	}

}
