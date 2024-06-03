package io.mosip.resident.util;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UinVidValidator {

    @Autowired
    private UinValidator<String> uinValidator;

    @Autowired
    private VidValidator<String> vidValidator;

    @Autowired
    private IdentityUtil identityUtil;

    public String getUinForIndividualId(String idvid) throws ResidentServiceCheckedException {
        if(getIndividualIdType(idvid).equals(IdType.UIN)){
            return idvid;
        }
        return identityUtil.getIdentity(idvid).getUIN();
    }

    public IdType getIndividualIdType(String individualId){
        if(validateUin(individualId)){
            return IdType.UIN;
        } else if(validateVid(individualId)){
            return IdType.VID;
        } else {
            return IdType.AID;
        }
    }

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
