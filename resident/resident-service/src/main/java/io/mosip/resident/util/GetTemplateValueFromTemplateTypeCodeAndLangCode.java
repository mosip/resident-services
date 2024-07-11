package io.mosip.resident.util;

import io.mosip.resident.service.ProxyMasterdataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetTemplateValueFromTemplateTypeCodeAndLangCode {

    @Autowired
    private ProxyMasterdataService proxyMasterdataService;

    public String getTemplateValueFromTemplateTypeCodeAndLangCode(String languageCode, String templateTypeCode) {
        return proxyMasterdataService
                    .getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
    }
}
