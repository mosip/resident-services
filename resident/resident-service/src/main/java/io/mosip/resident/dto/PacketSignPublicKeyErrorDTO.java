package io.mosip.resident.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonPropertyOrder({"message", "errorCode"})
public class PacketSignPublicKeyErrorDTO implements Serializable {

    private static final long serialVersionUID = -5261464773892046294L;

    /**
     * The message.
     */
    private String message;

    /**
     * The errorcode.
     */
    private String errorCode;

}
