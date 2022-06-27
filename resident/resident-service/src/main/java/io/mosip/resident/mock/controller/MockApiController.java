package io.mosip.resident.mock.controller;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.mock.service.MockService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

/**
 * Mock API Controller class.
 *
 * @author Kamesh Shekhar Prasad
 */
@RequestMapping("/mock")
@RestController
@Tag(name = "mock-api-controller", description = "Mock API Controller")
public class MockApiController {

    @Autowired
    private AuditUtil auditUtil;

    @Autowired
    private MockService mockService;

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
			@ApiResponse(responseCode = "402", description = "Payment Required", content = @Content(schema = @Schema(hidden = true))),
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
    @GetMapping(path= "/rid-digital-card/{rid}")
    public ResponseEntity<Object> getRIDDigitalCard(
            @PathVariable("rid") String rid) throws Exception {
        auditUtil.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ);
        byte[] pdfBytes = mockService.getRIDDigitalCardV2(rid);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        auditUtil.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_SUCCESS);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        rid + ".pdf\"")
                .body((Object) resource);
    }
}
