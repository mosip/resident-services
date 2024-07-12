package io.mosip.resident.util;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class AccessTokenUtility {

    @Autowired
    AuthUserDetailsUtil authUserDetailsUtil;

    public  String getAccessToken(){
        AuthUserDetails authUserDetails = authUserDetailsUtil.getAuthUserDetails();
        if(authUserDetails != null){
            return authUserDetails.getToken();
        }
        return "";
    }


}
