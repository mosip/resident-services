package io.mosip.resident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;

@SpringBootApplication(scanBasePackages = { 
		"io.mosip.resident.*", 
		"io.mosip.kernel.core.*", 
		"io.mosip.kernel.dataaccess.hibernate.*",
		"io.mosip.kernel.crypto.jce.*", 
		"io.mosip.commons.packet.*", 
		"io.mosip.kernel.keygenerator.bouncycastle.*",
		"${mosip.auth.adapter.impl.basepackage}", 
		"io.mosip.kernel.virusscanner.*", 
		"io.mosip.commons.khazana.*",
		"io.mosip.idrepository.core.util.*"})
@Import({TokenIDGenerator.class,CbeffImpl.class})
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
