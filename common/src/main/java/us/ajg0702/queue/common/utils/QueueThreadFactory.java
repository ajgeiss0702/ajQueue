package us.ajg0702.queue.common.utils;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueThreadFactory implements ThreadFactory {
    private final String name;
    private final AtomicInteger i = new AtomicInteger(0);

    public QueueThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        return new Thread(runnable, "AJQUEUE-" + name + "-" + i.incrementAndGet());

    }
}
