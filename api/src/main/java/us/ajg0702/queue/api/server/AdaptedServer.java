package us.ajg0702.queue.api.server;

import us.ajg0702.queue.api.players.AdaptedPlayer;

import java.util.concurrent.CompletableFuture;

public interface AdaptedServer {

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
    CompletableFuture<AdaptedServerPing> ping();

    /**
     * If the player can access the server
     * Uses bungeecord's restricted server feature
     * Will always return true on other platforms
     * @param player The player to check
     * @return False if the server is restricted and the player does not have permission to join.
     */
    boolean canAccess(AdaptedPlayer player);

}
