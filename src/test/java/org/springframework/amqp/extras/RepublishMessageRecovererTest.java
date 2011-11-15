package org.springframework.amqp.extras;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@RunWith(MockitoJUnitRunner.class)
public class RepublishMessageRecovererTest {
	Message message = new Message("".getBytes(), new MessageProperties());
	Throwable cause = new Exception("I am Error. When all else fails use fire.");
	@Mock AmqpTemplate amqpTemplate;
	RepublishMessageRecoverer recoverer;
	
	@Before
	public void beforeEach(){
		recoverer = new RepublishMessageRecoverer();
		recoverer.setErrorTemplate(amqpTemplate);
		message.getMessageProperties().setReceivedRoutingKey("some.key");		
	}
	@Test
	public void shouldPublishWithRoutingKeyPrefixedWithErrorWhenExchangeIsNotSet(){
		recoverer.recover(message, cause);
		
		verify(amqpTemplate).send("error.some.key", message);
	}
	
	@Test
	public void shouldPublishToProvidedExchange(){
		recoverer.setErrorExchange("error");
		
		recoverer.recover(message, cause);
		
		verify(amqpTemplate).send("error", "some.key", message);
	}
	@Test
	public void shouldIncludeTheStacktraceInTheHeaderOfThePublishedMessage(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		cause.printStackTrace(new PrintStream(baos));
		final String expectedHeaderValue = baos.toString();
		
		recoverer.recover(message, cause);
		assertEquals(expectedHeaderValue, message.getMessageProperties().getHeaders().get("x-exception"));
	}
	
}
