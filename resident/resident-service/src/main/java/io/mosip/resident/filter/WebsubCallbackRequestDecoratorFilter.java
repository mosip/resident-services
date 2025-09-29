package io.mosip.resident.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Servlet filter creating a repeatable servlet request wrapper to allow re-read
 * of the request by the {@code WebSubClientAspect}. This was added because
 * of the issue with the aspect was not able to get the request body for the signature
 * verification as the request is already read by the spring framework message
 * converter ({@code AbstractMessageConverterMethodArgumentResolver}.
 * 
 * @author Loganathan S
 *
 */
@Component
@ConditionalOnProperty(value = "resident.websub.request.decorator.filter.enabled", havingValue = "true", matchIfMissing = true)
public class WebsubCallbackRequestDecoratorFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof RepeatableStreamHttpServletRequest) {
			//Since it is already RepeatableStreamHttpServletRequest, we can use the same
			chain.doFilter(request, response);
		} else if (request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			RepeatableStreamHttpServletRequest reusableRequest = new RepeatableStreamHttpServletRequest(
					httpServletRequest);
			chain.doFilter(reusableRequest, response);
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

}
