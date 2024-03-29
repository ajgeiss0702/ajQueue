package us.ajg0702.queue.api.players;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.util.Handle;

import java.util.List;
import java.util.UUID;

/**
 * Represents a cross-platform player
 */
@SuppressWarnings("unused")
public interface AdaptedPlayer extends Handle, Audience {

    /**
     * Check if the plauer is currently connected
     * @return True if connected, false if not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isConnected();

    /**
     * Send a player a message from a Component
     * @param message The message to send
     */
    void sendMessage(@NotNull Component message);

    /**
     * Sends an actionbar message to the player
     * @param message The message to send
     */
    void sendActionBar(@NotNull Component message);

    /**
     * Send a player a message from a string
     * Converted to Component internally
     * @param message The message to send
     */
    void sendMessage(String message);

    /**
     * Checks if the player has a certain permission
     * @param permission The permission to check
     * @return True if they have the permission, false if not
     */
    boolean hasPermission(String permission);

    /**
     * Gets the name of the server the player is currently on
     * @return The name of the server
     */
    String getServerName();

    /**
     * Gets the server that the player is currently connected to
     * @return The server that the player is currently connected to.
     */
    AdaptedServer getCurrentServer();

    /**
     * Gets the player's unique id (UUID)
     * @return The player's uuid
     */
    UUID getUniqueId();

    /**
     * Sends the player to a different server.
     * Does not use the queue.
     */
    void connect(AdaptedServer server);

    /**
     * Returns the version this player is running.
     * @return the version
     */
    int getProtocolVersion();

    /**
     * Gets the player's username
     * @return the player's username
     */
    @Nullable String getName();

    /**
     * Kick a player from the proxy
     * @param reason The reason to kick them with
     */
    void kick(Component reason);

    List<String> getPermissions();

    default boolean equals(AdaptedPlayer other) {
        return this.getUniqueId().equals(other.getUniqueId());
    }
}
