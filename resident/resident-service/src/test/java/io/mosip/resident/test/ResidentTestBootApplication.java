package io.mosip.resident.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;

@SpringBootApplication(scanBasePackages = {"io.mosip.resident.*"})
@Import({TokenIDGenerator.class})
public class ResidentTestBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentTestBootApplication.class, args);
	}

}
