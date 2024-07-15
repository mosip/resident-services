package io.mosip.resident.util;

import io.mosip.resident.constant.RequestType;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.constant.UISchemaTypes;
import io.mosip.resident.handler.service.ResidentConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class AttributesDisplayText {

    @Autowired
    private ResidentConfigService residentConfigService;

    @Autowired
    private AttributeBasedOnLangCode attributeBasedOnLangCode;

    /**
     * This method accepts a string having comma-separated attributes with camel
     * case convention and splits it by a comma. Then it takes each attribute value
     * from the template in logged-in language and appends it to a string with
     * comma-separated value.
     *
     * @param attributesFromDB attribute values having comma separated attributes.
     * @param languageCode     logged in language code.
     * @return attribute value stored in the template.
     */
    public String getAttributesDisplayText(String attributesFromDB, String languageCode, RequestType requestType) {
        List<String> attributeListTemplateValue = new ArrayList<>();
        if (attributesFromDB != null && !attributesFromDB.isEmpty()) {
            Optional<String> schemaType = UISchemaTypes.getUISchemaTypeFromRequestTypeCode(requestType);
            if (schemaType.isPresent()) {
//	    		Cacheable UI Schema data
                Map<String, Map<String, Object>> uiSchemaDataMap = residentConfigService
                        .getUISchemaCacheableData(schemaType.get()).get(languageCode);
                List<String> attributeListFromDB = List.of(attributesFromDB.split(ResidentConstants.SEMI_COLON));
                attributeListTemplateValue = attributeListFromDB.stream().map(attribute -> {
                    String[] attrArray = attribute.trim().split(ResidentConstants.COLON);
                    String attr = attrArray[0];
                    if (uiSchemaDataMap.containsKey(attr)) {
                        Map<String, Object> attributeDataFromUISchema = (Map<String, Object>) uiSchemaDataMap.get(attr);
                        attr = (String) attributeDataFromUISchema.get(ResidentConstants.LABEL);
                        if (attrArray.length > 1) {
                            String formatAttr = attrArray[1];
                            Map<String, String> formatDataMapFromUISchema = (Map<String, String>) attributeDataFromUISchema
                                    .get(ResidentConstants.FORMAT_OPTION);
                            List<String> formatAttrList = List
                                    .of(formatAttr.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
                                    .map(String::trim).map(format -> formatDataMapFromUISchema.get(format))
                                    .collect(Collectors.toList());
                            if (!formatAttrList.contains(null)) {
                                return String.format("%s%s%s%s", attr, ResidentConstants.OPEN_PARENTHESIS,
                                        formatAttrList.stream().collect(
                                                Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER)),
                                        ResidentConstants.CLOSE_PARENTHESIS);
                            }
                        }
                    }
                    return attr;
                }).collect(Collectors.toList());
            } else {
                attributeListTemplateValue = List.of(attributesFromDB.split(ResidentConstants.ATTRIBUTE_LIST_DELIMITER)).stream()
                        .map(attribute -> attributeBasedOnLangCode.getAttributeBasedOnLangcode(attribute.trim(), languageCode))
                        .collect(Collectors.toList());
            }
        }
        if (attributeListTemplateValue.isEmpty()) {
            return "";
        } else {
            return attributeListTemplateValue.stream()
                    .collect(Collectors.joining(ResidentConstants.UI_ATTRIBUTE_DATA_DELIMITER));
        }
    }
}
