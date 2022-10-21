package io.mosip.resident.service.impl;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.DigitalCardStatusResponseDto;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;


@Service
public class DownloadCardServiceImpl implements DownloadCardService {

    private static final String AVAILABLE = "AVAILABLE";

    @Autowired
    private ResidentController residentController;

    @Autowired
    private Utilities utilities;

    @Autowired
    private AuditUtil audit;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectStoreHelper objectStoreHelper;

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Autowired
    private IdAuthService idAuthService;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
    public byte[] getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
        String rid = null;
        try {
            if (idAuthService.validateOtp(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId(),
                    downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId(), downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
                String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
                rid = utilities.getRidByIndividualId(individualId);
            } else {
                logger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
                        LoggerFileConstant.APPLICATIONID.toString(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
                audit.setAuditRequestDto(EventEnum.CREDENTIAL_REQ_EXCEPTION);
                throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(),
                        ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorMessage());
            }
        } catch (ApisResourceAccessException e) {
            audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
            throw new ResidentServiceException(
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        } catch (OtpValidationFailedException e) {
            audit.setAuditRequestDto(EventEnum.REQ_CARD);
            throw new ResidentServiceException(ResidentErrorCode.OTP_VALIDATION_FAILED.getErrorCode(), e.getErrorText(),
                    e);
        }
        return getUINCard(rid);
    }

    private byte[] getUINCard(String rid) {
        try {
            DigitalCardStatusResponseDto digitalCardStatusResponseDto = getDigitalCardStatus(rid);
            if(digitalCardStatusResponseDto!=null){
                if(!digitalCardStatusResponseDto.getStatusCode().equals(AVAILABLE)) {
                    audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
                    throw new ResidentServiceException(
                            ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorCode(),
                            ResidentErrorCode.DIGITAL_CARD_RID_NOT_FOUND.getErrorMessage());
                }
                URI dataShareUri = URI.create(digitalCardStatusResponseDto.getUrl());
                String encryptedData = residentServiceRestClient.getApi(dataShareUri, String.class);
                return decryptDataShareData(encryptedData);
            }
        } catch (ApisResourceAccessException e) {
            audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
            throw new ResidentServiceException(
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
        } catch (IOException e) {
            audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
            throw new ResidentServiceException(
                    ResidentErrorCode.IO_EXCEPTION.getErrorCode(),
                    ResidentErrorCode.IO_EXCEPTION.getErrorMessage(), e);
        }
        return new byte[0];
    }

    private byte[] decryptDataShareData(String encryptedData) {
        String decryptedData = objectStoreHelper.decryptData(encryptedData, env.getProperty(ResidentConstants.DATA_SHARE_APPLICATION_ID),
                env.getProperty(ResidentConstants.DATA_SHARE_REFERENCE_ID));
        return HMACUtils2.decodeBase64(decryptedData);
    }

    private DigitalCardStatusResponseDto getDigitalCardStatus(String individualId)
            throws ApisResourceAccessException, IOException {
        String digitalCardStatusUrl = env.getProperty(ApiName.DIGITAL_CARD_STATUS_URL.name()) +
                individualId;
        URI digitalCardStatusUri = URI.create(digitalCardStatusUrl);
        ResponseWrapper<DigitalCardStatusResponseDto> responseDto =
                residentServiceRestClient.getApi(digitalCardStatusUri, ResponseWrapper.class);
        return JsonUtil.readValue(
                JsonUtil.writeValueAsString(responseDto.getResponse()), DigitalCardStatusResponseDto.class);
    }

}
