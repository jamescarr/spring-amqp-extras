package org.springframework.amqp.extras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

/**
 * 
 * @author James Carr
 *
 */
public class LoggingErrorHandler implements ErrorHandler {
	private final Logger logger;

	public LoggingErrorHandler(Logger logger) {
		this.logger = logger;
	}
	/**
	 * Creates a logger using the provided name.
	 * @param loggerName
	 */
	public LoggingErrorHandler(String loggerName) {
		this(LoggerFactory.getLogger(loggerName));
	}
	
	public void handleError(Throwable t) {
		logger.error("Error consuming message", t);

	}

}
