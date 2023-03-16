package io.mosip.resident.test.util;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import io.mosip.resident.util.ServerUtil;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class ServerUtilTest {

	@Test
	public void serverUtilTest() throws UnknownHostException {
		ServerUtil util = ServerUtil.getServerUtilInstance();
		String serverIp = InetAddress.getLocalHost().getHostAddress();
		assertTrue(serverIp.equals(util.getServerIp()));
		String serverName = InetAddress.getLocalHost().getHostName();
		assertTrue(serverName.equals(util.getServerName()));	}
}
