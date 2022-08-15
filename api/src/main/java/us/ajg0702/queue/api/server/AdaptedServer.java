package us.ajg0702.queue.api.server;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.util.Handle;
import us.ajg0702.queue.api.util.QueueLogger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface AdaptedServer extends Handle {

    /**
     * Gets the ServerInfo for this server
     * @return The AdaptedServerInfo for this server
     */
    AdaptedServerInfo getServerInfo();

    /**
     * Gets the server's name
     * @return the server's name
     */
    String getName();

    /**
     * Pings the server and gets info back
     * @return A CompletableFuture with the ServerPing
     */
    default CompletableFuture<AdaptedServerPing> ping() {
        return ping(false, null);
    }

    CompletableFuture<AdaptedServerPing> ping(boolean debug, QueueLogger logger);

    Optional<AdaptedServerPing> getLastPing();

    /**
     * If the player can access the server
     * Uses bungeecord's restricted server feature
     * Will always return true on other platforms
     * @param player The player to check
     * @return False if the server is restricted and the player does not have permission to join.
     */
    @SuppressWarnings("SameReturnValue")
    boolean canAccess(AdaptedPlayer player);

    List<AdaptedPlayer> getPlayers();

    /**
     * Gets the number of seconds this server has been offline
     * @return The number of seconds this server has been offline
     */
    int getOfflineTime();

    boolean canJoinFull(AdaptedPlayer player);

    boolean justWentOnline();

    default boolean isJoinable(AdaptedPlayer player) {
        if(player != null) {
            if (isWhitelisted() && !getWhitelistedPlayers().contains(player.getUniqueId())) {
                return false;
            }
            if (isFull() && !canJoinFull(player)) {
                return false;
            }
        }
        return isOnline() &&
                canAccess(player);
    }

    default boolean isFull() {
        if(!getLastPing().isPresent()) return false;
        return getLastPing().get().getPlayerCount() >= getLastPing().get().getMaxPlayers();
    }

    /**
     * Gets if the last ping was successfull
     * (which almost always means the server is online)
     * @return If the server is determined to be online or not
     */
    default boolean isOnline() {
        return getLastPing().isPresent();
    }

    /**
     * Gets the number of players currently online
     * @return The number of players online
     */
    default int getPlayerCount() {
        if(!getLastPing().isPresent()) return 0;

        AdaptedServerPing ping = getLastPing().get();
        return ping.getPlayerCount();
    }

    /**
     * Gets the maximum number of players that can join this server.
     * @return The maximum number of players that can join this server
     */
    default int getMaxPlayers() {
        if(!getLastPing().isPresent()) return 0;

        AdaptedServerPing ping = getLastPing().get();
        return ping.getMaxPlayers();
    }

    /**
     * Temporarly adds one player to the player count
     */
    default void addPlayer() {
        if(!getLastPing().isPresent()) return;
        getLastPing().get().addPlayer();
    }

    /**
     * Checks if the spigot-side reports that the server is whitelisted
     * @return True if the server is whitelisted
     */
    default boolean isWhitelisted() {
        if(!getLastPing().isPresent()) return false;
        return getLastPing().get().getPlainDescription().contains("ajQueue;whitelisted=");
    }

    /**
     * (if the server is whitelisted) returns the list of players that are whitelisted
     * @return The list of players that are whitelisted
     */
    default List<UUID> getWhitelistedPlayers() {
        if(!getLastPing().isPresent()) return Collections.emptyList();
        if(!isWhitelisted()) return Collections.emptyList();
        List<UUID> uuids = new ArrayList<>();
        for(String uuid : getLastPing().get().getPlainDescription().substring(20).split(",")) {
            if(uuid.isEmpty()) continue;
            UUID parsedUUID;
            try {
                parsedUUID = UUID.fromString(uuid);
            } catch(IllegalArgumentException ignored) {
                continue;
            }
            uuids.add(parsedUUID);
        }
        return uuids;
    }

}
