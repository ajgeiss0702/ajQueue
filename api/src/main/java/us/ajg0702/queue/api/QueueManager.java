package us.ajg0702.queue.api;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;

public interface QueueManager {

    /**
     * Adds a player to a queue
     * @param player The player to be added
     * @param server The server or group to add the player to
     * @return True if adding was successfull, false if not.
     */
    boolean addToQueue(AdaptedPlayer player, QueueServer server);

    /**
     * Adds a player to a queue
     * @param player The player to be added
     * @param serverName The name of the server or group to add the player too
     * @return True if adding was successfull, false if not.
     */
    boolean addToQueue(AdaptedPlayer player, String serverName);

    /**
     * Gets a list of QueueServers (servers and groups)
     * @return A list of QueueServerss
     */
    ImmutableList<QueueServer> getServers();

    /**
     * Gets a list of QueueServer (servers and groups) names
     * @return A list of QueueServer names
     */
    ImmutableList<String> getServerNames();


}
