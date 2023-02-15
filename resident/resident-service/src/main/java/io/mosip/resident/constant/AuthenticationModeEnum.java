package io.mosip.resident.constant;

/**
 * @author Kamesh Shekhar Prasad
 */
public enum AuthenticationModeEnum {
    OTP("mosip:idp:acr:generated-code", "mosip.idp.otp.template.property"),
    BIOMETRICS("mosip:idp:acr:biometrics", "mosip.idp.biometrics.template.property"),
    UNKNOWN("Unknown", "mosip.idp.unknown.authentication.template.property");
    private String authenticationModeName;
    private String templatePropertyName;
    AuthenticationModeEnum(String name, String templatePropertyName){
        this.authenticationModeName = name;
        this.templatePropertyName = templatePropertyName;
    }

    public static String getTemplatePropertyName(String authenticationModeName) {
        for (AuthenticationModeEnum authenticationModeEnum : values()) {
            if (authenticationModeEnum.authenticationModeName.equalsIgnoreCase(authenticationModeName)) {
                return authenticationModeEnum.templatePropertyName;
            }
        }
        return AuthenticationModeEnum.UNKNOWN.templatePropertyName;
    }

}
