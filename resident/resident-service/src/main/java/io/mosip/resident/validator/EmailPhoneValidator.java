package io.mosip.resident.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class EmailPhoneValidator {

    @Value("${mosip.id.validation.identity.phone}")
    private String phoneRegex;

    @Value("${mosip.id.validation.identity.email}")
    private String emailRegex;

    public boolean emailValidator(String email) {
		return email.matches(emailRegex);
	}

    public boolean phoneValidator(String phone) {
        return phone.matches(phoneRegex);
    }
}
