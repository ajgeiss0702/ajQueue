package us.ajg0702.queue.api.events;

import us.ajg0702.queue.api.players.AdaptedPlayer;

/**
 * Called before AjQueue auto-queues a player for a server. (on player kick)
 * Use Case: View/Change the server that the player is auto-queued for.
 * If canceled, the player will not be queued to any server.
 * If you cancel this event, it is up to you to send a message telling the player why they were not auto-queued.
 */
@SuppressWarnings("unused")
public class AutoQueueOnKickEvent implements Event, Cancellable {
    private final AdaptedPlayer player;
    private String targetServer;

    private boolean cancelled = false;

    public AutoQueueOnKickEvent(AdaptedPlayer player, String targetServer) {
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}

