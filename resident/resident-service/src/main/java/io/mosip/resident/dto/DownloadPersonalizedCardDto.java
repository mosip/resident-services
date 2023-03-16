package io.mosip.resident.dto;

import java.util.List;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used a request DTO for download html 2 pdf API.
 */
@Data
public class DownloadPersonalizedCardDto {

    private String html;

    private List<String> attributes;

}