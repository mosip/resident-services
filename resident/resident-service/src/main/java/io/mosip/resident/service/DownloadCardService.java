package io.mosip.resident.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import reactor.util.function.Tuple2;

/**
 * This class is used to create service class to download uin card.
 * @Author Kamesh Shekhar Prasad
 */
public interface DownloadCardService {
	Tuple2<byte[], String> getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO);

    Tuple2<byte[], String> downloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO);

    ResponseWrapper<VidDownloadCardResponseDto> getVidCardEventId(String vid) throws BaseCheckedException;
}
