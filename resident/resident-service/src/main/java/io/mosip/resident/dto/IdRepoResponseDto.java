package io.mosip.resident.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdRepoResponseDto implements Serializable {
	private static final long serialVersionUID = 8965769421273362497L;

	private Object identity;

	private List<Documents> documents;

	private String status;

}
