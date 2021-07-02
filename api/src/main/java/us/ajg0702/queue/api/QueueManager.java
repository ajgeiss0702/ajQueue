package us.ajg0702.queue.api;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;

public interface QueueManager {

    /**
     * Adds a player to a queue
     * @param player The player to be added
     * @param server The server or group to add the player to
     * @return True if adding was successfull, false if not.
     */
    public boolean addToQueue(AdaptedPlayer player, QueueServer server);

}
