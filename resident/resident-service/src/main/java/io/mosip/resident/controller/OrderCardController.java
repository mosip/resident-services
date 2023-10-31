package io.mosip.resident.controller;

import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_DESCRIPTION;
import static io.mosip.resident.constant.ResidentConstants.API_RESPONSE_TIME_ID;

import java.net.URI;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentCredentialRequestDto;
import io.mosip.resident.dto.ResidentCredentialResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.service.OrderCardService;
import io.mosip.resident.service.impl.IdentityServiceImpl;
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
	
	@Autowired
	private IdentityServiceImpl identityServiceImpl;
	
	@Autowired
	private Environment env;

	private static final Logger logger = LoggerConfiguration.logConfig(OrderCardController.class);

	/**
	 * Send a physical card.
	 * 
	 * @param requestWrapper
	 * @return responseWrapper<ResidentCredentialResponseDto> object
	 * @throws ResidentServiceCheckedException
	 * @throws ApisResourceAccessException 
	 */
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
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
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("OrderCardController::sendPhysicalCard()::entry");
		ResponseWrapper<ResidentCredentialResponseDto> responseWrapper = new ResponseWrapper<>();
		try {
			responseWrapper.setResponse(orderCardService.sendPhysicalCard(requestWrapper.getRequest()));
		} catch (ResidentServiceCheckedException e) {
			auditUtil.setAuditRequestDto(EventEnum.SEND_PHYSICAL_CARD_EXCEPTION);
			e.setMetadata(Map.of(ResidentConstants.REQ_RES_ID, env.getProperty(ResidentConstants.SEND_CARD_ID)));
		}
		auditUtil.setAuditRequestDto(EventEnum.SEND_PHYSICAL_CARD_SUCCESS);
		logger.debug("OrderCardController::sendPhysicalCard()::exit");
		return responseWrapper;
	}
	
	@ResponseFilter 
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetOrderRedirect()" + ")")
	@GetMapping("/physical-card/order")
	@Operation(summary = "get", description = "Get redirect-url", tags = { "order-card-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> physicalCardOrder(@RequestParam(name = "partnerId") String partnerId ,
			@RequestParam(name = "redirectUri") String redirectUri )
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("OrderCardController::getphysicalCardOrder()::entry");
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		String redirectURL = orderCardService.getRedirectUrl(partnerId,individualId);
		logger.debug("OrderCardController::getphysicalCardOrder()::exit");
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectURL)).build();
	}
	
	@ResponseFilter
	@Timed(value=API_RESPONSE_TIME_ID,description=API_RESPONSE_TIME_DESCRIPTION, percentiles = {0.5, 0.9, 0.95, 0.99} )
    @PreAuthorize("@scopeValidator.hasAllScopes(" + "@authorizedScopes.getGetOrderRedirect()" + ")")
	@GetMapping("/physical-card/order-redirect")
	@Operation(summary = "get", description = "Get redirect-url", tags = { "resident-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<Object> physicalCardOrderRedirect(@RequestParam(name = "redirectUrl",required = false) String redirectUrl,
			@RequestParam(name = "paymentTransactionId",required = false) String paymentTransactionId,
			@RequestParam(name = "eventId",required = false) String eventId,
			@RequestParam(name = "residentFullAddress",required = false) String residentFullAddress,
			@RequestParam(name = "error_code",required = false) String errorCode,
			@RequestParam(name = "error_message",required = false) String errorMessage)
			throws ResidentServiceCheckedException, ApisResourceAccessException {
		logger.debug("OrderCardController::physicalCardOrderRedirect()::entry");
		String individualId = identityServiceImpl.getResidentIndvidualIdFromSession();
		String response = orderCardService.physicalCardOrder(redirectUrl, paymentTransactionId, eventId,
				residentFullAddress,individualId,errorCode,errorMessage);
		logger.debug("OrderCardController::physicalCardOrderRedirect()::exit");
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(response)).build();
	}

}