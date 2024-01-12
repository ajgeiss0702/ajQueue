package us.ajg0702.queue.api.events;

import us.ajg0702.queue.api.players.AdaptedPlayer;

/**
 * Called before AjQueue queues a player for a server.
 * Use Case: View/Change the server that the player is re-queued for.
 */
@SuppressWarnings("unused")
public class AutoQueueEvent implements Event {
    private final AdaptedPlayer player;
    private String targetServer;

    public AutoQueueEvent(AdaptedPlayer player, String targetServer) {
        this.player = player;
        this.targetServer = targetServer;
    }

    /**
     * @return the player that is being re-queued
     */
    public AdaptedPlayer getPlayer() {
        return player;
    }

    /**
     * @return The name of the server AjQueue will queue the player for.
     */
    public String getTargetServer() {
        return targetServer;
    }

    /**
     * Set the name of the server AjQueue will queue the player for.
     * @param targetServer The name of the server to queue the player for.
     */
    public void setTargetServer(String targetServer) {
        this.targetServer = targetServer;
    }
}

