package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.EventStatus;
import io.mosip.resident.constant.ResidentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GetEventStatusBasedOnLangCode {

    private static final String RESIDENT_TEMPLATE_EVENT_STATUS = "resident.event.status.%s.template.property";
    private static final Logger logger = LoggerConfiguration.logConfig(GetEventStatusBasedOnLangCode.class);

    @Autowired
    private Environment environment;

    @Autowired
    private GetTemplateValueFromTemplateTypeCodeAndLangCode getTemplateValueFromTemplateTypeCodeAndLangCode;

    public String getTemplateTypeCode(String templateCodeProperty) {
        return environment.getProperty(templateCodeProperty);
    }

    public String getEventStatusBasedOnLangcode(EventStatus eventStatus, String languageCode) {
        String templateCodeProperty = String.format(RESIDENT_TEMPLATE_EVENT_STATUS, eventStatus.name());
        String templateTypeCode = getTemplateTypeCode(templateCodeProperty);
        if (templateTypeCode == null) {
            logger.warn(String.format("Template property is missing for %s", eventStatus.name()));
            templateTypeCode = getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
        }
        return getTemplateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
    }
}
