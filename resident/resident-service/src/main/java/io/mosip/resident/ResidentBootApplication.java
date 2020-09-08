package io.mosip.resident;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication(scanBasePackages = { "io.mosip.resident.*", "io.mosip.kernel.*"})
/*@Import({PacketWriter.class, PacketManagerHelper.class, PacketWriterImpl.class,
		PacketKeeper.class, S3Adapter.class, PosixAdapter.class, SwiftAdapter.class,
		OnlinePacketCryptoServiceImpl.class, OfflinePacketCryptoServiceImpl.class})*/
public class ResidentBootApplication {

	@Value("${objectstore.adapter.name}")
	private String osAdapterName;

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

	@PostConstruct
	public void test() {
		System.out.println("Name ############################## - " +osAdapterName);
	}

}
