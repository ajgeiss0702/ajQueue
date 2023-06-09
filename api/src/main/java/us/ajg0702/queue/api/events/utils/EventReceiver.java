package us.ajg0702.queue.api.events.utils;

@FunctionalInterface
public interface EventReceiver<E> {
    void execute(E event);
}
