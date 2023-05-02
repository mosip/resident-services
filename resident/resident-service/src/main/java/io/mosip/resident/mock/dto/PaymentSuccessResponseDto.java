package io.mosip.resident.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to return response of payment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessResponseDto {

    private String transactionId;

    private String trackingId;
}
