package us.ajg0702.queue.api;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

public interface EventHandler {

    void handleMessage(AdaptedPlayer reciever, byte[] data);

    void onPlayerJoin(AdaptedPlayer player);

    void onPlayerLeave(AdaptedPlayer player);

    /**
     * Called when a player joins a server or switches between servers
     * @param player the player
     */
    void onPlayerJoinServer(AdaptedPlayer player);

    void onServerKick(AdaptedPlayer player, AdaptedServer from, Component reason, boolean moving);

    @Nullable
    AdaptedServer changeTargetServer(AdaptedPlayer player, AdaptedServer initialChoice);
}
