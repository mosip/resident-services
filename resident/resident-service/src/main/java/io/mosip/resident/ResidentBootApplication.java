package io.mosip.resident;

import io.mosip.kernel.idobjectvalidator.config.IdObjectValidatorConfig;
import io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;
import io.mosip.kernel.pdfgenerator.itext.impl.PDFGeneratorImpl;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import io.mosip.kernel.transliteration.icu4j.impl.TransliterationImpl;
import io.mosip.kernel.websub.api.client.PublisherClientImpl;
import io.mosip.kernel.websub.api.client.SubscriberClientImpl;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
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
		"io.mosip.kernel.authcodeflowproxy.*"}
)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@Import({TokenIDGenerator.class, ValidateTokenUtil.class, CbeffImpl.class, TransliterationService.class, TransliterationServiceUtil.class
, PDFGeneratorImpl.class, UinValidatorImpl.class, VidValidatorImpl.class, RidValidatorImpl.class, TemplateManagerBuilderImpl.class,
		SubscriberClientImpl.class, RestTemplateHelper.class, TransliterationImpl.class, PublisherClientImpl.class, IdObjectValidatorConfig.class
, RestTemplate.class})
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
