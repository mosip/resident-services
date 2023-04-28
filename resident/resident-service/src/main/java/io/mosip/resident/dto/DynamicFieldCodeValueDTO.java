package io.mosip.resident.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Kamesh Shekhar Prasad
 * Dynamic Field Code value Dto
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFieldCodeValueDTO {

    private String code;

    private String value;

}
