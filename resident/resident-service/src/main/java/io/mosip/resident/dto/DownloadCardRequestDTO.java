package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used a request DTO for download card API.
 */
@Data
public class DownloadCardRequestDTO {

    private String transactionId;

    private String individualId;

    private String otp;

}