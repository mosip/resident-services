package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used a request DTO for download html 2 pdf API.
 */
@Data
public class DownloadPersonalizedCard {

    private String html;

}