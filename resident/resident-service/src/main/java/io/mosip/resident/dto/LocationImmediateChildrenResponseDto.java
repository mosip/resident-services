package io.mosip.resident.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Kamesh Shekhar Prasad
 */
@Data
public class LocationImmediateChildrenResponseDto {
    Map<String, List<Map<String, Object>>> locations;
}
