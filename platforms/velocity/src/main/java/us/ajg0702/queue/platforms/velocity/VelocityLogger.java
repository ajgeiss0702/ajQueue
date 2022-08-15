package us.ajg0702.queue.platforms.velocity;


import org.slf4j.Logger;
import us.ajg0702.queue.api.util.QueueLogger;

public class VelocityLogger implements QueueLogger {

    private final Logger logger;

    protected VelocityLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public void warning(String message, Throwable t) {
        logger.warn(message, t);
    }

    @Override
    public void info(String message, Throwable t) {
        logger.info(message, t);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void severe(String message, Throwable t) {
        logger.error(message, t);
    }
}
