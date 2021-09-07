package us.ajg0702.queue.api.util;

import us.ajg0702.utils.common.UtilsLogger;

public interface QueueLogger extends UtilsLogger {
    void warn(String message);
    void warning(String message);
    void info(String message);
    void error(String message);
    void severe(String message);
}
