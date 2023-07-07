package io.mosip.resident.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.servlet.Filter;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;
import io.mosip.resident.util.ResidentServiceRestClient;
import io.mosip.resident.util.Utility;


@Configuration
@EnableScheduling
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
		return new ResidentServiceRestClient(residentRestTemplate);
	}
	
	@Bean("restClientWithPlainRestTemplate")
	public ResidentServiceRestClient plainRestClient(@Qualifier("restTemplate")RestTemplate restTemplate) {
		return new ResidentServiceRestClient(restTemplate);
	}

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}
	
	

}
