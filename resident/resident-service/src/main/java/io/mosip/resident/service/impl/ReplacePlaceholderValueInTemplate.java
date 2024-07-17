package io.mosip.resident.service.impl;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.util.DescriptionTemplateVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class ReplacePlaceholderValueInTemplate {

    @Autowired
    private DescriptionTemplateVariables descriptionTemplateVariables;

    public String replacePlaceholderValueInTemplate(ResidentTransactionEntity residentTransactionEntity, String fileText, RequestType requestType, String langCode) {
        return requestType.getDescriptionTemplateVariables(descriptionTemplateVariables, residentTransactionEntity, fileText, langCode);
    }
}
