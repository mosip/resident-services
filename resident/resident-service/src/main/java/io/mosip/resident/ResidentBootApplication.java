package io.mosip.resident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "io.mosip.resident.*", "io.mosip.kernel.core.*",
		"io.mosip.kernel.crypto.jce.*", "io.mosip.commons.packet.*", "io.mosip.kernel.keygenerator.bouncycastle.*"})
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
