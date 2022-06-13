package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ServiceTypeRespenseDto {

    /** Variable To hold id */
    private String id;

    /** Variable To hold version */
    private String version;

    /** The error List */
    private List<AuthError> errors;

    /** List to hold AutnTxnDto */
    private Map<String, List<ServiceHistoryResponseDto>> response;
    /** The id. */

    /** The resTime value */
    private String responseTime;

}
