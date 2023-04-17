package io.mosip.resident.constant;

import org.springframework.core.env.Environment;

/**
 * @author Kamesh Shekhar Prasad
 */
public enum AttributeNameEnum {
    FULL_NAME("fullName", "mosip.full.name.template.property.attribute.list"),
    DATE_OF_BIRTH("dateOfBirth", "mosip.date.of.birth.template.property.attribute.list"),
    UIN("UIN", "mosip.uin.template.property.attribute.list"),
    PERPETUAL_VID("perpetualVID", "mosip.perpetual.vid.template.property.attribute.list"),
    PHONE("phone", "mosip.phone.template.property.attribute.list"),
    EMAIL("email", "mosip.email.template.property.attribute.list"),
    ADDRESS("addressLine1", "mosip.address.template.property.attribute.list"),
    PROVINCE("province", "mosip.province.template.property.attribute.list"),
    CITY("city", "mosip.city.template.property.attribute.list"),
    ZONE("zone", "mosip.zone.template.property.attribute.list"),
    POSTAL_CODE("postalCode", "mosip.postal.code.template.property.attribute.list"),
    REGION("region", "mosip.region.template.property.attribute.list"),
    GENDER("gender", "mosip.gender.template.property.attribute.list"),
    DEFAULT("Default", "mosip.defualt.template.property.attribute.list");
    private String attributeValue;
    private String templatePropertyName;
    AttributeNameEnum(String name, String templatePropertyName){
        this.attributeValue = name;
        this.templatePropertyName = templatePropertyName;
    }

    public static String getTemplatePropertyName(String attributeName, Environment env) {
        for (AttributeNameEnum attributeNameEnum : values()) {
            if (attributeNameEnum.attributeValue.equalsIgnoreCase(attributeName)) {
                return env.getProperty(attributeNameEnum.templatePropertyName);
            }
        }
        return env.getProperty(AttributeNameEnum.DEFAULT.templatePropertyName);
    }

}
