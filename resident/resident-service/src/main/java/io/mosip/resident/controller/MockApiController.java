package io.mosip.resident.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Mock API Controller class.
 * 
 * @author Ritik Jain
 */
@RequestMapping("/mock")
@RestController
@Tag(name = "mock-api-controller", description = "Mock API Controller")
public class MockApiController {

	private static final Logger logger = LoggerConfiguration.logConfig(MockApiController.class);

	/**
	 * Get order status.
	 * 
	 * @param transactionId
	 * @param individualId
	 */
	@ResponseFilter
	@GetMapping(value = "/print-partner/check-order-status")
	@Operation(summary = "getOrderStatus", description = "getOrderStatus", tags = { "mock-api-controller" })
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "402", description = "Payment is not received yet", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseEntity<?> getOrderStatus(@RequestParam("transactionId") String transactionId,
			@RequestParam("individualId") String individualId) {
		logger.debug("MockApiController::getOrderStatus()::entry");
		if (Character.getNumericValue(transactionId.charAt(transactionId.length() - 1)) >= 6
				&& Character.getNumericValue(transactionId.charAt(transactionId.length() - 1)) <= 9) {
			logger.debug("payment is required for this id");
			return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
		}
		logger.debug("MockApiController::getOrderStatus()::exit");
		return ResponseEntity.ok().build();
	}

}
