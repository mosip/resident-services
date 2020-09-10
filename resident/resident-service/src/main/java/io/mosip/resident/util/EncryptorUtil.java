package io.mosip.resident.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.packet.CryptomanagerRequestDto;
import io.mosip.commons.packet.dto.packet.CryptomanagerResponseDto;
import io.mosip.commons.packet.dto.packet.DecryptResponseDto;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.security.exception.MosipInvalidDataException;
import io.mosip.kernel.core.security.exception.MosipInvalidKeyException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.ApiName;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.dto.LogDescription;
import io.mosip.resident.dto.PublicKeyResponseDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResponseWrapper;
import io.mosip.resident.exception.ApisResourceAccessException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class EncryptorUtil.
 */
@Component
public class EncryptorUtil {

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TokenGenerator tokenGenerator;

    @Value("${mosip.kernel.cryptomanager.request_version:v1}")
    private String APPLICATION_VERSION;

    @Value("${CRYPTOMANAGER_ENCRYPT:null}")
    private String cryptomanagerEncryptUrl;

    @Autowired
    private ResidentServiceRestClient restClientService;

    private static final String IO_EXCEPTION = "Exception while reading packet inputStream";
    private static final String DATE_TIME_EXCEPTION = "Error while parsing packet timestamp";
    public static final String APPLICATION_ID = "REGISTRATION";
    private static final String DECRYPT_SERVICE_ID = "mosip.cryptomanager.decrypt";

    public String encrypt(byte[] data, String refId) {
        try {
            String packetString = CryptoUtil.encodeBase64String(data);
            CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
            io.mosip.kernel.core.http.RequestWrapper<CryptomanagerRequestDto> request = new io.mosip.kernel.core.http.RequestWrapper<>();
            cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
            cryptomanagerRequestDto.setData(packetString);
            cryptomanagerRequestDto.setReferenceId(refId);
            cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());

            request.setId(DECRYPT_SERVICE_ID);
            request.setMetadata(null);
            request.setRequest(cryptomanagerRequestDto);
            request.setRequesttime(DateUtils.getUTCCurrentDateTime());
            request.setVersion(APPLICATION_VERSION);

            ResponseWrapper responseDto = restClientService
                    .postApi(env.getProperty(ApiName.ENCRYPTURL.name()), MediaType.APPLICATION_JSON, request, ResponseWrapper.class, tokenGenerator.getRegprocToken());

            if (responseDto != null && !CollectionUtils.isEmpty(responseDto.getErrors())) {
                ServiceError error = (ServiceError) responseDto.getErrors().get(0);
                throw new PacketDecryptionFailureException(error.getMessage());
            }

            DecryptResponseDto responseObject = mapper.readValue(mapper.writeValueAsString(responseDto.getResponse()), DecryptResponseDto.class);
            return responseObject.getData();

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

}
