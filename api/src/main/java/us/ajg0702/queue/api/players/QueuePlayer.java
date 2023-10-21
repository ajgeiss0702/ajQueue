package us.ajg0702.queue.api.players;

import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;

import javax.annotation.Nullable;
import java.util.UUID;

public interface QueuePlayer {

    /**
     * Returns the player's UUID
     * @return the player's UUID
     */
    UUID getUniqueId();

    /**
     * Gets the server or group this player is queued for
     * @return The QueueServer this player is queued for
     */
    QueueServer getQueueServer();

    /**
     * Gets the player's position in the queue
     * @return The player's position. 1 being 1st, 2 being 2nd, etc
     */
    int getPosition();

    /**
     * Get the player this represents.
     * Can be null because the player could not be online
     * @return The player if they are online, null otherwise
     */
    @Nullable AdaptedPlayer getPlayer();

    /**
     * Sets the player that this represents.
     * Will throw IllegalArgumentException if the player's uuid does not match the original.
     * @param player The player to add
     */
    void setPlayer(AdaptedPlayer player);

    /**
     * Gets the highest priority level the player has.
     * In free ajQueue, no priority is 0 and priority is 1
     * @return The priority level of this player for this server
     */
    int getPriority();

    /**
     * Gets if this player has priority
     */
    boolean hasPriority();

    /**
     * Gets the player's username
     * @return the player's username
     */
    String getName();

    /**
     * Returns the number of miliseconds since this player was online
     * @return The number of miliseconds since this player was online
     */
    long getTimeSinceOnline();

    /**
     * Gets the max number of seconds this player is allowed to be offline before getting removed from the queue.
     * @return the max number of seconds this player can be offline before being removed from the queue
     */
    int getMaxOfflineTime();

    /**
     * Gets the server that the player was in when they joined the queue
     * @return the server that the player was in when they joined the queue
     */
    AdaptedServer getInitialServer();
}
