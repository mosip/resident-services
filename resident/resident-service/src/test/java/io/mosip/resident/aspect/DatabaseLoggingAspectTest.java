package io.mosip.resident.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * @author Kamesh Shekhar Prasad
 */
public class DatabaseLoggingAspectTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testAroundAdvice3() throws Throwable {
        DatabaseLoggingAspect databaseLoggingAspect = new DatabaseLoggingAspect();
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        when(proceedingJoinPoint.proceed()).thenReturn("Proceed");
        when(proceedingJoinPoint.getSignature()).thenReturn(null);
        assertEquals("Proceed", databaseLoggingAspect.aroundAdvice(proceedingJoinPoint));
        verify(proceedingJoinPoint).proceed();
        verify(proceedingJoinPoint, atLeast(1)).getSignature();
    }

    @Test
    public void testAroundAdvice4() throws Throwable {
        DatabaseLoggingAspect databaseLoggingAspect = new DatabaseLoggingAspect();
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        when(proceedingJoinPoint.proceed()).thenThrow(new Throwable());
        when(proceedingJoinPoint.getSignature()).thenReturn(null);
        thrown.expect(Throwable.class);
        databaseLoggingAspect.aroundAdvice(proceedingJoinPoint);
        verify(proceedingJoinPoint).proceed();
        verify(proceedingJoinPoint).getSignature();
    }

    @Test
    public void testConstructor() {
        (new DatabaseLoggingAspect()).loggableMethods();
    }
}

