package org.springframework.amqp.extras;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;

import static org.junit.Assert.assertFalse;

/**
 * Created by IntelliJ IDEA.
 * User: jamescarr
 * Date: 11/16/11
 * Time: 12:02 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleRetryOperationBuilderTest {
    @Mock
    AmqpTemplate amqpTemplate;

    @Test
    public void shouldBuildRetryInterceptorBean(){
        SimpleRetryOperationBuilder builder = new SimpleRetryOperationBuilder(amqpTemplate);
        StatefulRetryOperationsInterceptor interceptor = builder.afterMaxAttempts(10).publishTo("error");

        assertFalse(interceptor == null);
    }
}
