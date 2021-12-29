package io.mosip.resident.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class is used to get machine data from the master data service.
 *
 * @author Abubacker Siddik
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MachineCreateResponseDTO extends BaseRestResponseDTO {

    private Map<String, Object> metadata;

    /**
     * Variable to hold machine data
     */
    private MachineDto response;
    /**
     * The error.
     */
    private List<MachineErrorDTO> errors;

}