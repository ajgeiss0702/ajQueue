package us.ajg0702.queue.api.events;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;

/**
 * Called after all checks are made, right before a player is actually added to the queue.
 * If canceled, the player will not be added to the queue.
 * If you cancel this event, it is up to you to send a message telling the player why they were not added to the queue.
 */
public class PreQueueEvent implements Event, Cancellable {
    private final AdaptedPlayer player;
    private final QueueServer target;

    private boolean cancelled = false;

    public PreQueueEvent(AdaptedPlayer player, QueueServer target) {
        this.player = player;
        this.target = target;
    }

    /**
     * Gets the player that is joining the queue
     * @return
     */
    public AdaptedPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the target QueueServer that the player is trying to queue for
     * @return The QueueServer that the player is trying to queue for
     */
    public QueueServer getTarget() {
        return target;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
