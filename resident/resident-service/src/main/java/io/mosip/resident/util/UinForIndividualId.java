package io.mosip.resident.util;

import io.mosip.resident.constant.IdType;
import io.mosip.resident.exception.ResidentServiceCheckedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UinForIndividualId {

    @Autowired
    private UinVidValidator uinVidValidator;

    @Autowired
    private IdentityUtil identityUtil;

    public String getUinForIndividualId(String idvid) throws ResidentServiceCheckedException {
        if(uinVidValidator.getIndividualIdType(idvid).equals(IdType.UIN)){
            return idvid;
        }
        return identityUtil.getIdentity(idvid).getUIN();
    }


}
