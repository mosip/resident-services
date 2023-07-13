package io.mosip.resident.util;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.constants.CryptomanagerConstant;
import io.mosip.commons.packet.dto.packet.DecryptResponseDto;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.dto.CryptomanagerRequestDto;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.PacketEncryptionFailureException;

/**
 * The Class EncryptorUtil.
 */
@Component
public class EncryptorUtil {

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @Value("${mosip.kernel.cryptomanager.request_version:v1}")
    private String APPLICATION_VERSION;

	@Value("${crypto.PrependThumbprint.enable:true}")
	private boolean isPrependThumbprintEnabled;

    @Value("${CRYPTOMANAGER_ENCRYPT:null}")
    private String cryptomanagerEncryptUrl;

    @Autowired
    private ResidentServiceRestClient restClientService;

    @Autowired
    private Utilities utilities;

    private static final String IO_EXCEPTION = "Exception while reading packet inputStream";
    private static final String DATE_TIME_EXCEPTION = "Error while parsing packet timestamp";
    public static final String APPLICATION_ID = "REGISTRATION";
    private static final String DECRYPT_SERVICE_ID = "mosip.cryptomanager.decrypt";

    public String encrypt(byte[] data, String refId) {
        try {
            String packetString = CryptoUtil.encodeToPlainBase64(data);
            CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
			io.mosip.kernel.core.http.RequestWrapper<CryptomanagerRequestDto> request = new io.mosip.kernel.core.http.RequestWrapper<>();
            cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
            cryptomanagerRequestDto.setData(packetString);
            cryptomanagerRequestDto.setReferenceId(refId);
            SecureRandom sRandom = utilities.getSecureRandom();
            byte[] nonce = new byte[CryptomanagerConstant.GCM_NONCE_LENGTH];
            byte[] aad = new byte[CryptomanagerConstant.GCM_AAD_LENGTH];
            sRandom.nextBytes(nonce);
            sRandom.nextBytes(aad);
            cryptomanagerRequestDto.setAad(CryptoUtil.encodeToPlainBase64(aad));
            cryptomanagerRequestDto.setSalt(CryptoUtil.encodeToPlainBase64(nonce));
            cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());
			cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
            request.setId(DECRYPT_SERVICE_ID);
            request.setMetadata(null);
            request.setRequest(cryptomanagerRequestDto);
            request.setRequesttime(DateUtils.getUTCCurrentDateTime());
            request.setVersion(APPLICATION_VERSION);

            ResponseWrapper responseDto = restClientService
                    .postApi(env.getProperty(ApiName.ENCRYPTURL.name()), MediaType.APPLICATION_JSON, request, ResponseWrapper.class);
            if (responseDto != null && !CollectionUtils.isEmpty(responseDto.getErrors())) {
                ServiceError error = (ServiceError) responseDto.getErrors().get(0);
                throw new PacketEncryptionFailureException(error.getMessage());
            }
            if(responseDto != null && responseDto.getResponse() != null) {
                DecryptResponseDto responseObject = mapper.readValue(mapper.writeValueAsString(responseDto.getResponse()), DecryptResponseDto.class);
                return CryptoUtil.encodeToURLSafeBase64(mergeEncryptedData(CryptoUtil.decodeURLSafeBase64(responseObject.getData()), nonce, aad));
            }
            throw new PacketEncryptionFailureException("Packet encryption failed");
        } catch (IOException e) {
            throw new PacketDecryptionFailureException(IO_EXCEPTION, e);
        } catch (DateTimeParseException e) {
            throw new PacketDecryptionFailureException(DATE_TIME_EXCEPTION);
        } catch (Exception e) {
            if (e.getCause() instanceof HttpClientErrorException) {
                HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
                throw new ApiNotAccessibleException(httpClientException.getResponseBodyAsString());
            } else if (e.getCause() instanceof HttpServerErrorException) {
                HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
                throw new ApiNotAccessibleException(httpServerException.getResponseBodyAsString());
            } else {
                throw new PacketDecryptionFailureException(e);
            }
        }
    }

    private static byte[] mergeEncryptedData(byte[] encryptedData, byte[] nonce, byte[] aad) {
        byte[] finalEncData = new byte[encryptedData.length + CryptomanagerConstant.GCM_AAD_LENGTH + CryptomanagerConstant.GCM_NONCE_LENGTH];
        System.arraycopy(nonce, 0, finalEncData, 0, nonce.length);
        System.arraycopy(aad, 0, finalEncData, nonce.length, aad.length);
        System.arraycopy(encryptedData, 0, finalEncData, nonce.length + aad.length,	encryptedData.length);
        return finalEncData;
    }

}
