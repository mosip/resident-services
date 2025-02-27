package io.mosip.resident;

import io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig;
import io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;
import io.mosip.kernel.keymanager.hsm.impl.KeyStoreImpl;
import io.mosip.kernel.pdfgenerator.impl.PDFGeneratorImpl;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.kernel.transliteration.icu4j.impl.TransliterationImpl;
import io.mosip.kernel.websub.api.config.WebSubClientConfig;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import io.mosip.idrepository.core.util.TokenIDGenerator;
import io.mosip.kernel.authcodeflowproxy.api.validator.ValidateTokenUtil;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.preregistration.application.service.TransliterationService;
import io.mosip.preregistration.application.service.util.TransliterationServiceUtil;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = { "io.mosip.resident.*",
		"io.mosip.kernel.core.*",
		"io.mosip.kernel.crypto.jce.*",
		"io.mosip.commons.packet.*",
		"io.mosip.kernel.keygenerator.bouncycastle.*",
		"${mosip.auth.adapter.impl.basepackage}",
		"io.mosip.kernel.virusscanner.*",
		"io.mosip.commons.khazana.*",
		"io.mosip.idrepository.core.util.*",
		"io.mosip.kernel.authcodeflowproxy.*",
		"io.mosip.kernel.websub.api.config"}
)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@Import({TokenIDGenerator.class, ValidateTokenUtil.class, CbeffImpl.class, TransliterationService.class, TransliterationServiceUtil.class
, PDFGeneratorImpl.class, UinValidatorImpl.class, VidValidatorImpl.class, RidValidatorImpl.class, TemplateManagerBuilderImpl.class,
		RestTemplateHelper.class, TransliterationImpl.class, IdObjectValidatorConfig.class
		, RestTemplate.class, KeyStoreImpl.class, WebSubPublisherClientConfig.class,
		WebSubClientConfig.class,})
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
