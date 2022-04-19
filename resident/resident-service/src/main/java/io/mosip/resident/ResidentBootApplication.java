package io.mosip.resident;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.mosip.commons.khazana.impl.PosixAdapter;
import io.mosip.commons.khazana.impl.S3Adapter;
import io.mosip.commons.khazana.impl.SwiftAdapter;
import io.mosip.commons.khazana.util.EncryptionHelper;
import io.mosip.commons.khazana.util.OfflineEncryptionUtil;
import io.mosip.commons.khazana.util.OnlineCryptoUtil;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packet.impl.OfflinePacketCryptoServiceImpl;
import io.mosip.commons.packet.impl.OnlinePacketCryptoServiceImpl;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.util.PacketManagerHelper;
import io.mosip.preregistration.application.controller.LoginController;
import io.mosip.preregistration.application.service.LoginService;
import io.mosip.preregistration.application.service.OTPManager;
import io.mosip.preregistration.application.service.util.NotificationServiceUtil;
import io.mosip.preregistration.application.util.LoginCommonUtil;
import io.mosip.preregistration.core.util.AuditLogUtil;
import io.mosip.preregistration.core.util.RequestValidator;
import io.mosip.preregistration.core.util.ValidationUtil;

@SpringBootApplication(scanBasePackages = { "io.mosip.resident.*", "io.mosip.kernel.core.*",
		"io.mosip.kernel.dataaccess.hibernate.*", "io.mosip.kernel.crypto.jce.*",
		"io.mosip.kernel.keygenerator.bouncycastle.*", "${mosip.auth.adapter.impl.basepackage}" })
@Import({ LoginController.class, LoginService.class, LoginCommonUtil.class, ValidationUtil.class,
		RequestValidator.class, OTPManager.class, AuditLogUtil.class, NotificationServiceUtil.class, PacketWriter.class,
		PacketKeeper.class, SwiftAdapter.class, S3Adapter.class, PosixAdapter.class,
		EncryptionHelper.class, OfflineEncryptionUtil.class, OnlineCryptoUtil.class,
		OfflinePacketCryptoServiceImpl.class, OnlinePacketCryptoServiceImpl.class, PacketManagerHelper.class, String.class })
public class ResidentBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResidentBootApplication.class, args);
	}

}
