package io.mosip.resident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.dataaccess.hibernate.config.HibernateDaoConfig;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;

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
		"io.mosip.idrepository.core.util.*"},exclude = HibernateDaoConfig.class)
@Import({TokenIDGenerator.class, ValidateTokenUtil.class, CbeffImpl.class, TransliterationService.class, TransliterationServiceUtil.class})
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
