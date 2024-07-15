package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class AttributeBasedOnLangCode {

    private static final String RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST = "resident.%s.template.property.attribute.list";

    @Autowired
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    private static final Logger logger = LoggerConfiguration.logConfig(AttributeBasedOnLangCode.class);

    @Autowired
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    public String getAttributeBasedOnLangcode(String attributeName, String languageCode) {
        String templateTypeCode = eventStatusBasedOnLangCode.getTemplateTypeCode(
                String.format(RESIDENT_TEMPLATE_PROPERTY_ATTRIBUTE_LIST, attributeName));
        if (templateTypeCode == null) {
            logger.warn(String.format("Template property is missing for %s", attributeName));
            templateTypeCode = eventStatusBasedOnLangCode.getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
        }
        return templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
    }
}
