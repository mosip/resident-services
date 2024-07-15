package io.mosip.resident.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ResidentConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class AuthTypeCodeTemplateData {

    private static final String RESIDENT_AUTH_TYPE_CODE_TEMPLATE_PROPERTY = "resident.auth-type-code.%s.code";
    private static final String RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR = "resident.id-auth.request-type.%s.%s.descr";
    private static final String UNKNOWN = "UNKNOWN";

    @Autowired
    private TemplateValueFromTemplateTypeCodeAndLangCode templateValueFromTemplateTypeCodeAndLangCode;

    @Autowired
    private EventStatusBasedOnLangCode eventStatusBasedOnLangCode;

    private static final Logger logger = LoggerConfiguration.logConfig(AuthTypeCodeTemplateData.class);

    public String getAuthTypeCodeTemplateData(String authTypeCodeFromDB, String statusCode, String languageCode) {
        List<String> authTypeCodeTemplateValues = new ArrayList<>();
        if (authTypeCodeFromDB != null && !authTypeCodeFromDB.isEmpty()) {
            authTypeCodeTemplateValues = List.of(authTypeCodeFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
                    .map(authTypeCode -> {
                        String templateTypeCode;
                        if(statusCode == null) {
                            templateTypeCode = getAuthTypeCodeTemplateTypeCode(authTypeCode.trim());
                        } else {
                            templateTypeCode = getIDAuthRequestTypeDescriptionTemplateTypeCode(authTypeCode.trim(), statusCode);
                        }
                        return templateValueFromTemplateTypeCodeAndLangCode.getTemplateValueFromTemplateTypeCodeAndLangCode(languageCode, templateTypeCode);
                    })
                    .collect(Collectors.toList());
        }

        if (authTypeCodeTemplateValues.isEmpty()) {
            return "";
        } else {
            return authTypeCodeTemplateValues.stream()
                    .collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
        }
    }

    private String getAuthTypeCodeTemplateTypeCode(String authTypeCode) {
        String templateCodeProperty = String.format(RESIDENT_AUTH_TYPE_CODE_TEMPLATE_PROPERTY, authTypeCode);
        String templateTypeCode = eventStatusBasedOnLangCode.getTemplateTypeCode(templateCodeProperty);
        if (templateTypeCode == null) {
            logger.warn(String.format("Template property is missing for %s", authTypeCode));
            return eventStatusBasedOnLangCode.getTemplateTypeCode(ResidentConstants.RESIDENT_UNKNOWN_TEMPLATE_PROPERTY);
        } else {
            return templateTypeCode;
        }
    }

    private String getIDAuthRequestTypeDescriptionTemplateTypeCode(String authTypeCode, String statusCode) {
        String templateCodeProperty = String.format(RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR, authTypeCode, statusCode);
        String templateTypeCode = eventStatusBasedOnLangCode.getTemplateTypeCode(templateCodeProperty);
        if (templateTypeCode == null) {
            logger.warn(String.format("Template property is missing for %s", authTypeCode));
            return eventStatusBasedOnLangCode.getTemplateTypeCode(String.format(RESIDENT_ID_AUTH_REQUEST_TYPE_DESCR, UNKNOWN, statusCode));
        } else {
            return templateTypeCode;
        }
    }
}
