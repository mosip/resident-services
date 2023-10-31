package io.mosip.resident.config;

/**
 * @author Neha Farheen
 *
 */
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.kernel.dataaccess.hibernate.repository.impl.HibernateRepositoryImpl;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.interceptor.ResidentEntityInterceptor;
import io.mosip.resident.repository.OtpTransactionRepository;
import io.mosip.resident.repository.ResidentTransactionRepository;
import io.mosip.resident.repository.ResidentUserRepository;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "entityManagerFactory", basePackageClasses = {
		ResidentTransactionRepository.class, ResidentUserRepository.class,
		OtpTransactionRepository.class }, basePackages = "io.mosip.resident.repository.*", repositoryBaseClass = HibernateRepositoryImpl.class)
@EntityScan(basePackageClasses = { ResidentTransactionEntity.class })
public class ResidentDataSourceConfig extends HibernateDaoConfig {

	@Autowired
	private ResidentEntityInterceptor interceptor;

	public Map<String, Object> jpaProperties() {
		Map<String, Object> jpaProperties = super.jpaProperties();
		jpaProperties.put("hibernate.ejb.interceptor", interceptor);
		return jpaProperties;
	}
}
