package org.springframework.amqp.extras;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageKeyGenerator;
import org.springframework.util.Assert;
/**
 * 
 * @author James Carr
 *
 */
public class ContentBasedMessageKeyGenerator implements MessageKeyGenerator{

	/**
	 * Generate a unique key for the message that is repeatable on redelivery. Implementation
	 * uses an MD5 hash of the contents of the message body to generate a unique key. 
	 * 
	 * @param message the message to generate a key for
	 * @return a unique key for this message
	 */
	public Object getKey(Message message) {
		Assert.notNull(message);
		return DigestUtils.md5Hex(message.getBody());
	}

}
