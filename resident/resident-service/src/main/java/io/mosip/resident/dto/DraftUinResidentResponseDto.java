package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;

/**
 * The DraftUinResponseDto.
 *
 * @author Kamesh Shekhar Prasad
 */
@Data
public class DraftUinResidentResponseDto {
    private String eid;
    private String aid;
    private String createdDTimes;
    private List<String> attributes;
    private boolean cancellable;
}
