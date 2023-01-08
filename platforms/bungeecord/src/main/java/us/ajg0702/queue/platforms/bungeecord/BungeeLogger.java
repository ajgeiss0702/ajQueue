package us.ajg0702.queue.platforms.bungeecord;

import us.ajg0702.queue.api.util.QueueLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BungeeLogger implements QueueLogger {

    private final Logger logger;

    protected BungeeLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @Override
    public void severe(String message) {
        logger.severe(message);
    }

    @Override
    public void warn(String message, Throwable t) {
        logger.log(Level.WARNING, message, t);
    }

    @Override
    public void warning(String message, Throwable t) {
        logger.log(Level.WARNING, message, t);
    }

    @Override
    public void info(String message, Throwable t) {
        logger.log(Level.INFO, message, t);
    }

    @Override
    public void error(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }

    @Override
    public void severe(String message, Throwable t) {
        logger.log(Level.SEVERE, message, t);
    }
}
