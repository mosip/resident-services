package io.mosip.resident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;

@SpringBootApplication(scanBasePackages = { "io.mosip.resident.*", "io.mosip.kernel.core.*",
		"io.mosip.kernel.crypto.jce.*", "io.mosip.commons.packet.*", "io.mosip.kernel.keygenerator.bouncycastle.*",
		"${mosip.auth.adapter.impl.basepackage}" }
		, exclude = HibernateDaoConfig.class
)

public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
