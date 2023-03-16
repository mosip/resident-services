package io.mosip.resident.constant;

import java.util.Objects;

import org.springframework.core.env.Environment;

/**
 * @author Kamesh Shekhar Prasad
 */
public enum AuthenticationModeEnum {
    OTP("mosip.idp.acr.generated.code", "mosip.idp.otp.template.property"),
    BIOMETRICS("mosip.idp.acr.biometrics", "mosip.idp.biometrics.template.property"),
    UNKNOWN("mosip.idp.unknown.authentication", "mosip.idp.unknown.authentication.template.property");
    private String nameProperty;
    private String templatePropertyName;
    AuthenticationModeEnum(String nameProperty, String templatePropertyName){
        this.nameProperty = nameProperty;
        this.templatePropertyName = templatePropertyName;
    }

    public static String getTemplatePropertyName(String authenticationModeName, Environment environment) {
        for (AuthenticationModeEnum authenticationModeEnum : values()) {
            if (Objects.requireNonNull(environment.getProperty(authenticationModeEnum.nameProperty)).
                    equalsIgnoreCase(authenticationModeName)) {
                return authenticationModeEnum.templatePropertyName;
            }
        }
        return AuthenticationModeEnum.UNKNOWN.templatePropertyName;
    }

}
