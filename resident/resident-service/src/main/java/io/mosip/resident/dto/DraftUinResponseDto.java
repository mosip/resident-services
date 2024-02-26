package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;

/**
 * The DraftUinResponseDto.
 *
 * @author Kamesh Shekhar Prasad
 */
@Data
public class DraftUinResponseDto {
    private String rid;
    private String createdDTimes;
    private List<String> attributes;
}
