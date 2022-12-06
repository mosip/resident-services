package io.mosip.resident.interceptor;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.commons.khazana.config.LoggerConfiguration;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.exception.ResidentServiceException;
import io.mosip.resident.helper.ObjectStoreHelper;
@Component
public class ResidentEntityInterceptor extends EmptyInterceptor {
	private static final String APP_ID = "mosip.resident.keymanager.application-name";
	private static final String REF_ID = "mosip.resident.keymanager.reference-id";
	
	@Autowired
	private transient ObjectStoreHelper objectStoreHelper;
	
	@Autowired
	private Environment env;
	
/** The mosip logger. */
	private static final Logger logger = LoggerConfiguration.logConfig(ResidentEntityInterceptor.class);
	
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			List<String> propertyNamesList = Arrays.asList(propertyNames);
			if (entity instanceof ResidentTransactionEntity) {
				encryptDataOnSave(id, state, propertyNamesList, types, (ResidentTransactionEntity) entity);
			}
		} catch (ResidentServiceException e) {
		
		}
		return super.onSave(entity, id, state, propertyNames, types);
	}

	private <T extends ResidentTransactionEntity> void encryptDataOnSave(Serializable id, Object[] state, List<String> propertyNamesList,
			Type[] types, T uinEntity) throws ResidentServiceException {
		if (Objects.nonNull(uinEntity.getIndividualId())) {
			String idividualId=Base64.encodeBase64String(uinEntity.getIndividualId().getBytes());
			String encryptedData = objectStoreHelper.encryptDecryptData(idividualId,true,env.getProperty(APP_ID),env.getProperty(REF_ID));
			uinEntity.setIndividualId(encryptedData);
			int indexOfData = propertyNamesList.indexOf("individualId");
			state[indexOfData] = encryptedData;
			
		}

	
	}
	
	


//	 * (non-Javadoc)
//	 * 
//	 * @see org.hibernate.EmptyInterceptor#onLoad(java.lang.Object,
//	 * java.io.Serializable, java.lang.Object[], java.lang.String[],
//	 * org.hibernate.type.Type[])
//	 */
	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
		try {
			List<String> propertyNamesList = Arrays.asList(propertyNames);
			if (entity instanceof ResidentTransactionEntity) {
				int indexOfData = propertyNamesList.indexOf("individualId");
				if (Objects.nonNull(state[indexOfData])) {
					decryptDataOnLoad(id, state, propertyNamesList, types, (ResidentTransactionEntity) entity);		

				}
			}
		} catch (ResidentServiceException e) {
			//logger.error(IdRepoSecurityManager.getUser(), ID_REPO_ENTITY_INTERCEPTOR, "onLoad", "\n" + e.getMessage());
		//throw new IdRepoAppUncheckedException(ENCRYPTION_DECRYPTION_FAILED, e);
		}
		return super.onLoad(entity, id, state, propertyNames, types);
	}
	
	
	private <T extends ResidentTransactionEntity> void decryptDataOnLoad(Serializable id, Object[] state, List<String> propertyNamesList,
			Type[] types, T uinEntity) throws ResidentServiceException {
		if (Objects.nonNull(uinEntity.getIndividualId())) {
			String idividualId=Base64.encodeBase64String(uinEntity.getIndividualId().getBytes());

			String decryptedData = objectStoreHelper.encryptDecryptData(idividualId,false,env.getProperty(APP_ID),env.getProperty(REF_ID));
			uinEntity.setIndividualId(decryptedData);
			int indexOfData = propertyNamesList.indexOf("individualId");
			state[indexOfData] = decryptedData;
			
		}

	
	}

	

}
