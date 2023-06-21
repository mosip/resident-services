package io.mosip.resident.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kamesh Shekhar Prasad
 * Dynamic Field Consolidate Response Dto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Model representing a response of dynamic field get by name Request")
public class DynamicFieldConsolidateResponseDto {

    private String name;

    private String description;

    private List<DynamicFieldCodeValueDTO> values;

}
