package io.mosip.resident.dto;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to add extra variable eventId in update-uin api response for authenticated patch api.
 */
@Data
public class ResidentUpdateResponseDTOV2 extends ResidentUpdateResponseDTO{
    private String eventId;
}
