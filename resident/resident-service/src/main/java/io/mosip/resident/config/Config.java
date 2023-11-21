package io.mosip.resident.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;
import io.mosip.resident.constant.ResidentConstants;
import io.mosip.resident.interceptor.RestTemplateLoggingInterceptor;
import io.mosip.resident.interceptor.RestTemplateMetricsInterceptor;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;


@Configuration
@EnableScheduling
@EnableAsync
public class Config {
	private String defaultEncoding = StandardCharsets.UTF_8.name();
	/** The resource loader. */
	private String resourceLoader = "classpath";

	/** The template path. */
	private String templatePath = ".";

	/** The cache. */
	private boolean cache = Boolean.TRUE;

	@Value("${resident-data-format-mvel-file-source}")
	private Resource mvelFile;

	@Value("${" + ResidentConstants.RESIDENT_REST_TEMPLATE_LOGGING_INTERCEPTOR_FILTER_ENABLED + ":false}")
	private boolean isResidentLoggingInterceptorFilterEnabled;
	
	@Value("${" + ResidentConstants.RESIDENT_REST_TEMPLATE_METRICS_INTERCEPTOR_FILTER_ENABLED + ":false}")
	private boolean isResidentMetricsInterceptorFilterEnabled;
	

	@Autowired(required = false)
	private RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;
	
	@Autowired(required = false)
	private RestTemplateMetricsInterceptor restTemplateMetricsInterceptor;

	@Autowired
	private Environment env;

	@Bean("varres")
	public VariableResolverFactory getVariableResolverFactory() {
		String mvelExpression = Utility.readResourceContent(mvelFile);
		VariableResolverFactory functionFactory = new MapVariableResolverFactory();
		MVEL.eval(mvelExpression, functionFactory);
		return functionFactory;
	}

	@Bean
	public FilterRegistrationBean<Filter> registerReqResFilter() {
		FilterRegistrationBean<Filter> corsBean = new FilterRegistrationBean<>();
		corsBean.setFilter(getReqResFilter());
		corsBean.setOrder(1);
		return corsBean;
	}

	@Bean
	public Filter getReqResFilter() {
		return new ReqResFilter();
	}

	@Bean
	public KeyGenerator keyGenerator() {
		return new KeyGenerator();
	}

	@Bean
	public TemplateManager getTemplateManager() {
		final Properties properties = new Properties();
		properties.put(RuntimeConstants.INPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.OUTPUT_ENCODING, defaultEncoding);
		properties.put(RuntimeConstants.ENCODING_DEFAULT, defaultEncoding);
		properties.put(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, templatePath);
		properties.put(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, cache);
		properties.put(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
		properties.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		properties.put("file.resource.loader.class", FileResourceLoader.class.getName());
		VelocityEngine engine = new VelocityEngine(properties);
		engine.init();
		return new TemplateManagerImpl(engine);
	}

	@Bean
	public AfterburnerModule afterburnerModule() {
		return new AfterburnerModule();
	}

	@Bean("restClientWithSelfTOkenRestTemplate")
	@Primary
	public ResidentServiceRestClient selfTokenRestClient(@Qualifier("selfTokenRestTemplate")RestTemplate residentRestTemplate) {
		addInterceptors(residentRestTemplate);
		return new ResidentServiceRestClient(residentRestTemplate);
	}

	private void addLoggingInterceptor(RestTemplate restTemplate) {
		if(isResidentLoggingInterceptorFilterEnabled) {
			List<ClientHttpRequestInterceptor> interceptors
					= restTemplate.getInterceptors();
			if (CollectionUtils.isEmpty(interceptors)) {
				interceptors = new ArrayList<>();
			}
			interceptors.add(restTemplateLoggingInterceptor);
			restTemplate.setInterceptors(interceptors);
		}
	}
	
	private void addMetricsInterceptor(RestTemplate restTemplate) {
		if(isResidentMetricsInterceptorFilterEnabled) {
			List<ClientHttpRequestInterceptor> interceptors
					= restTemplate.getInterceptors();
			if (CollectionUtils.isEmpty(interceptors)) {
				interceptors = new ArrayList<>();
			}
			interceptors.add(restTemplateMetricsInterceptor);
			restTemplate.setInterceptors(interceptors);
		}
	}

	@Bean("restClientWithPlainRestTemplate")
	public ResidentServiceRestClient plainRestClient(@Qualifier("restTemplate")RestTemplate restTemplate) {
		addInterceptors(restTemplate);
		return new ResidentServiceRestClient(restTemplate);
	}

	private void addInterceptors(RestTemplate restTemplate) {
		addLoggingInterceptor(restTemplate);
		addMetricsInterceptor(restTemplate);
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}

	@Bean
	@Qualifier("AuditExecutor")
	public TaskExecutor AuditExecutor() {
	    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
	    executor.setCorePoolSize(Math.floorDiv(env.getProperty("mosip.resident.async-core-pool-size", Integer.class, 100), 4));
	    executor.setMaxPoolSize(env.getProperty("mosip.resident.async-max-pool-size", Integer.class, 100));
	    executor.setThreadNamePrefix("Async-audit");
	    executor.setWaitForTasksToCompleteOnShutdown(true);
	    executor.initialize();
	    return new DelegatingSecurityContextAsyncTaskExecutor(executor);
	}

}
