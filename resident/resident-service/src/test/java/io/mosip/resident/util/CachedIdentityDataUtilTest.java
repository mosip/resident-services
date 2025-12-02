package io.mosip.resident.util;

import io.mosip.resident.constant.ApiName;
import io.mosip.resident.exception.ApisResourceAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CachedIdentityDataUtil
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
public class CachedIdentityDataUtilTest {

    private CachedIdentityDataUtil cachedIdentityDataUtil;

    @Mock
    private ResidentServiceRestClient restClientWithSelfTokenRestTemplate;

    @Before
    public void setUp() {
        cachedIdentityDataUtil = new CachedIdentityDataUtil();

        // inject mock
        ReflectionTestUtils.setField(
                cachedIdentityDataUtil,
                "restClientWithSelfTokenRestTemplate",
                restClientWithSelfTokenRestTemplate
        );
    }

    @Test
    public void testGetIdentityData_success() throws Exception {
        String id = "123456";
        Class<?> responseType = String.class;
        String expectedResponse = "SUCCESS_PAYLOAD";

        when(restClientWithSelfTokenRestTemplate.getApi(
                eq(ApiName.IDREPO_IDENTITY_URL),
                anyMap(),
                anyList(),
                anyList(),
                eq(responseType)
        )).thenReturn(expectedResponse);

        String result = cachedIdentityDataUtil.getIdentityData(id, responseType);

        assertEquals(expectedResponse, result);

        // Capture request arguments
        ArgumentCaptor<Map> pathCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<List> nameCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<List> valueCaptor = ArgumentCaptor.forClass(List.class);

        verify(restClientWithSelfTokenRestTemplate, times(1))
                .getApi(eq(ApiName.IDREPO_IDENTITY_URL),
                        pathCaptor.capture(),
                        nameCaptor.capture(),
                        valueCaptor.capture(),
                        eq(responseType));

        // Validate path segment
        assertEquals("123456", pathCaptor.getValue().get("id"));

        // Validate query param name
        assertEquals(List.of("type"), nameCaptor.getValue());

        // Validate query param value
        assertEquals(List.of("demo"), valueCaptor.getValue());
    }

    @Test
    public void testGetCachedIdentityData_delegatesToGetIdentityData() throws Exception {
        String id = "789";
        String accessToken = "TOKEN123"; // part of cache key, but unused in unit test
        Class<?> responseType = String.class;
        String expectedResponse = "CACHED_RESPONSE";

        // stub getIdentityData() internal call
        when(restClientWithSelfTokenRestTemplate.getApi(
                eq(ApiName.IDREPO_IDENTITY_URL),
                anyMap(),
                anyList(),
                anyList(),
                eq(responseType)
        )).thenReturn(expectedResponse);

        String result = cachedIdentityDataUtil.getCachedIdentityData(id, accessToken, responseType);

        assertEquals(expectedResponse, result);

        verify(restClientWithSelfTokenRestTemplate, times(1))
                .getApi(eq(ApiName.IDREPO_IDENTITY_URL),
                        anyMap(),
                        anyList(),
                        anyList(),
                        eq(responseType));
    }

    @Test(expected = ApisResourceAccessException.class)
    public void testGetIdentityData_whenApiThrows_shouldPropagate() throws Exception {
        when(restClientWithSelfTokenRestTemplate.getApi(
                any(),
                anyMap(),
                anyList(),
                anyList(),
                any()
        )).thenThrow(new ApisResourceAccessException("API FAIL"));

        cachedIdentityDataUtil.getIdentityData("111", String.class);
    }
}
