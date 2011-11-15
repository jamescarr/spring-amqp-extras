package org.springframework.amqp.extras;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

/**
 * A default implementation of ErrorHandler that takes a Logger
 * provided by the user and logs all messages to that logger with
 * a log level of warn.
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
