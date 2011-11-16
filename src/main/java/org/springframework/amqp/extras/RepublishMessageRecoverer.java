package org.springframework.amqp.extras;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

/**
 * MessageRecoverer implementation that republishes recovered messages
 * to a specified exchange with the exception stacktrace stored in the
 * message header x-exception.
 * <p/>
 * If no exchange is specified for the exchangeName then the message will
 * simply be published to the default exchange with "error." prepended
 * to the routing key.
 *
 * @author jamescarr
 */
public class RepublishMessageRecoverer implements MessageRecoverer {
    private AmqpTemplate errorTemplate;
    private String exchangeName;

    /**
     * @param errorTemplate
     */
    public void setErrorTemplate(AmqpTemplate errorTemplate) {
        this.errorTemplate = errorTemplate;
    }

    /**
     * @param exchangeName
     */
    public void setErrorExchange(String exchangeName) {
        this.exchangeName = exchangeName;

    }

    public void recover(Message message, Throwable cause) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        cause.printStackTrace(new PrintStream(byteArrayOutputStream));
        message.getMessageProperties().getHeaders()
                .put("x-exception", byteArrayOutputStream.toString());
        if (null != exchangeName) {
            errorTemplate.send(exchangeName, message.getMessageProperties().getReceivedRoutingKey(), message);
        } else {
            errorTemplate.send("error." + message.getMessageProperties().getReceivedRoutingKey(), message);
        }
    }


}
