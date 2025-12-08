package io.mosip.resident.util;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.when;

/**
 * Utility class for setting up authentication details in tests.
 *
 * @author Kamesh Shekhar Prasad
 */

public class AuthenticationTestUtil {

    private static final String TOKEN = "dummy.jwt.token.for.tests";
    
    public static void getAuthUserDetailsFromAuthentication() {
        Authentication authentication= Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setToken(TOKEN);
        // test the case where the principal is an AuthUserDetails object
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, TOKEN);
        when(authentication.getPrincipal()).thenReturn(authUserDetails);
    }
}
