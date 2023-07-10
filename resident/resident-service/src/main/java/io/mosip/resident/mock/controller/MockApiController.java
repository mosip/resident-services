package io.mosip.resident.mock.controller;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.mock.dto.PaymentSuccessResponseDto;
import io.mosip.resident.mock.exception.CantPlaceOrderException;
import io.mosip.resident.mock.exception.PaymentCanceledException;
import io.mosip.resident.mock.exception.PaymentFailedException;
import io.mosip.resident.mock.exception.TechnicalErrorException;
import io.mosip.resident.mock.service.MockService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
											@RequestParam("individualId") String individualId) throws ResidentServiceCheckedException {
		int lastDigit = Character.getNumericValue(transactionId.charAt(transactionId.length() - 1));
		ResponseWrapper<PaymentSuccessResponseDto> responseWrapper = new ResponseWrapper<>();
		if (lastDigit >= 0 && lastDigit < 6) {
			PaymentSuccessResponseDto paymentSuccessResponseDto = new PaymentSuccessResponseDto();
			paymentSuccessResponseDto.setTrackingId(UUID.randomUUID().toString());
			paymentSuccessResponseDto.setTransactionId(transactionId);
			responseWrapper.setResponse(paymentSuccessResponseDto);
			return ResponseEntity.ok().body(responseWrapper);
		} else if (lastDigit == 6) {
			throw new PaymentFailedException();
		} else if(lastDigit ==7){
			throw new PaymentCanceledException();
		} else if (lastDigit ==8) {
			throw new TechnicalErrorException();
		}else {
			throw new CantPlaceOrderException();
		}
	}
    @GetMapping(path= "/rid-digital-card/{rid}")
    public ResponseEntity<Object> getRIDDigitalCard(
            @PathVariable("rid") String rid) throws Exception {
        byte[] pdfBytes = mockService.getRIDDigitalCardV2(rid);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));
        auditUtil.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_SUCCESS);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header("Content-Disposition", "attachment; filename=\"" +
                        rid + ".pdf\"")
                .body((Object) resource);
    }
}
