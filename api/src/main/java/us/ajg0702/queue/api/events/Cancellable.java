package us.ajg0702.queue.api.events;

public interface Cancellable {
    /**
     * Whether this event is canceled.
     * @return True if canceled. False if not.
     */
    boolean isCancelled();

    /**
     * Allows you to cancel or un-cancel this event
     * @param cancelled True to cancel the event, false to un-cancel
     */
    void setCancelled(boolean cancelled);
}
