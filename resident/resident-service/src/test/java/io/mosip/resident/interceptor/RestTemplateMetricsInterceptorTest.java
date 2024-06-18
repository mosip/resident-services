package io.mosip.resident.interceptor;


import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.URI;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kamesh Shekhar Prasad
 */


@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
@TestPropertySource(locations="classpath:application.properties")
@Import(RestTemplateMetricsInterceptorTest.AdditionalConfig.class)
public class RestTemplateMetricsInterceptorTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Mock
    private Timer timer;

    @InjectMocks
    private RestTemplateMetricsInterceptor interceptor;

    @Before
    public void init() {
        ReflectionTestUtils.setField(interceptor, "registry", meterRegistry);
    }

    @Test
    public void intercept_SuccessfulRequest_RecordTimer() throws IOException {


        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(response);

        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("https://example.com"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);

        Assert.assertNotNull(interceptor.intercept(request, new byte[]{}, execution));

    }

    @Test(expected = IOException.class)
    public void intercept_FailedRequest_RecordTimerWithError() throws IOException {
        // Arrange
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        when(execution.execute(any(), any())).thenThrow(new IOException("Simulated error"));

        HttpRequest request = mock(HttpRequest.class);
        when(request.getURI()).thenReturn(URI.create("https://example.com"));
        when(request.getMethod()).thenReturn(HttpMethod.GET);

        interceptor.intercept(request, new byte[]{}, execution);
    }

    @TestConfiguration
     static class AdditionalConfig {
        @Bean
        public MeterRegistry registry() {
            return new SimpleMeterRegistry();
        }
    }
}

