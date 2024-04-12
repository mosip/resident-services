package io.mosip.resident.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This class is used to get TPM signkey from keymanager.
 *
 * @author Abubacker Siddik
 */

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class PacketSignPublicKeyRequestDTO extends BaseRestRequestDTO {

    private Map<String, Object> metadata;

    /**
     * Variable to hold request
     */
    private PacketSignPublicKeyRequest request;

    @Data
    @Builder
    public static class PacketSignPublicKeyRequest {
        private String serverProfile;
    }
}