package io.mosip.resident.interceptor;

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
import org.springframework.test.util.ReflectionTestUtils;

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
        ReflectionTestUtils.setField(residentEntityInterceptor, "appId", "resident");
        ReflectionTestUtils.setField(residentEntityInterceptor, "refId", "resident");
    }

    @Test
    public void testOnSaveSuccess(){
        assertFalse(residentEntityInterceptor.onSave(residentTransactionEntity,
                null, state, propertyName, null));
    }

    @Test
    public void testOnSaveFailure(){
        assertFalse(residentEntityInterceptor.onSave(residentTransactionEntity,
                null, state, propertyName, null));
    }

    @Test
    public void testOnLoadFailure(){
        assertFalse(residentEntityInterceptor.onLoad(residentTransactionEntity, null, state, propertyName, null));
    }

    @Test
    public void testOnLoadSuccess(){
        assertFalse(residentEntityInterceptor.onLoad(residentTransactionEntity, null, state, propertyName, null));
    }

    @Test
    public void testOnFlushDirty(){
        assertFalse(residentEntityInterceptor.onFlushDirty(residentTransactionEntity, null, state, null, propertyName, null));
    }
}
