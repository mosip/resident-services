package io.mosip.resident.util;

import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.exception.ApisResourceAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SessionUserNameUtility
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionUserNameUtilityTest {

    private SessionUserNameUtility sessionUserNameUtility;

    @Mock
    private Environment environment;

    @Mock
    private AvailableClaimValueUtility availableClaimValueUtility;

    @Before
    public void setUp() {
        sessionUserNameUtility = new SessionUserNameUtility();
        ReflectionTestUtils.setField(sessionUserNameUtility, "environment", environment);
        ReflectionTestUtils.setField(sessionUserNameUtility, "availableClaimValueUtility", availableClaimValueUtility);
    }

    @Test
    public void getSessionUserNameWhenNamePresentReturnsName() throws Exception {
        String profileKey = "profile.name";
        when(environment.getProperty(eq(ResidentConstants.NAME_FROM_PROFILE))).thenReturn(profileKey);
        when(availableClaimValueUtility.getAvailableClaimValue(eq(profileKey))).thenReturn("John Doe");

        String result = sessionUserNameUtility.getSessionUserName();

        assertEquals("John Doe", result);
        verify(environment, times(1)).getProperty(eq(ResidentConstants.NAME_FROM_PROFILE));
        verify(availableClaimValueUtility, times(1)).getAvailableClaimValue(eq(profileKey));
    }

    @Test
    public void getSessionUserNameWhenNameNullReturnsUnknownConstant() throws Exception {
        String profileKey = "profile.name";
        when(environment.getProperty(eq(ResidentConstants.NAME_FROM_PROFILE))).thenReturn(profileKey);
        when(availableClaimValueUtility.getAvailableClaimValue(eq(profileKey))).thenReturn(null);

        String result = sessionUserNameUtility.getSessionUserName();

        assertEquals(ResidentConstants.UNKNOWN, result);
        verify(availableClaimValueUtility, times(1)).getAvailableClaimValue(eq(profileKey));
    }

    @Test
    public void getSessionUserNameWhenNameBlankReturnsUnknownConstant() throws Exception {
        String profileKey = "profile.name";
        when(environment.getProperty(eq(ResidentConstants.NAME_FROM_PROFILE))).thenReturn(profileKey);
        when(availableClaimValueUtility.getAvailableClaimValue(eq(profileKey))).thenReturn("   ");

        String result = sessionUserNameUtility.getSessionUserName();

        assertEquals(ResidentConstants.UNKNOWN, result);
        verify(availableClaimValueUtility, times(1)).getAvailableClaimValue(eq(profileKey));
    }

    @Test(expected = RuntimeException.class)
    public void getSessionUserNameWhenAvailableClaimThrowsShouldWrapAndRethrow() throws Exception {
        String profileKey = "profile.name";
        when(environment.getProperty(eq(ResidentConstants.NAME_FROM_PROFILE))).thenReturn(profileKey);
        when(availableClaimValueUtility.getAvailableClaimValue(eq(profileKey)))
                .thenThrow(new ApisResourceAccessException("userinfo down"));

        // expect RuntimeException wrapping the ApisResourceAccessException
        sessionUserNameUtility.getSessionUserName();
    }
}
