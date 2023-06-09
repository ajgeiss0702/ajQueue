package us.ajg0702.queue.api.events;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

/**
 * Called when someone is added or removed from a queue that the player is in, causing the player's position to change
 */
public class PositionChangeEvent implements Event {
    private final QueuePlayer player;
    private final int position;

    public PositionChangeEvent(QueuePlayer player) {
        this.player = player;
        position = player.getPosition();
    }

    /**
     * Gets the QueuePlayer object that represents this player
     * @return the QueuePlayer object
     */
    public QueuePlayer getQueuePlayer() {
        return player;
    }

    /**
     * Gets the AdaptedPlayer that this event is about. May return null!
     * @return The AdaptedPlayer that this event is about. Returns null if the player is offline.
     */
    public @Nullable AdaptedPlayer getPlayer() {
        return player.getPlayer();
    }

    /**
     * Gets the player's new position in the queue
     * @return The player's new position. 1 being 1st, 2 being 2nd, etc
     */
    public int getPosition() {
        return position;
    }

    /**
     * Gets the queue that this event is from
     * @return The QueueServer that the player is in that their position changed
     */
    public QueueServer getQueue() {
        return player.getQueueServer();
    }
}
