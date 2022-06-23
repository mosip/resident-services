package io.mosip.resident.mock.service.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.mock.service.MockService;
import io.mosip.resident.service.impl.ResidentCredentialServiceImpl;
import io.mosip.resident.util.AuditUtil;
import io.mosip.resident.util.ResidentServiceRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Service
public class MockServiceImpl implements MockService {

    @Value("${mosip.resident.service.pdf.url}")
    private String residentServicePdfUrl;

    @Autowired
    private AuditUtil audit;

    private static final Logger logger = LoggerConfiguration.logConfig(ResidentCredentialServiceImpl.class);

    @Autowired
    private ResidentServiceRestClient residentServiceRestClient;

    @Override
    public byte[] getRIDDigitalCardV2(String rid) throws ApisResourceAccessException, IOException {
        return getPdfFromUrl(residentServicePdfUrl);
    }

    private byte[] getPdfFromUrl(String url) throws ApisResourceAccessException, IOException {
        URL pdfUrl = new URL(url);
        byte[] pdf = getAsByteArray(pdfUrl);
        return pdf;
    }

    public  byte[] getAsByteArray(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        int contentLength = connection.getContentLength();
        ByteArrayOutputStream tmpOut;
        if (contentLength != -1) {
            tmpOut = new ByteArrayOutputStream(contentLength);
        } else {
            tmpOut = new ByteArrayOutputStream(16384);
        }

        byte[] buf = new byte[512];
        while (true) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }
            tmpOut.write(buf, 0, len);
        }
        in.close();
        tmpOut.close();

        byte[] array = tmpOut.toByteArray();

        return array;
    }
}
