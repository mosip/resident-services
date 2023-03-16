package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

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