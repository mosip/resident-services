package io.mosip.resident.config;

/**
 * @author Neha Farheen
 *
 */
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.resident.builder.RestRequestBuilder;
import io.mosip.resident.constant.RestServicesConstants;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.interceptor.ResidentEntityInterceptor;
import io.mosip.resident.repository.OtpTransactionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.repository.ResidentUserRepository;
@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", basePackageClasses = {
		ResidentTransactionRepository.class,ResidentUserRepository.class,
		OtpTransactionRepository.class }, basePackages = "io.mosip.resident.repository.*", repositoryBaseClass = HibernateRepositoryImpl.class)
@EntityScan(basePackageClasses = { ResidentTransactionEntity.class })
public class ResidentDataSourceConfig extends HibernateDaoConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(ResidentDataSourceConfig.class);

	@Autowired
	private ResidentEntityInterceptor interceptor;
	
	@Autowired
	private Environment env;
	
	public static final String RESIDENT_DB_URL = "javax.persistence.jdbc.url";
	
	public static final String RESIDENT_DB_USERNAME = "javax.persistence.jdbc.user";
	
	public static final String RESIDENT_DB_PASSWORD = "javax.persistence.jdbc.password";
	
	public static final String RESIDENT_DB_DRIVER_CLASS_NAME = "javax.persistence.jdbc.driver";

	public Map<String, Object> jpaProperties() {
		Map<String, Object> jpaProperties = super.jpaProperties();
		jpaProperties.put("hibernate.ejb.interceptor",interceptor);
		return jpaProperties;
	}

	@Bean
	public JpaTransactionManager transactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
		return transactionManager;
	}
	/**
	 * Builds the data source.
	 *
	 * @return the data source
	 */
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource(env.getProperty(RESIDENT_DB_URL));
		dataSource.setUsername(env.getProperty(RESIDENT_DB_USERNAME));
		dataSource.setPassword(env.getProperty(RESIDENT_DB_PASSWORD));
		dataSource.setDriverClassName(env.getProperty(RESIDENT_DB_DRIVER_CLASS_NAME));
		dataSource.setSchema("resident");
		return dataSource;
	}

	@Bean
	public AfterburnerModule afterburnerModule() {
		return new AfterburnerModule();
	}
	
	@Bean
	public RestRequestBuilder getRestRequestBuilder() {
		return new RestRequestBuilder(Arrays.stream(RestServicesConstants.values())
				.map(RestServicesConstants::getServiceName).collect(Collectors.toList()));
	}

}
