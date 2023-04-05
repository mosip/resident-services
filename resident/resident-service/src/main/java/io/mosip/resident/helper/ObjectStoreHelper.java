package io.mosip.resident.helper;

import static io.mosip.resident.constant.ResidentConstants.CRYPTO_APPLICATION_NAME;
import static io.mosip.resident.constant.ResidentConstants.CRYPTO_DECRYPT_URI;
import static io.mosip.resident.constant.ResidentConstants.CRYPTO_ENCRYPT_URI;
import static io.mosip.resident.constant.ResidentConstants.CRYPTO_REFERENCE_ID;
import static io.mosip.resident.constant.ResidentConstants.OBJECT_STORE_ACCOUNT_NAME;
import static io.mosip.resident.constant.ResidentConstants.OBJECT_STORE_ADAPTER_NAME;
import static io.mosip.resident.constant.ResidentConstants.OBJECT_STORE_BUCKET_NAME;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import io.mosip.commons.khazana.dto.ObjectDto;
import io.mosip.commons.khazana.exception.ObjectStoreAdapterException;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.resident.config.LoggerConfiguration;
import io.mosip.resident.constant.LoggerFileConstant;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.exception.ApisResourceAccessException;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.util.JsonUtil;
import io.mosip.resident.util.ResidentServiceRestClient;

/**
 * It's a helper class that uses the ObjectStoreAdapter to put and get objects
 * from the object store
 * 
 * @author Manoj SP
 */
@Component
public class ObjectStoreHelper {

	private static final Logger logger = LoggerConfiguration.logConfig(ObjectStoreHelper.class);

	@Value("${" + OBJECT_STORE_ACCOUNT_NAME + "}")
	private String objectStoreAccountName;

	@Value("${" + OBJECT_STORE_BUCKET_NAME + "}")
	private String objectStoreBucketName;

	@Value("${" + OBJECT_STORE_ADAPTER_NAME + "}")
	private String objectStoreAdapterName;

	@Value("${" + CRYPTO_APPLICATION_NAME + "}")
	private String applicationId;

	@Value("${" + CRYPTO_REFERENCE_ID + "}")
	private String referenceId;

	@Value("${" + CRYPTO_ENCRYPT_URI + "}")
	private String encryptUri;

	@Value("${" + CRYPTO_DECRYPT_URI + "}")
	private String decryptUri;

	private ObjectStoreAdapter adapter;

	@Autowired
	public void setObjectStore(ApplicationContext context) {
		this.adapter = context.getBean(objectStoreAdapterName, ObjectStoreAdapter.class);
	}

	@Autowired
	private ResidentServiceRestClient restClient;

	/**
	 * This function is used to upload an object to the OSS bucket
	 * 
	 * @param objectName The name of the object to be uploaded.
	 * @param data       The data to upload.
	 */
	public void putObject(String objectName, InputStream data) {
		this.putObject(objectName, data, null);
	}

