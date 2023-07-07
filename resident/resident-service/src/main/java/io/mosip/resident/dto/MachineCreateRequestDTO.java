package io.mosip.resident.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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