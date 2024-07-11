package io.mosip.resident.service.impl;

import io.mosip.resident.constant.*;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import io.mosip.resident.util.GetPurposeTemplateTypeCode;
import io.mosip.resident.util.GetTemplateValueFromTemplateTypeCodeAndLangCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetDescriptionForLangCode {

    @Autowired
    private ReplacePlaceholderValueInTemplate replacePlaceholderValueInTemplate;

    @Autowired
    private GetTemplateValueFromTemplateTypeCodeAndLangCode getTemplateValueFromTemplateTypeCodeAndLangCode;

    @Autowired
    private GetPurposeTemplateTypeCode getPurposeTemplateTypeCode;

    public String getDescriptionForLangCode(ResidentTransactionEntity residentTransactionEntity, String langCode, String statusCode, RequestType requestType)
            throws ResidentServiceCheckedException {
        TemplateType templateType;
        if (statusCode.equalsIgnoreCase(EventStatus.SUCCESS.name())) {
            templateType = TemplateType.SUCCESS;
        } else if (statusCode.equalsIgnoreCase(EventStatusCanceled.CANCELED.name())) {
            templateType = TemplateType.CANCELED;
        }else if (residentTransactionEntity.getStatusCode().equalsIgnoreCase(EventStatusInProgress.IDENTITY_UPDATED.name())) {
            templateType = TemplateType.REGPROC_SUCCESS;
        } else {
            templateType = TemplateType.FAILURE;
        }
        String templateTypeCode = getPurposeTemplateTypeCode.getPurposeTemplateTypeCode(requestType, templateType);
        String fileText = getTemplateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(langCode, templateTypeCode);
        return replacePlaceholderValueInTemplate.replacePlaceholderValueInTemplate(residentTransactionEntity, fileText, requestType, langCode);
    }
}
