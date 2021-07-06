package us.ajg0702.queue.api.players;

import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.util.Handle;

import java.util.UUID;

/**
 * Represents a cross-platform player
 */
public interface AdaptedPlayer extends Handle {

    /**
     * Check if the plauer is currently connected
     * @return True if connected, false if not
     */
    boolean isConnected();

    /**
     * Send a player a message from a Component
     * @param message The message to send
     */
    void sendMessage(Component message);

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
     * Gets the player's unique id (UUID)
     * @return The player's uuid
     */
    UUID getUniqueId();
}
