package us.ajg0702.queue.api.queueholders;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class QueueHolder {

    private final QueueServer queueServer;

    public QueueHolder(QueueServer queueServer) {
        this.queueServer = queueServer;
    }

    /**
     * Returns the identifier of this QueueHolder
     * Used by the server owner in order to tell ajQueue to use this QueueHolder
     * @return a string that is very unlikely to be re-used by another QueueHolder
     */
    public abstract String getIdentifier();


    /**
     * Adds a player to the end of the queue
     * NOTE: Do not manually call this! Use the QueueManager to add players to queues
     * @param player The QueuePlayer to add
     */
    public abstract void addPlayer(QueuePlayer player);

    /**
     * Adds a player to the specified position in the queue
     * NOTE: Do not manually call this! Use the QueueManager to add players to queues
     * @param player The QueuePlayer to add
     * @param position The position to add them to
     */
    public abstract void addPlayer(QueuePlayer player, int position);

    public void removePlayer(AdaptedPlayer player) {
        removePlayer(player.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        QueuePlayer player = findPlayer(uuid);
        if(player == null) return;
        removePlayer(player);
    }

    /**
     * Removes a player from the queue
     * @param player The player to remove
     */
    public abstract void removePlayer(QueuePlayer player);

    /**
     * Finds the player with this uuid in this queue and returns the representative QueuePlayer
     * @return The QueuePlayer representing the player, null if not found
     */
    public abstract QueuePlayer findPlayer(UUID uuid);

    /**
     * Finds the player with this username in this queue and returns the representative QueuePlayer
     * @return The QueuePlayer representing the player, null if not found
     */
    public abstract QueuePlayer findPlayer(String name);

    public QueuePlayer findPlayer(AdaptedPlayer player) {
        return findPlayer(player.getUniqueId());
    }

    /**
     * Returns the size of the standard queue
     * @return The number of players in the standard queue
     */
    public abstract int getStandardQueueSize();

    /**
     * Returns the size of the express queue
     * @return The number of players in the express queue
     */
    public abstract int getExpressQueueSize();

    /**
     * Gets the size of both the express and standard queues combines
     * @return the number of players in both queues
     */
    public abstract int getTotalQueueSize();

    public abstract int getPosition(QueuePlayer player);

    /**
     * Get all players that are in the standard queue
     * @return a list of players in the standard queue
     */
    public abstract List<QueuePlayer> getAllStandardPlayers();

    /**
     * Get all players that are in the express queue
     * @return a list of players in the express queue
     */
    public abstract List<QueuePlayer> getAllExpressPlayers();

    /**
     * Gets all players that are in all queues for this server
     * @return a list of all players. Express queue players are first in the list, then standard queue players.
     */
    public List<QueuePlayer> getAllPlayers() {
        List<QueuePlayer> players = new ArrayList<>();
        players.addAll(getAllExpressPlayers());
        players.addAll(getAllStandardPlayers());
        return players;
    }
}
