package io.mosip.resident.util;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GetAccessTokenUtility {

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
