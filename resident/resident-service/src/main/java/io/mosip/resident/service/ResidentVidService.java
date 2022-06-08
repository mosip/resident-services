package io.mosip.resident.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;

@Service
public interface ResidentVidService {

    public ResponseWrapper<VidResponseDto> generateVid(BaseVidRequestDto requestDto, String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException;

    public ResponseWrapper<VidRevokeResponseDTO> revokeVid(BaseVidRevokeRequestDTO requestDto,String vid, String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException;

	public String getVidPolicy() throws ResidentServiceCheckedException;

	public ResponseWrapper<List<Map<String, ?>>> retrieveVids(String residentIndividualId) throws ResidentServiceCheckedException, ApisResourceAccessException;

	public Optional<String> getPerpatualVid(String uin) throws ResidentServiceCheckedException, ApisResourceAccessException;

}
