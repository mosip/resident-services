package io.mosip.resident.interceptor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.commons.khazana.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.constant.ResidentErrorCode;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;

/**
 * @author Neha Farheen
 *
 */
@Component
public class ResidentEntityInterceptor extends EmptyInterceptor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3428378823034671471L;

	private static final String INDIVIDUAL_ID = "individualId";

	@Autowired
	private transient ObjectStoreHelper objectStoreHelper;
	
	@Value("${mosip.resident.keymanager.application-name}")
	private String appId;
	
	@Value("${mosip.resident.keymanager.reference-id}")
	private String refId;

	/** The mosip logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentEntityInterceptor.class);

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			if (entity instanceof ResidentTransactionEntity) {
				List<String> propertyNamesList = Arrays.asList(propertyNames);
				encryptDataOnSave(id, state, propertyNamesList, types, (ResidentTransactionEntity) entity);
			}
		} catch (ResidentServiceException e) {
			logger.error(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
					ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage(), e);
			throw new ResidentServiceException(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
					ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage(), e);
		}
		return super.onSave(entity, id, state, propertyNames, types);
	}

	private <T extends ResidentTransactionEntity> void encryptDataOnSave(Serializable id, Object[] state,
			List<String> propertyNamesList, Type[] types, T uinEntity) throws ResidentServiceException {
		if (Objects.nonNull(uinEntity.getIndividualId())) {
			String idividualId = Base64.encodeBase64String(uinEntity.getIndividualId().getBytes());
			String encryptedData = objectStoreHelper.encryptDecryptData(idividualId, true, appId, refId);
			uinEntity.setIndividualId(encryptedData);
			int indexOfData = propertyNamesList.indexOf(INDIVIDUAL_ID);
			state[indexOfData] = encryptedData;
		}
	}
	
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			if (entity instanceof ResidentTransactionEntity) {
				List<String> propertyNamesList = Arrays.asList(propertyNames);
				int indexOfData = propertyNamesList.indexOf(INDIVIDUAL_ID);
				if (Objects.nonNull(state[indexOfData])) {
					decryptDataOnLoad(id, state, propertyNamesList, types, (ResidentTransactionEntity) entity);
				}
			}
		} catch (ResidentServiceException e) {
			logger.error(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
					ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage(), e);
			throw new ResidentServiceException(ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorCode(),
					ResidentErrorCode.ENCRYPT_DECRYPT_ERROR.getErrorMessage(), e);
		}
		return super.onLoad(entity, id, state, propertyNames, types);
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id, Object[] state, Object[] previousState,
			String[] propertyNames, Type[] types) {
		if(entity instanceof ResidentTransactionEntity) {
			List<String> propertyNamesList = Arrays.asList(propertyNames);
			encryptDataOnSave(id, state, propertyNamesList, types, (ResidentTransactionEntity) entity);
		}
		return super.onFlushDirty(entity, id, state, previousState, propertyNames, types);
	}

	private <T extends ResidentTransactionEntity> void decryptDataOnLoad(Serializable id, Object[] state,
			List<String> propertyNamesList, Type[] types, T uinEntity) throws ResidentServiceException {
		int indexOfData = propertyNamesList.indexOf(INDIVIDUAL_ID);
		if (Objects.nonNull(state[indexOfData])) {
			String individualId = (String) state[indexOfData];
			String decodedIndividualId = tryDecryption(individualId, INDIVIDUAL_ID);
			uinEntity.setIndividualId(decodedIndividualId);
			state[indexOfData] = decodedIndividualId;
		}
	}

	private String tryDecryption(String data, String attributeName) {
		try {
			String decryptedData = objectStoreHelper.encryptDecryptData(data, false, appId, refId);
			String decodedIndividualId = new String(Base64.decodeBase64(decryptedData));
			return decodedIndividualId;
		} catch (ResidentServiceException e) {
			logger.debug(String.format("Unable to decrpt data in interceptor: %s", attributeName));
			return data;
		}
	}
}
