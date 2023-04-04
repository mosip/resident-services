package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * @author Kamesh Shekhar Prasad
 */

@Data
public class ServiceTypeResponseDto {

    /** Variable To hold id */
    private String id;

    /** Variable To hold version */
    private String version;

    /** The error List */
    private List<AuthError> errors;

    /** List to hold ServiceType */
    private Map<String, List<ServiceHistoryResponseDto>> response;
    /** The id. */

    /** The resTime value */
    private String responseTime;

}
