package io.mosip.resident.util;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UinVidValidator {

    @Autowired
    private UinValidator<String> uinValidator;

    @Autowired
    private VidValidator<String> vidValidator;

    public boolean validateVid(String individualId) {
        try {
            return vidValidator.validateId(individualId);
        } catch (InvalidIDException e) {
            return false;
        }
    }

    public boolean validateUin(String individualId) {
        try {
            return uinValidator.validateId(individualId);
        } catch (InvalidIDException e) {
            return false;
        }
    }
}
