package io.mosip.resident.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.service.validator.ValidateTokenHelper;

@SpringBootApplication(scanBasePackages = {"io.mosip.resident.*"})
@Import({TokenIDGenerator.class, ValidateTokenHelper.class})
public class ResidentTestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentTestBootApplication.class, args);
	}

}
