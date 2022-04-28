package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.IdRepoResponseDto;
import io.mosip.resident.dto.IdentityDTO;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface IdentityService {

	public IdentityDTO getIdentity(String id) throws ResidentServiceCheckedException;

}
