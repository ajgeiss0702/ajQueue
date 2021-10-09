package us.ajg0702.queue.api.queues;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Represents a server or a group that can be queued for
 */
@SuppressWarnings("unused")
public interface QueueServer {

    /**
     * Get the players who are queued.
     * @return The players who are queued
     */
    ImmutableList<QueuePlayer> getQueue();

    /**
     * Get the status of the server as a string
     * @param p The player that you are checking for. Used for checking restricted servers
     * @return The status of the server as a string
     */
    String getStatusString(AdaptedPlayer p);

    /**
     * Get the status of the server as a string.
     * Does not check if the player has access using restricted mode. May show online if it is restricted
     * @return The status of the server as a string
     */
    String getStatusString();

    /**
     * Sends a server ping and uses the response to update online status, player count status, and whitelist status
     */
    void updatePing();

    /**
     * Gets the time the server has been offline, in miliseconds
     * @return The number of miliseconds the server has been offline for
     */
    int getOfflineTime();

    /**
     * Gets how long since the last person was sent
     * @return The number of miliseconds since the last person was sent
     */
    long getLastSentTime();

    /**
     * Sets the time the last person was sent
     * @param lastSentTime the time the last person was sent
     */
    void setLastSentTime(long lastSentTime);



    /**
     * Gets if the server is whitelisted or not
     * @return True if whitelisted, false if not
     */
    boolean isWhitelisted();

    /**
     * Sets if the server is whitelisted or not
     */
    void setWhitelisted(boolean whitelisted);

    /**
     * Gets the list of players who are whitelisted
     * @return The list of player UUIDs who are whitelisted
     */
    ImmutableList<UUID> getWhitelistedPlayers();

    /**
     * Sets the list of UUIDs that are whitelisted
     */
    void setWhitelistedPlayers(List<UUID> whitelistedPlayers);

    /**
     * Checks if the server is joinable by a player
     * @param p The player to see if they can join
     * @return If the server is joinable
     */
    boolean isJoinable(AdaptedPlayer p);

    /**
     * Pauses or unpauses a server
     * @param paused true = paused, false = unpaused
     */
    void setPaused(boolean paused);

    /**
     * Checks if the server is paused
     * @return True if the server is paused, false if its not
     */
    boolean isPaused();

    /**
     * Checks if the server is online
     * @return True if the server is online, false if not
     */
    boolean isOnline();

    /**
     * Checks if the server went online within the time set in the config
     * @return If the sevrer just came online
     */
    boolean justWentOnline();

    /**
     * Checks if the server is full
     * @return If the server is full
     */
    boolean isFull();

    /**
     * Removes a player from the queue
     * @param player The player to remove
     */
    void removePlayer(QueuePlayer player);

    /**
     * Removes a player from the queue
     * @param player The player to remove
     */
    void removePlayer(AdaptedPlayer player);

    /**
     * Adds a player to the end of the queue
     * NOTE: It is reccomended to use QueueManager#addToQueue
     * @param player The QueuePlayer t add
     */
    void addPlayer(QueuePlayer player);

    /**
     * Adds a player to the specified position in the queue
     * NOTE: It is reccomended to use QueueManager#addToQueue
     * @param player The QueuePlayer to add
     * @param position The position to add them
     */
    void addPlayer(QueuePlayer player, int position);

    /**
     * Sends the first player in the queue to the server
     */
    void sendPlayer();

    /**
     * Gets the name of the server/group
     * @return The name of the server/group
     */
    String getName();


    /**
     * If the player can access the server. (Bungeecord's restricted servers)
     * If on a platform that doesnt have restricted servers, this will always return true.
     * @param ply The player
     * @return True if the player can join based on bungeecord's restricted servers system
     */
    boolean canAccess(AdaptedPlayer ply);

    /**
     * The alias of this server. For displaying.
     * @return The alias of this server
     */
    String getAlias();

    /**
     * Get the servers that this QueueServer represents
     * @return A list of servers that this QueueServer represents
     */
    ImmutableList<AdaptedServer> getServers();

    /**
     * Gets the names of the servers in this group
     * @return A list of names
     */
    ImmutableList<String> getServerNames();


    /**
     * Returns if this server is a group
     * @return True if this server is a group
     */
    boolean isGroup();

    /**
     * Finds the player in this queue and returns the representative QueuePlayer
     * @return The QueuePlayer representing the player, null if not found
     */
    QueuePlayer findPlayer(AdaptedPlayer player);

    /**
     * Finds the player with this uuid in this queue and returns the representative QueuePlayer
     * @return The QueuePlayer representing the player, null if not found
     */
    QueuePlayer findPlayer(UUID uuid);


    /**
     * Gets the most ideal server in this group to join
     * @param player The player that would be joining
     * @return The ideal server to join
     */
    AdaptedServer getIdealServer(AdaptedPlayer player);

    /**
     * Gets the last server pings
     * @return The last server pings for this server/group
     */
    HashMap<AdaptedServer, AdaptedServerPing> getLastPings();

    /**
     * Gets the protocol versions this queue supports.
     * A blank list means all protocols are supported.
     * @return The protocol versions this queue supports
     */
    List<Integer> getSupportedProtocols();

    /**
     * Sets the protocols that are supported
     * A blank list means all protocols are supported.
     * @param list the list of protocols that are supported
     */
    void setSupportedProtocols(List<Integer> list);

    /**
     * Gets the balancer this server is using
     * @return The balancer this server is using
     */
    Balancer getBalancer();


    /**
     * elliot is bad
     * @return true because elliot is bad
     */
    @SuppressWarnings({"unused", "SameReturnValue"})
    default boolean elliot_is_bad() {
        return true;
    }
}
