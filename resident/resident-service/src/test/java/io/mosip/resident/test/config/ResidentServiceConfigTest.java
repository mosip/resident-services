package io.mosip.resident.test.config;

import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.kernel.idvalidator.rid.impl.RidValidatorImpl;
import io.mosip.kernel.idvalidator.uin.impl.UinValidatorImpl;
import io.mosip.kernel.idvalidator.vid.impl.VidValidatorImpl;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Configuration
@ComponentScan(basePackages = { "io.mosip.resident.*" })
//@ContextConfiguration(classes = {PacketWriter.class})
public class ResidentServiceConfigTest {

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CbeffImpl cbeff;

	@MockBean
	private PacketWriter packetWriter;

	@Bean
	@Primary
	public VidValidator<String> vidValidator1() {
		return new VidValidatorImpl();
	}

	@Bean
	@Primary
	public UinValidator<String> uinValidator1() {
		return new UinValidatorImpl();
	}

	@Bean
	@Primary
	public RidValidator<String> ridValidator1() {
		return new RidValidatorImpl();
	}

}
