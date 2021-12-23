package io.mosip.resident.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * This class is used to get TPM signkey from keymanager.
 *
 * @author Abubacker Siddik
 */

@Data
@Builder
public class SignKeyRequestDTO implements Serializable {

    /**
     * Variable to hold id
     */
    private String id;

    /**
     * Variable to hold version
     */
    private String version;

    /**
     * Variable to hold Request time
     */
    private String requestTime;

    private Map<String, Object> metadata;

    /**
     * Variable to hold request
     */
    private SignRequest request;

    @Data
    @Builder
    public static class SignRequest {
        private String serverProfile;
    }
}