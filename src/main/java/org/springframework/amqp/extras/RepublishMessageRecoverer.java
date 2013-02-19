package org.springframework.amqp.extras;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

/**
 * MessageRecoverer implementation that republishes recovered messages to a
 * specified exchange with the exception stacktrace stored in the message header
 * x-exception.
 * <p/>
 * If no exchange is specified for the exchangeName then the message will simply
 * be published to the default exchange with "error." prepended to the routing
 * key.
 * 
 * @author jamescarr
 */
public class RepublishMessageRecoverer implements MessageRecoverer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RepublishMessageRecoverer.class);
	private AmqpTemplate errorTemplate;
	private String errorRoutingKey;

	
	private String exchangeName;

	/**
	 * @param errorTemplate
	 */
	public void setErrorTemplate(AmqpTemplate errorTemplate) {
		this.errorTemplate = errorTemplate;
	}

	public void setErrorRoutingKey(String errorRoutingKey) {
		this.errorRoutingKey = errorRoutingKey;
	}

	/**
	 * @param exchangeName
	 */
	public void setErrorExchange(String exchangeName) {
		this.exchangeName = exchangeName;

	}

	public void recover(Message message, Throwable cause) {
		Map <String,Object> headers = message.getMessageProperties().getHeaders();
		headers.put("x-exception-stacktrace", getStackTraceAsString(cause));
		headers.put("x-exception-message", cause.getCause().getMessage());
		headers.put("x-original-exchange", message.getMessageProperties().getReceivedExchange());

		headers.put("x-consumerxxx-error-date", getFormattedDate());

		if (null != exchangeName) {
			String routingKey = errorRoutingKey != null ? errorRoutingKey : message.getMessageProperties().getReceivedRoutingKey();
			errorTemplate.send(exchangeName, routingKey, message);
			LOGGER.warn("Republishing message to exchange {}", exchangeName);
		}
		else {
			final String routingKey = "error." + message.getMessageProperties().getReceivedRoutingKey();
			errorTemplate.send(routingKey, message);
			LOGGER.warn("Republishing error'd message to default exchange with routing key {}", routingKey);
		}
	}

	private String getFormattedDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(new Date());
	}

	private String getStackTraceAsString(Throwable cause) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		cause.printStackTrace(new PrintStream(byteArrayOutputStream));
		String exceptionAsString = byteArrayOutputStream.toString();
		return exceptionAsString;
	}

}
