package io.mosip.resident.util;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.TemplateType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class GetSmsTemplateTypeCode {

    @Autowired
    private GetEventStatusBasedOnLangCode getEventStatusBasedOnLangCode;

    public String getSmsTemplateTypeCode(RequestType requestType, TemplateType templateType) {
        String smsTemplateCodeProperty = requestType.getSmsTemplateCodeProperty(templateType);
        return getEventStatusBasedOnLangCode.getTemplateTypeCode(smsTemplateCodeProperty);
    }
}
