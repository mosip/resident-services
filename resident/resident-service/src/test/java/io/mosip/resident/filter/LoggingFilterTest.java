package io.mosip.resident.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import static org.mockito.Mockito.*;

/*
 * Test class for LoggingFilter
 * @author Kamesh Shekhar Prasad
 */

public class LoggingFilterTest {

    private LoggingFilter loggingFilter;
    private HttpServletRequest httpServletRequest;
    private FilterChain filterChain;
    private ServletResponse response;

    @Before
    public void setup() {
        loggingFilter = new LoggingFilter();
        httpServletRequest = mock(HttpServletRequest.class);
        filterChain = mock(FilterChain.class);
        response = mock(ServletResponse.class);
    }

    @Test
    public void doFilterWrapsHttpServletRequestAndLogsHeadersAndBody() throws IOException, ServletException {
        // Mock headers
        when(httpServletRequest.getHeaderNames())
                .thenReturn(Collections.enumeration(Collections.singleton("Content-Type")));
        when(httpServletRequest.getHeader("Content-Type")).thenReturn("application/json");

        // Mock URI
        when(httpServletRequest.getRequestURI()).thenReturn("/test/api");

        // Mock body reader
        when(httpServletRequest.getReader()).thenReturn(new BufferedReader(new StringReader("{\"name\":\"John\"}")));

        // Execute filter
        loggingFilter.doFilter(httpServletRequest, response, filterChain);

        // Capture what was passed to filterChain.doFilter
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        // Assert wrapped request was passed
        ServletRequest wrappedRequest = requestCaptor.getValue();
        assert (wrappedRequest instanceof RepeatableStreamHttpServletRequest);
    }

    @Test
    public void doFilterDoesNotWrapIfAlreadyRepeatableRequest() throws IOException, ServletException {
        // Mock an already wrapped request
        RepeatableStreamHttpServletRequest repeatableRequest = mock(RepeatableStreamHttpServletRequest.class);

        loggingFilter.doFilter(repeatableRequest, response, filterChain);

        // Should pass same request to chain
        verify(filterChain).doFilter(eq(repeatableRequest), eq(response));

        // Should not call header/body reading methods
        verify(repeatableRequest, never()).getReader();
        verify(repeatableRequest, never()).getHeaderNames();
    }

    @Test
    public void doFilterHandlesEmptyBodyGracefully() throws IOException, ServletException {
        when(httpServletRequest.getHeaderNames())
                .thenReturn(Collections.enumeration(Collections.emptyList()));
        when(httpServletRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

        loggingFilter.doFilter(httpServletRequest, response, filterChain);

        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));

        assert (captor.getValue() instanceof RepeatableStreamHttpServletRequest);
    }
}
