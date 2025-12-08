package io.mosip.resident.util;

import io.mosip.resident.exception.ApisResourceAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AvailableClaimValueUtility
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class AvailableClaimValueUtilityTest {

    private AvailableClaimValueUtility availableClaimValueUtility;

    @Mock
    private AccessTokenUtility accessTokenUtility;

    @Mock
    private UserInfoUtility userInfoUtility;

    @Before
    public void setUp() {
        availableClaimValueUtility = new AvailableClaimValueUtility();

        ReflectionTestUtils.setField(availableClaimValueUtility, "accessTokenUtility", accessTokenUtility);
        ReflectionTestUtils.setField(availableClaimValueUtility, "userInfoUtility", userInfoUtility);
    }

    @Test
    public void testGetClaimsFromTokenSuccess() throws Exception {
        String token = "TOKEN-1";
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", "user123");
        userInfo.put("email", "user@example.com");

        when(userInfoUtility.getUserInfo(eq(token))).thenReturn(userInfo);

        Map<String, String> result = availableClaimValueUtility.getClaimsFromToken(Set.of("sub", "email"), token);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user123", result.get("sub"));
        assertEquals("user@example.com", result.get("email"));

        verify(userInfoUtility, times(1)).getUserInfo(eq(token));
    }

    @Test
    public void testGetClaimsWhenAccessTokenEmptyReturnsEmptyMap() throws Exception {
        when(accessTokenUtility.getAccessToken()).thenReturn("");

        Map<String, String> result = availableClaimValueUtility.getClaims(Set.of("sub"));

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accessTokenUtility, times(1)).getAccessToken();
        verifyNoInteractions(userInfoUtility);
    }

    @Test
    public void testGetClaimsVarargsDelegatesToSet() throws Exception {
        String token = "TOK";
        when(accessTokenUtility.getAccessToken()).thenReturn(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("a", "A");
        userInfo.put("b", "B");
        when(userInfoUtility.getUserInfo(eq(token))).thenReturn(userInfo);

        Map<String, String> result = availableClaimValueUtility.getClaims("a", "b");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get("a"));
        assertEquals("B", result.get("b"));

        verify(accessTokenUtility, times(1)).getAccessToken();
        verify(userInfoUtility, times(1)).getUserInfo(eq(token));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetClaimsFromTokenWhenUserInfoThrowsApisExceptionPropagates() throws Exception {
        String token = "TOK_ERR";
        when(userInfoUtility.getUserInfo(eq(token))).thenThrow(new ApisResourceAccessException("down"));

        // should propagate the ApisResourceAccessException
        availableClaimValueUtility.getClaimsFromToken(Set.of("x"), token);
    }

    @Test
    public void testGetAvailableClaimValueWhenClaimMissingReturnsNull() throws Exception {
        // access token present
        String token = "T-1";
        when(accessTokenUtility.getAccessToken()).thenReturn(token);

        // userInfo does not contain the requested claim -> getClaimFromUserInfo will throw ResidentServiceException
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("other", "value");
        when(userInfoUtility.getUserInfo(eq(token))).thenReturn(userInfo);

        // call method under test - it should catch ResidentServiceException and return null
        String result = availableClaimValueUtility.getAvailableClaimValue("missingClaim");

        assertNull(result);

        verify(accessTokenUtility, times(1)).getAccessToken();
        verify(userInfoUtility, times(1)).getUserInfo(eq(token));
    }
}
