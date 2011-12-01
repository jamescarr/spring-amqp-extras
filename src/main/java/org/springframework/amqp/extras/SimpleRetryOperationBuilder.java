package org.springframework.amqp.extras;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.config.StatefulRetryOperationsInterceptorFactoryBean;
import org.springframework.amqp.rabbit.retry.MessageKeyGenerator;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * <p>Simplified facade to make it easier and simpler to build a StatefulRetryOperationsInterceptor
 * by providing a more fluent interface to defining behavior on error.
 * </p>
 * <p>
 * Typical example:
 * </p>
 *
 * <pre>
 * <code>
 * SimpleRetryOperationsBuillder builder = new  SimpleRetryOperationsBuillder(amqpTemplate);
 * StatefulRetryOperationsInterceptor advice = builder.afterMaxAttempts(10).publishTo("error-exchange");
 * </code>
 * </pre>
 * <p>
 * The default behavior determines message identity based on messageId. This isn't a required field and may  not
 * even be set. If it is not, you can change the logic to determine message
identity based on contents:</p>
 * <pre>
 * <code>
 * SimpleRetryOperationsBuillder builder = new  SimpleRetryOperationsBuillder(amqpTemplate);
 * StatefulRetryOperationsInterceptor advice = builder.using(new ContentBasedMessageKeyGenerator())
 *                                                                      .afterMaxAttempts(10).publishTo("error-exchange");
 * </code>
 * </pre>
 * <p>
 * The current default behavior uses a ContentBasedMessageKeyGenerator for identifying message "uniqueness"
 * but can be overridden by providing a MessageKeyGenerator implementation to the with() method.
 * </p>
 * 
 * @author James Carr
 */
public class SimpleRetryOperationBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRetryOperationBuilder.class);
    private final AmqpTemplate amqpTemplate;
    private MessageKeyGenerator messageKeyGenerator;

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
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(maxAttempts, Collections
                .<Class<? extends Throwable>, Boolean>singletonMap(Exception.class, true));
        
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setListeners(new RetryListener[]{new RetryListener(){

			public <T> boolean open(RetryContext context,
					RetryCallback<T> callback) {
				LOGGER.warn("Retry onError: {}", context.getRetryCount());
				return true;
			}

			public <T> void close(RetryContext context,
					RetryCallback<T> callback, Throwable throwable) {
				LOGGER.warn("Retry onError: {}", context.getRetryCount());
			}

			public <T> void onError(RetryContext context,
					RetryCallback<T> callback, Throwable throwable) {
				LOGGER.warn("Retry onError: {}", context.getRetryCount());
				
			}
		}});
        interceptorFactoryBean.setRetryOperations(retryTemplate);
        if(messageKeyGenerator != null){
            interceptorFactoryBean.setMessageKeyGeneretor(messageKeyGenerator);
        }

        RepublishMessageRecoverer messageRecoverer = new RepublishMessageRecoverer();
        messageRecoverer.setErrorTemplate(amqpTemplate);
        interceptorFactoryBean.setMessageRecoverer(messageRecoverer);
        return new PublishBuilder(interceptorFactoryBean, messageRecoverer);
    }

    public SimpleRetryOperationBuilder using(MessageKeyGenerator messageKeyGenerator) {
        this.messageKeyGenerator = messageKeyGenerator;
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

