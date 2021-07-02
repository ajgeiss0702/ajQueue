package us.ajg0702.queue.api.players;

import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.util.Handle;

/**
 * Represents a cross-platform player
 */
public interface AdaptedPlayer extends Handle {

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
}