	/**
	 * The function takes an object name, an input stream, and a map of metadata. It
	 * then uploads the object to the object store and adds the metadata to the
	 * object
	 * 
	 * @param objectName The name of the object to be uploaded.
	 * @param data       The data to be uploaded.
	 * @param metadata   This is a map of key-value pairs that you want to store as
	 *                   metadata for the object.
	 */
	public void putObject(String objectName, InputStream data, Map<String, Object> metadata) {
		try {
			adapter.putObject(objectStoreAccountName, null, null, null, objectName, encryptData(data));
			if (Objects.nonNull(metadata))
				adapter.addObjectMetaData(objectStoreAccountName, null, null, null, objectName,
						metadata);
		} catch (ResidentServiceException | ObjectStoreAdapterException | IOException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_UPLOAD_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_UPLOAD_DOC.getErrorMessage(), e);
		}
	}

	/**
	 * This function returns an InputStream object that contains the contents of the
	 * object stored in the object store
	 * 
	 * @param objectName The name of the object to be retrieved.
	 * @return The object is being returned as an InputStream.
	 */
	public String getObject(String objectName) {
		try {
			return decryptData(
					adapter.getObject(objectStoreAccountName, null, null, null, objectName));
		} catch (ResidentServiceException | ObjectStoreAdapterException | IOException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorMessage(), e);
		}
	}

	/**
	 * This function gets all the objects in the specified bucket
	 * 
	 * @param objectName The name of the object you want to retrieve.
	 * @return A list of ObjectDto objects.
	 */
	public List<ObjectDto> getAllObjects(String objectName) {
		try {
			return adapter.getAllObjects(objectStoreAccountName, objectName);
		} catch (ObjectStoreAdapterException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorMessage(), e);
		}
	}

	/**
	 * This function gets the metadata of the object in the object store
	 * 
	 * @param objectName The name of the object to get metadata for.
	 * @return A map of metadata for the object.
	 */
	public Map<String, Object> getMetadata(String objectName) {
		try {
			return adapter.getMetaData(objectStoreAccountName, null, null, null, objectName);
		} catch (ObjectStoreAdapterException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_RETRIEVE_DOC.getErrorMessage(), e);
		}
	}

	/**
	 * It takes an input stream, converts it to a string, and then decrypts it
	 * 
	 * @param data The data to be encrypted or decrypted.
	 * @return The decrypted data.
	 */
	private String decryptData(InputStream data) throws IOException {
    return encryptDecryptData(IOUtils.toString(data, Charset.defaultCharset()), false, applicationId, referenceId);

	}

	/**
	 * It takes an input stream, converts it to a byte array, encrypts the byte
	 * array, converts the
	 * encrypted byte array to a string, and then converts the string to an input
	 * stream
	 * 
	 * @param data The data to be encrypted.
	 * @return A ByteArrayInputStream
	 */
	private InputStream encryptData(InputStream data) throws IOException {
		return new ByteArrayInputStream(

				(encryptDecryptData(CryptoUtil.encodeToURLSafeBase64(IOUtils.toByteArray(data)), true, applicationId, referenceId).getBytes()));
	}


	public String decryptData(String data, String applicationId, String referenceId){
		return encryptDecryptData(data, false, applicationId, referenceId);
	}

	/**
	 * It encrypts and decrypts the data.
	 * 
	 * @param data      The data to be encrypted or decrypted.
	 * @param toEncrypt true if you want to encrypt, false if you want to decrypt
	 * @return ResponseWrapper<Map<String, Object>>
	 */
	public String encryptDecryptData(String data, boolean toEncrypt, String applicationId, String referenceId) {
		try {
			CryptomanagerRequestDto request = new CryptomanagerRequestDto();
			request.setApplicationId(applicationId);
			request.setReferenceId(referenceId);
			request.setData(data);
			request.setTimeStamp(DateUtils.getUTCCurrentDateTime());
			RequestWrapper<CryptomanagerRequestDto> requestWrapper = new RequestWrapper<>();
			requestWrapper.setRequest(request);
			ResponseWrapper<Map<String, Object>> responseWrapper = restClient.postApi(
					toEncrypt ? encryptUri : decryptUri, MediaType.APPLICATION_JSON_UTF8, requestWrapper,
					ResponseWrapper.class);
			if (Objects.nonNull(responseWrapper.getErrors()) && !responseWrapper.getErrors().isEmpty()) {
				logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
						LoggerFileConstant.APPLICATIONID.toString(), responseWrapper.getErrors().get(0).getMessage());
				throw new ResidentServiceException(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
						ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage());
			}
			return JsonUtil.convertValue(responseWrapper.getResponse(), CryptomanagerResponseDto.class).getData();
		} catch (ApisResourceAccessException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
					ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage(), e);
		}
	}

	/**
	 * This function returns boolean value indicating whether the object deleted or not.
	 * @param objectName The name of the object to be deleted.
	 * @return boolean value indicating whether the object deleted or not.
	 */
	public boolean deleteObject(String objectName) {
		try {
			return adapter.deleteObject(objectStoreAccountName, null, null, null, objectName);
		} catch (ResidentServiceException | ObjectStoreAdapterException e) {
			logger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
					LoggerFileConstant.APPLICATIONID.toString(), ExceptionUtils.getStackTrace(e));
			throw new ResidentServiceException(ResidentErrorCode.FAILED_TO_DELETE_DOC.getErrorCode(),
					ResidentErrorCode.FAILED_TO_DELETE_DOC.getErrorMessage(), e);
		}
	}
}
