package io.mosip.resident.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.controller.ResidentController;
import io.mosip.resident.dto.DownloadCardRequestDTO;
import io.mosip.resident.dto.MainRequestDTO;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.OtpValidationFailedException;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
import io.mosip.resident.service.DownloadCardService;
import io.mosip.resident.service.IdAuthService;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.EventEnum;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.TemplateUtil;
import io.mosip.resident.util.Utilities;
import io.mosip.resident.util.Utilitiy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author Kamesh Shekhar Prasad
 * This class is used to create service class implementation of download card api.
 */
@Service
public class DownloadCardServiceImpl implements DownloadCardService {

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

    @Autowired
    private ResidentServiceImpl residentService;

    @Autowired
    private Utilitiy utilitiy;

    @Autowired
    private IdentityServiceImpl identityService;

    @Autowired
    private TemplateUtil templateUtil;

    private static final Logger logger = LoggerConfiguration.logConfig(DownloadCardServiceImpl.class);

    @Override
    public byte[] getDownloadCardPDF(MainRequestDTO<DownloadCardRequestDTO> downloadCardRequestDTOMainRequestDTO) {
        String rid = null;
        try {
            if (idAuthService.validateOtp(downloadCardRequestDTOMainRequestDTO.getRequest().getTransactionId(),
                    getUINForIndividualId(downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId()), downloadCardRequestDTOMainRequestDTO.getRequest().getOtp())) {
                String individualId = downloadCardRequestDTOMainRequestDTO.getRequest().getIndividualId();
                String idType=templateUtil.getIndividualIdType(individualId);
                if(idType.equalsIgnoreCase(IdType.RID.toString())){
                    rid = individualId;
                } else {
                    rid = utilities.getRidByIndividualId(individualId);
                }
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
        return residentService.getUINCard(rid);
    }

    private String getUINForIndividualId(String individualId)  {
        String idType = templateUtil.getIndividualIdType(individualId);
        if(idType.equalsIgnoreCase(IdType.UIN.toString()) || idType.equalsIgnoreCase(IdType.VID.toString())){
            return individualId;
        } else {
            try {
                return identityService.getIndividualIdForAid(individualId);
            } catch (ResidentServiceCheckedException e) {
                audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
                throw new ResidentServiceException(
                        ResidentErrorCode.AID_NOT_FOUND.getErrorCode(),
                        ResidentErrorCode.AID_NOT_FOUND.getErrorMessage(), e);
            } catch (ApisResourceAccessException e) {
                audit.setAuditRequestDto(EventEnum.RID_DIGITAL_CARD_REQ_EXCEPTION);
                throw new ResidentServiceException(
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorCode(),
                        ResidentErrorCode.API_RESOURCE_ACCESS_EXCEPTION.getErrorMessage(), e);
            }
        }
    }

}
