package io.mosip.resident.service;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.resident.dto.CheckStatusResponseDTO;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCardDto;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.dto.VidDownloadCardResponseDto;
import io.mosip.resident.exception.ApisResourceAccessException;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

import java.io.IOException;

/**
 * This class is used to create service class to download uin card.
 * @Author Kamesh Shekhar Prasad
 */
public interface DownloadCardService {
    Tuple2<byte[], String> getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO);

    Tuple2<byte[], String> downloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCardDto> downloadPersonalizedCardMainRequestDTO, int timeZoneOffset);

    Tuple2<ResponseWrapper<VidDownloadCardResponseDto>, String> getVidCardEventId(String vid, int timeZoneOffset) throws BaseCheckedException;

    ResponseWrapper<CheckStatusResponseDTO> getIndividualIdStatus(String vid) throws ApisResourceAccessException, IOException;
}
