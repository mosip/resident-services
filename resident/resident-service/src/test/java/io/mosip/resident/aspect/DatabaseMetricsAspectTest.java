package io.mosip.resident.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * @author Kamesh Shekhar Prasad
 */
@ContextConfiguration(classes = {TestContext.class, WebApplicationContext.class})
@RunWith(SpringRunner.class)
@WebMvcTest
@TestPropertySource(locations="classpath:application.properties")
public class DatabaseMetricsAspectTest {

    @InjectMocks
    private DatabaseMetricsAspect databaseMetricsAspect;

    @Autowired
    private MeterRegistry meterRegistry;

    @Before
    public void init() {
        ReflectionTestUtils.setField(databaseMetricsAspect, "registry", meterRegistry);
    }

    @Test
    public void testAroundAdvice() throws Throwable {
        ProceedingJoinPoint proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        when(proceedingJoinPoint.proceed()).thenReturn("Proceed");
        Signature signature = mock(MethodSignature.class);
        when(signature.toShortString()).thenReturn("mockedMethod()");
        when(proceedingJoinPoint.getSignature()).thenReturn(signature);
        assertEquals("Proceed", databaseMetricsAspect.aroundAdvice(proceedingJoinPoint));
    }

    @TestConfiguration
    static class AdditionalConfig {
        @Bean
        public MeterRegistry registry() {
            return new SimpleMeterRegistry();
        }
    }
}

