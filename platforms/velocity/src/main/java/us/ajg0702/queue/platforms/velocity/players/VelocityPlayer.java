package us.ajg0702.queue.platforms.velocity.players;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements AdaptedPlayer {

    Player handle;

    public VelocityPlayer(Player player) {
        handle = player;
    }

    @Override
    public boolean isConnected() {
        return handle.isActive();
    }

    @Override
    public void sendMessage(Component message) {
        handle.sendMessage(message);
    }

    @Override
    public void sendActionBar(Component message) {
        handle.sendActionBar(message);
    }

    @Override
    public void sendMessage(String message) {
        handle.sendMessage(Component.text().content(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public String getServerName() {
        Optional<ServerConnection> serverConnection = handle.getCurrentServer();
        if(!serverConnection.isPresent()) return "none";
        ServerConnection connection = serverConnection.get();
        return connection.getServerInfo().getName();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public void connect(AdaptedServer server) {
        handle.createConnectionRequest((RegisteredServer) server.getHandle()).connect();
    }

    @Override
    public Player getHandle() {
        return handle;
    }
}
