package us.ajg0702.queue.api.events;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

/**
 * Called after a player is successfully sent to a server.
 */
public class SuccessfulSendEvent implements Event {
    private final QueuePlayer player;
    private final AdaptedServer server;

    public SuccessfulSendEvent(QueuePlayer player, AdaptedServer server) {
        this.player = player;
        this.server = server;
    }

    /**
     * Gets the player that was sent
     * @return the player that was sent
     */
    public AdaptedPlayer getPlayer() {
        return player.getPlayer();
    }

    /**
     * Gets the server that the player was sent to
     * @return The server that the player was sent to
     */
    public AdaptedServer getServer() {
        return server;
    }
}
