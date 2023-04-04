package io.mosip.resident.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This class is used to search machine data from master data service.
 *
 * @author Abubacker Siddik
 */

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MachineSearchRequestDTO extends BaseRestRequestDTO {

    private Map<String, Object> metadata;

    /**
     * Variable to hold request
     */
    private MachineSearchRequest request;

    @Data
    @Builder
    public static class MachineSearchRequest {
        private String languageCode;
        private MachineSearchPagination pagination;
        private List<MachineSearchSort> sort;
        private List<MachineSearchFilter> filters;
    }

    @Data
    @Builder
    public static class MachineSearchPagination {
        private int pageStart;
        private int pageFetch;
    }

    @Data
    @Builder
    public static class MachineSearchSort {
        private String sortField;
        private String sortType;
    }

    @Data
    @Builder
    public static class MachineSearchFilter {
        private String value;
        private String fromValue;
        private String toValue;
        private String columnName;
        private String type;
    }

}