package us.ajg0702.queue.platforms.bungeecord;

import us.ajg0702.queue.api.util.QueueLogger;

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
}
