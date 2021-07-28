package us.ajg0702.queue.api.util;

public interface QueueLogger {
    void warn(String message);
    void warning(String message);
    void info(String message);
    void error(String message);
    void severe(String message);
}
