package io.mosip.resident.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BiometricRequestDto {

    private String id;
    private String person;
    private List<BiometricType> modalities;
    private String source;
    private String process;
    private boolean bypassCache;
}
