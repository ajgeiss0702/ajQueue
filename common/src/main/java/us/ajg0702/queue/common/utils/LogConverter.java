package us.ajg0702.queue.common.utils;

import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.util.QueueLogger;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogConverter extends Logger {
    private final QueueLogger logger;
    public LogConverter(QueueLogger logger) {
        super("ajqueue-convert", null);
        this.logger = logger;
    }

    @Override
    public void log(@NotNull LogRecord logRecord) {
        String message = logRecord.getMessage();
        switch(logRecord.getLevel().getName()) {
            case "OFF":
                break;
            case "SEVERE":
                logger.error(message);
                break;
            case "WARNING":
                logger.warn(message);
                break;
            case "INFO":
                logger.info(message);
                break;
            default:
                logger.info(message);
                break;
        }
    }
}
