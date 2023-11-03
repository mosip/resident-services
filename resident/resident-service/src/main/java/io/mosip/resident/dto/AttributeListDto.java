package io.mosip.resident.dto;

import java.util.List;

import lombok.Data;

/**
 * The attribute list dto.
 *
 * @author Kamesh Shekhar Prasad
 */
@Data
public class AttributeListDto {

    private List<UpdateCountDto> attributes;

}
