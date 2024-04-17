package io.mosip.resident.service;

import java.io.IOException;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.CheckStatusResponseDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import reactor.util.function.Tuple2;

/**
 * This class is used to create service class to download uin card.
 * @Author Kamesh Shekhar Prasad
 */
public interface DownloadCardService {
	Tuple2<byte[], String> getDownloadCardPDF(
			MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO)
			throws ResidentServiceCheckedException, OtpValidationFailedException;

	Tuple2<byte[], String> downloadPersonalizedCard(
			MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO, int timeZoneOffset, String locale)
			throws ResidentServiceCheckedException;

    Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> getVidCardEventId(String vid, int timeZoneOffset, String locale) throws BaseCheckedException;

    ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus(String vid) throws ApisResourceAccessException, IOException, ResidentServiceCheckedException;
}
