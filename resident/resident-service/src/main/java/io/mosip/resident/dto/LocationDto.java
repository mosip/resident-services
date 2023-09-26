package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Kamesh Shekhar Prasad
 * @since 1.0.0
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull
    private String code;

    @NotNull
    private String name;

    @Range(min = 0)
    private short hierarchyLevel;

    @NotNull
    private String hierarchyName;

    @Size(max = 32)
    private String parentLocCode;

    private String langCode;

    @NotNull
    private Boolean isActive;

}

