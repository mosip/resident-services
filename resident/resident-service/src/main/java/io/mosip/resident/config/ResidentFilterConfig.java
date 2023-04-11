package io.mosip.resident.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.mosip.resident.filter.LoggingFilter;
import io.mosip.resident.filter.WebsubCallbackRequestDecoratorFilter;

/**
 * The configuration for adding filters.
 *
 * @author Loganathan S
 */

@Configuration
public class ResidentFilterConfig {

	/**
	 * Gets the auth filter.
	 *
	 * @return the auth filter
	 */
	@Bean
	@ConditionalOnProperty(value = "resident.logging.filter.enabled", havingValue = "true", matchIfMissing = false)
	public FilterRegistrationBean<LoggingFilter> getIdAuthFilter(LoggingFilter loggingFilter) {
		FilterRegistrationBean<LoggingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(loggingFilter);
		registrationBean.addUrlPatterns("/*");
		return registrationBean;
	}
	
	@Bean
	@ConditionalOnProperty(value = "resident.websub.request.decorator.filter.enabled", havingValue = "true", matchIfMissing = true)
	public FilterRegistrationBean<WebsubCallbackRequestDecoratorFilter> getWebsubCallbackRequestDecoratorFilter(
			@Autowired(required = false) WebsubCallbackRequestDecoratorFilter loggingFilter) {
		FilterRegistrationBean<WebsubCallbackRequestDecoratorFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(loggingFilter);
		registrationBean.addUrlPatterns("/callback/*");
		return registrationBean;
	}

}
