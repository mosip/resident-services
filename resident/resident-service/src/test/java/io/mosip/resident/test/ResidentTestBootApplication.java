package io.mosip.resident.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;

@SpringBootApplication(scanBasePackages = {"io.mosip.resident.*"},
		exclude={DataSourceAutoConfiguration.class})
@Import({TokenIDGenerator.class, ValidateTokenUtil.class})
public class ResidentTestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentTestBootApplication.class, args);
	}

}
