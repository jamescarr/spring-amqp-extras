package org.springframework.amqp.extras;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.retry.MessageKeyGenerator;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

/**
 * Simplified facade to make it easier and simpler to build a StatefulRetryOperationsInterceptor
 * by providing a more fluent interface to defining behavior on error.
 * <p/>
 * Typical example:
 * <p/>
 * <code>
 * SimpleRetryOperationsBuillder builder = new  SimpleRetryOperationsBuillder(amqpTemplate);
 * StatefulRetryOperationsInterceptor advice = builder.afterMaxAttempts(10).publishTo("error-exchange");
 * </code>
 * <p/>
 * The current default behavior uses a ContentBasedMessageKeyGenerator for identifying message "uniqueness"
 * but can be overridden by providing a MessageKeyGenerator implementation to the with() method.
 *
 * @author James Carr
 */
public class SimpleRetryOperationBuilder {
    private final AmqpTemplate amqpTemplate;
    private MessageKeyGenerator messageKeyGenerator = new ContentBasedMessageKeyGenerator();

    public SimpleRetryOperationBuilder(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    /**
     * Starts the process of building an interceptor by specifying the maximum attempts
     * to retry a message before erroring out.
     *
     * @param maxAttempts
     * @return PublishBuilder
     */
    public PublishBuilder afterMaxAttempts(int maxAttempts) {
        StatefulRetryOperationsInterceptorFactoryBean interceptorFactoryBean = new StatefulRetryOperationsInterceptorFactoryBean();
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(maxAttempts, Collections
                .<Class<? extends Throwable>, Boolean>singletonMap(Exception.class, true)));

        interceptorFactoryBean.setRetryOperations(retryTemplate);
        interceptorFactoryBean.setMessageKeyGeneretor(messageKeyGenerator);
        RepublishMessageRecoverer messageRecoverer = new RepublishMessageRecoverer();
        messageRecoverer.setErrorTemplate(amqpTemplate);
        interceptorFactoryBean.setMessageRecoverer(messageRecoverer);
        return new PublishBuilder(interceptorFactoryBean, messageRecoverer);
    }

    public SimpleRetryOperationBuilder using(MessageKeyGenerator messageKeyGenerator) {
        return this;
    }

    static class PublishBuilder {

        private StatefulRetryOperationsInterceptorFactoryBean factory;
        private RepublishMessageRecoverer messageRecoverer;

        public PublishBuilder(StatefulRetryOperationsInterceptorFactoryBean factory, RepublishMessageRecoverer messageRecoverer) {
            this.factory = factory;
            this.messageRecoverer = messageRecoverer;
        }

        public StatefulRetryOperationsInterceptor publishTo(String exchangeName) {
            messageRecoverer.setErrorExchange(exchangeName);

            return factory.getObject();
        }
    }
}

