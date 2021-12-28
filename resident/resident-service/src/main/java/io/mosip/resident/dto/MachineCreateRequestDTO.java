package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * This class is used to create machine in the master data service.
 *
 * @author Abubacker Siddik
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineCreateRequestDTO implements Serializable {

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
    private String requesttime;

    private Map<String, Object> metadata;

    /**
     * Variable to hold machine data
     */
    private MachineDto request;

}