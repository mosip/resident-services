package io.mosip.resident.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * This class is used to map TPM signkey response from keymanager.
 *
 * @author Abubacker Siddik
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PacketSignPublicKeyResponseDTO extends BaseRestResponseDTO {

    private Map<String, Object> metadata;

    /**
     * The response.
     */
    private PacketSignPublicKeyResponse response;

    /**
     * The error.
     */
    private List<PacketSignPublicKeyErrorDTO> errors;

    @Data
    public static class PacketSignPublicKeyResponse {
        /**
         * Variable to hold public key
         */
        private String publicKey;
    }

}