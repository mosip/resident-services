package io.mosip.resident.service;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidRequestDto;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeRequestDTO;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface ResidentVidService {

    public ResponseWrapper<VidResponseDto> generateVid(VidRequestDto requestDto) throws OtpValidationFailedException, ResidentServiceCheckedException;

    public ResponseWrapper<VidRevokeResponseDTO> revokeVid(VidRevokeRequestDTO requestDto,String vid) throws OtpValidationFailedException, ResidentServiceCheckedException;

	public String getVidPolicy() throws ResidentServiceCheckedException;

}
