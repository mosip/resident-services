package io.mosip.resident.util;

import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test class for AccessTokenUtility
 *
 * @author Kamesh Shekhar Prasad
 */

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenUtilityTest {

    private AccessTokenUtility accessTokenUtility;

    @Mock
    private AuthUserDetailsUtil authUserDetailsUtil;

    @Before
    public void setUp() {
        accessTokenUtility = new AccessTokenUtility();

        // inject mock manually so no Spring context is required
        ReflectionTestUtils.setField(accessTokenUtility, "authUserDetailsUtil", authUserDetailsUtil);
    }

    @Test
    public void testGetAccessToken_whenUserDetailsExists_shouldReturnToken() {
        MosipUserDto mosipUserDto = new MosipUserDto();
        mosipUserDto.setUserId("testUser");
        AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, "TEST_TOKEN");
        authUserDetails.setToken("TEST_TOKEN_123");

        when(authUserDetailsUtil.getAuthUserDetails()).thenReturn(authUserDetails);

        String token = accessTokenUtility.getAccessToken();

        assertEquals("TEST_TOKEN_123", token);
        verify(authUserDetailsUtil, times(1)).getAuthUserDetails();
    }

    @Test
    public void testGetAccessToken_whenUserDetailsNull_shouldReturnEmptyString() {
        when(authUserDetailsUtil.getAuthUserDetails()).thenReturn(null);

        String token = accessTokenUtility.getAccessToken();

        assertEquals("", token);
        verify(authUserDetailsUtil, times(1)).getAuthUserDetails();
    }
}
