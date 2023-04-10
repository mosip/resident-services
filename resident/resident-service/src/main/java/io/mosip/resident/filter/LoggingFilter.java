package io.mosip.resident.filter;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.config.LoggerConfiguration;

/**
 * Servlet filter for logging purpose
 * 
 * @author Loganathan S
 *
 */
@Component
public class LoggingFilter implements Filter{
	
	private static final Logger logger = LoggerConfiguration.logConfig(LoggingFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("Beginning to process request at: " + DateUtils.getUTCCurrentDateTime());
		if (request instanceof HttpServletRequest) {
			ResettableStreamHttpServletRequest reusableRequest = new ResettableStreamHttpServletRequest(
					(HttpServletRequest) request);
			printHeaders(reusableRequest);
			printBody(reusableRequest);
			reusableRequest.resetInputStream();
			chain.doFilter(reusableRequest, response);
		} else {
			chain.doFilter(request, response);
		}
		logger.debug("Request processed at: " + DateUtils.getUTCCurrentDateTime());
	}

	private void printBody(ServletRequest request) throws IOException {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			String body = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			logger.debug(String.format("Body:\n%s", body));
		}
	}

	private void printHeaders(ServletRequest request) {
		if(request instanceof HttpServletRequest) {
			HttpServletRequest httpServletRequest = (HttpServletRequest) request;
			Iterator<String> headerNames = httpServletRequest.getHeaderNames().asIterator();
			StringBuffer headers = new StringBuffer();
			headerNames.forEachRemaining(header -> headers.append(String.format("%s -> %s; ", header, httpServletRequest.getHeader(header))));
			logger.debug(String.format("Headers: %s", headers));
		}
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
