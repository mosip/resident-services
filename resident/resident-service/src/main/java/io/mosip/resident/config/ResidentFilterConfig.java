package io.mosip.resident.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.resident.filter.LoggingFilter;

/**
 * The configuration for adding filters.
 *
 * @author Manoj SP
 */

@Configuration
public class ResidentFilterConfig {

	/**
	 * Gets the auth filter.
	 *
	 * @return the auth filter
	 */
	@Bean
	public FilterRegistrationBean<LoggingFilter> getIdAuthFilter(LoggingFilter loggingFilter) {
		FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(loggingFilter);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}

}
