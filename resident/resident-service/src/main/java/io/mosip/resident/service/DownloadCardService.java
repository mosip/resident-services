package io.mosip.resident.service;

import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.DownloadPersonalizedCard;
import io.mosip.resident.dto.MainRequestDTO;

/**
 * This class is used to create service class to download uin card.
 * @Author Kamesh Shekhar Prasad
 */
public interface DownloadCardService {
    byte[] getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO);

    byte[] downloadPersonalizedCard(MainRequestDTO<DownloadPersonalizedCard> downloadHtml2PdfRequestDTOMainRequestDTO);

    String getFileName();
}
