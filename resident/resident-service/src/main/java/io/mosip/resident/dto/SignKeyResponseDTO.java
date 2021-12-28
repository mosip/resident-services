package io.mosip.resident.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * This class is used to map TPM signkey response from keymanager.
 *
 * @author Abubacker Siddik
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SignKeyResponseDTO extends BaseRestResponseDTO {

    private Map<String, Object> metadata;

    /**
     * The response.
     */
    private SignResponseDTO response;

    /**
     * The error.
     */
    private List<SignKeyErrorDTO> errors;

    @Data
    public static class SignResponseDTO {
        /**
         * Variable to hold public key
         */
        private String publicKey;
    }

}