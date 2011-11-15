package org.springframework.amqp.extras;

import static org.junit.Assert.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

public class ContentBasedMessageKeyGeneratorTest {
	private ContentBasedMessageKeyGenerator generator = new ContentBasedMessageKeyGenerator();
	Message message = new Message("foo".getBytes(), new MessageProperties());
	@Test
	public void shouldGenerateKeyBasedOnContents() {
		final Object key = generator.getKey(message);
		
		assertEquals(DigestUtils.md5Hex(message.getBody()), key);
	}

}
