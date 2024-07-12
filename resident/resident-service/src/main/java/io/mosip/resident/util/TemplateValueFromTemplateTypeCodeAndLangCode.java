package io.mosip.resident.util;

import io.mosip.resident.service.ProxyMasterdataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class TemplateValueFromTemplateTypeCodeAndLangCode {

    @Autowired
    private ProxyMasterdataService proxyMasterdataService;

    public String getTemplateValueFromTemplateTypeCodeAndLangCode(String languageCode, String templateTypeCode) {
        return proxyMasterdataService
                    .getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
    }
}
