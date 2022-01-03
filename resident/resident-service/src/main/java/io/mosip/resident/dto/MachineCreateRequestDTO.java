package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Map;

/**
 * This class is used to create machine in the master data service.
 *
 * @author Abubacker Siddik
 */

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MachineCreateRequestDTO  extends BaseRestRequestDTO {

    private Map<String, Object> metadata;

    /**
     * Variable to hold machine data
     */
    private MachineDto request;

}