package io.mosip.resident.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/*
 * Test class for WebsubCallbackRequestDecoratorFilter.
 * @author Kamesh Shekhar Prasad
 */

public class WebsubCallbackRequestDecoratorFilterTest {

    private WebsubCallbackRequestDecoratorFilter filter;
    private FilterChain chain;
    private ServletResponse response;

    @Before
    public void setup() {
        filter = new WebsubCallbackRequestDecoratorFilter();
        chain = mock(FilterChain.class);
        response = mock(ServletResponse.class);
    }

    @Test
    public void doFilterWhenRequestIsAlreadyRepeatablePassesSameInstance() throws IOException, ServletException {
        // Arrange: create a mocked RepeatableStreamHttpServletRequest
        RepeatableStreamHttpServletRequest repeatableRequest = mock(RepeatableStreamHttpServletRequest.class);

        // Act
        filter.doFilter(repeatableRequest, response, chain);

        // Assert: chain receives the same instance
        verify(chain, times(1)).doFilter(eq(repeatableRequest), eq(response));
    }

    @Test
    public void doFilterWhenRequestIsHttpServletRequestWrapsAndPassesRepeatableRequest() throws IOException, ServletException {
        // Arrange
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);

        // Act
        filter.doFilter(httpRequest, response, chain);

        // Capture argument passed to chain
        ArgumentCaptor<ServletRequest> captor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(chain, times(1)).doFilter(captor.capture(), eq(response));

        ServletRequest passed = captor.getValue();

        // Assert that passed request is a RepeatableStreamHttpServletRequest and not the original
        assertNotNull(passed);
        assertTrue("Expected a RepeatableStreamHttpServletRequest", passed instanceof RepeatableStreamHttpServletRequest);
        assertNotSame("Wrapped request should not be the same as the original HttpServletRequest", httpRequest, passed);
    }

    @Test
    public void doFilterWhenRequestIsOtherServletRequestPassesThroughUnchanged() throws IOException, ServletException {
        // Arrange: generic ServletRequest implementation (mock)
        ServletRequest genericRequest = mock(ServletRequest.class);

        // Act
        filter.doFilter(genericRequest, response, chain);

        // Assert: chain receives the same instance
        verify(chain, times(1)).doFilter(eq(genericRequest), eq(response));
    }
}
