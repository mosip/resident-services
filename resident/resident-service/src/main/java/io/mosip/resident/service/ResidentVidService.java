package io.mosip.resident.service;

import io.mosip.resident.dto.BaseVidRequestDto;
import io.mosip.resident.dto.BaseVidRevokeRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidResponseDto;
import io.mosip.resident.dto.VidRevokeResponseDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.stereotype.Service;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public interface ResidentVidService {

    public ResponseWrapper<VidResponseDto> generateVid(BaseVidRequestDto requestDto, String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException;

    public ResponseWrapper<VidRevokeResponseDTO> revokeVid(BaseVidRevokeRequestDTO requestDto,String vid, String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException;

	public String getVidPolicy() throws ResidentServiceCheckedException;

	public ResponseWrapper<List<Map<String, ?>>> retrieveVids(String residentIndividualId, int timeZoneOffset, String locale) throws ResidentServiceCheckedException, ApisResourceAccessException;

	public Optional<String> getPerpatualVid(String uin) throws ResidentServiceCheckedException, ApisResourceAccessException;

	public Tuple2<ResponseWrapper<VidResponseDto>, String> generateVidV2(BaseVidRequestDto requestDto,
			String individualId) throws OtpValidationFailedException, ResidentServiceCheckedException;

	public Tuple2<ResponseWrapper<VidRevokeResponseDTO>, String> revokeVidV2(BaseVidRevokeRequestDTO requestDto,
			String vid, String indivudalId)
			throws OtpValidationFailedException, ResidentServiceCheckedException, ApisResourceAccessException, IOException;
	
	ResponseWrapper<List<Map<String, ?>>> retrieveVidsfromUin(String uin, int timeZoneOffset, String locale)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

	ResponseWrapper<List<Map<String, ?>>> retrieveVids(int timeZoneOffset, String locale, String uin)
			throws ResidentServiceCheckedException, ApisResourceAccessException;

}
