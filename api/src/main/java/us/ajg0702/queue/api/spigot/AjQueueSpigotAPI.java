package us.ajg0702.queue.api.spigot;

import java.util.UUID;
import java.util.concurrent.Future;

/**
 * An API that is usable from the spigot-side
 */
public abstract class AjQueueSpigotAPI {

    public static AjQueueSpigotAPI INSTANCE;

    /**
     * Gets the instance of the ajQueue spigot API
     * @return the ajQueue API
     */
    @SuppressWarnings("unused")
    public static AjQueueSpigotAPI getInstance() {
        return INSTANCE;
    }

    public abstract Future<Boolean> isInQueue(UUID player);

    /**
     * Adds a player to a queue (bypassing any permission checks that would prevent it)
     * @param player The player to be added
     * @param queueName The server or group to add the player to
     * @return True if adding was successful, false if not.
     */
    public abstract Future<Boolean> addToQueue(UUID player, String queueName);

    /**
     * Emulate the player running the queue command for a certain server, including any permission checks
     * @param player The player to sudo the command for
     * @param queueName The queue to send the player to
     */
    public abstract void sudoQueue(UUID player, String queueName);

    /**
     * Gets the name of the queue that the player is in
     * @param player the player
     * @return the name of the queue that the player is in
     */
    public abstract Future<MessagedResponse<String>> getQueueName(UUID player);

    /**
     * Gets the position of the player in their queue
     * @param player The player
     * @return The position of the player in their queue
     */
    public abstract Future<MessagedResponse<Integer>> getPosition(UUID player);

    /**
     * Gets the total number of players who are in queue with the player
     * @param player The player
     * @return The number of player in the queue that the player is in.
     */
    public abstract Future<MessagedResponse<Integer>> getTotalPositions(UUID player);

    /**
     * Gets the number of players in a specific queue
     * @param queueName The name of the queue
     * @return The number of players in that queue.
     */
    public abstract Future<Integer> getPlayersInQueue(String queueName);

    /**
     * Gets the status string for the queue specified (e.g. full, restarting, etc)
     * This is the display status, which is meant to be shown to players (and is pulled from the messages file)
     * @param queueName the name of the queue
     * @return The status string for the queue you specified.
     */
    public abstract Future<String> getServerStatusString(String queueName);

    /**
     * Gets the status string for the queue specified (e.g. full, restarting, etc)
     * This is the display status, which is meant to be shown to players (and is pulled from the messages file)
     * @param queueName the name of the queue
     * @param player the player to check with
     * @return The status string for the queue you specified.
     */
    public abstract Future<String> getServerStatusString(String queueName, UUID player);

    /**
     * Gets the estimated time until the player is sent to the server
     * @param player The player to get
     * @return The estimated time until the player is sent to the server
     */
    public abstract Future<MessagedResponse<String>> getEstimatedTime(UUID player);

}
