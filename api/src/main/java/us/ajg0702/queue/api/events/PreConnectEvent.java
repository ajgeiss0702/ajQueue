package us.ajg0702.queue.api.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

/**
 * Called before AjQueue attempts to connect a player to a proxy server (via Bungee or Velocity)
 * You can use setTargetServer to provide a custom AdaptedServer object for the player to connect to.
 * You can use setDelayStatus to temporarily delay the player from connecting. They will still be in the queue.
 */
@SuppressWarnings("unused")
public class PreConnectEvent implements Event {
    private @NotNull AdaptedServer targetServer;
    private final @NotNull AdaptedPlayer adaptedPlayer;
    private @Nullable String delayStatus = null;

    public PreConnectEvent(@NotNull AdaptedServer targetServer, @NotNull AdaptedPlayer adaptedPlayer) {
        this.targetServer = targetServer;
        this.adaptedPlayer = adaptedPlayer;
    }

    /**
     * Set the target server to connect the player to. (Override default behavior with a custom server)
     *
     * @param targetServer the target server (AdaptedServer)
     */
    public void setTargetServer(@NotNull AdaptedServer targetServer) {
        this.targetServer = targetServer;
    }

    /**
     * @return The target server that the player is trying to connect to
     */
    public @NotNull AdaptedServer getTargetServer() {
        return targetServer;
    }

    /**
     * Set the reason why the player should be delayed from connecting.
     *
     * @param delayStatus The reason why the player should be delayed from connecting
     */
    public void setDelayStatus(@Nullable String delayStatus) {
        this.delayStatus = delayStatus;
    }

    /**
     * @return The reason why the player should be delayed from connecting
     */
    public @Nullable String getDelayStatus() {
        return delayStatus;
    }

    /**
     * @return The player that is being connected to the server
     */
    public @NotNull AdaptedPlayer getPlayer() {
        return adaptedPlayer;
    }
}

