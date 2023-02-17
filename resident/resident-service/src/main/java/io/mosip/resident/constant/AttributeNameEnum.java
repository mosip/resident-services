package io.mosip.resident.constant;

/**
 * @author Kamesh Shekhar Prasad
 */
public enum AttributeNameEnum {
    FULL_NAME("fullName", "mosip.full.name.template.property"),
    DATE_OF_BIRTH("dateOfBirth", "mosip.date.of.birth.template.property"),
    UIN("UIN", "mosip.uin.template.property"),
    PERPETUAL_VID("perpetualVID", "mosip.perpetual.vid.template.property"),
    PHONE("phone", "mosip.phone.template.property"),
    EMAIL("email", "mosip.email.template.property"),
    ADDRESS("addressLine1", "mosip.address.template.property"),
    GENDER("gender", "mosip.gender.template.property"),
    DEFAULT("Default", "mosip.defualt.template.property");
    private String attributeValue;
    private String templatePropertyName;
    AttributeNameEnum(String name, String templatePropertyName){
        this.attributeValue = name;
        this.templatePropertyName = templatePropertyName;
    }

    public static String getTemplatePropertyName(String attributeName) {
        for (AttributeNameEnum authenticationModeEnum : values()) {
            if (authenticationModeEnum.attributeValue.equalsIgnoreCase(attributeName)) {
                return authenticationModeEnum.templatePropertyName;
            }
        }
        return AttributeNameEnum.DEFAULT.templatePropertyName;
    }

}
