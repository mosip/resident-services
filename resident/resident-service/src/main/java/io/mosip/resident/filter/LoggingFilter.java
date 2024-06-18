package io.mosip.resident.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;

/**
 * Logging filter - used to log the request for debugging purpose
 * 
 * @author Loganathan S
 *
 */
@Component
@ConditionalOnProperty(value = "resident.logging.filter.enabled", havingValue = "true", matchIfMissing = false)
public class LoggingFilter implements Filter {

	private static final Logger logger = LoggerConfiguration.logConfig(LoggingFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Beginning to process request at: " + DateUtils.getUTCCurrentDateTime());
		ServletRequest requestRef;
		if (!(request instanceof RepeatableStreamHttpServletRequest) && request instanceof HttpServletRequest) {
			//Since it is already RepeatableStreamHttpServletRequest, we can use the same
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			RepeatableStreamHttpServletRequest reusableRequest = new RepeatableStreamHttpServletRequest(
					httpServletRequest);
			logger.debug("URI: " + httpServletRequest.getRequestURI());
			requestRef = reusableRequest;
			printHeaders(reusableRequest);
			printBody(reusableRequest);
		} else {
			requestRef = request;
		}
		chain.doFilter(requestRef, response);
		logger.debug("Request processed at: " + DateUtils.getUTCCurrentDateTime());
	}

	private void printBody(ServletRequest request) throws IOException {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			String body = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			if(!body.isEmpty()) {
				logger.debug(String.format("Body: %s", body));
			}
		}
	}

	private void printHeaders(ServletRequest request) {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			Iterator<String> headerNames = httpServletRequest.getHeaderNames().asIterator();
			StringBuffer headers = new StringBuffer();
			headerNames.forEachRemaining(header -> headers.append(String.format("%s -> %s; ", header, httpServletRequest.getHeader(header))));
			logger.debug(String.format("Headers: %s", headers));
			String headersTxt = headers.toString();
			if(!headersTxt.isEmpty()) {
				logger.debug(String.format("Headers: %s", headersTxt));
			}
		}
	}

	@Override
	public void destroy() {
	}

}
