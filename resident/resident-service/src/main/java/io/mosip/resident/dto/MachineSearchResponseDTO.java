package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * This class is used to search machine data from master data service.
 *
 * @author Abubacker Siddik
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachineSearchResponseDTO extends BaseRestResponseDTO {

    private Map<String, Object> metadata;

    /**
     * Variable to hold machine data
     */
    private MachineSearchDto response;

    /**
     * The error.
     */
    private List<MachineErrorDTO> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MachineSearchDto {
        private int fromRecord;
        private int toRecord;
        private int totalRecord;
        private List<MachineDto> data;
    }
}