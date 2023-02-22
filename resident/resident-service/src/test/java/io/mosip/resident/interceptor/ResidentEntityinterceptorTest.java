package io.mosip.resident.interceptor;

import io.mosip.resident.entity.ResidentSessionEntity;
import io.mosip.resident.entity.ResidentTransactionEntity;
import io.mosip.resident.helper.ObjectStoreHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertFalse;

/**
 * @author Kamesh Shekhar Prasad
 */
@RunWith(MockitoJUnitRunner.class)
@RefreshScope
@ContextConfiguration
public class ResidentEntityinterceptorTest {

    @InjectMocks
    private ResidentEntityInterceptor residentEntityInterceptor;

    @Mock
    private ObjectStoreHelper objectStoreHelper;

    private ResidentTransactionEntity residentTransactionEntity;
    private Object[] state;
    private  String[] propertyName;

    @Before
    public void setup(){
        residentTransactionEntity = new ResidentTransactionEntity();
        residentTransactionEntity.setIndividualId("1234567890");
        state = new Object[1];
        state[0] = "k";
        propertyName = new String[1];
        propertyName[0] = "individualId";
    }

    @Test
    public void testOnSave(){
        assertFalse(residentEntityInterceptor.onSave(residentTransactionEntity,
                null, state, propertyName, null));
    }

    @Test
    public void testOnLoad(){
        propertyName[0] = "individualI";
        assertFalse(residentEntityInterceptor.onLoad(new ResidentSessionEntity(), null, state, propertyName, null));
    }

    @Test
    public void testOnFlushDirty(){
        assertFalse(residentEntityInterceptor.onFlushDirty(residentTransactionEntity, null, state, null, propertyName, null));
    }
}
