package us.ajg0702.queue.platforms.velocity.players;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements AdaptedPlayer, Audience {

    final Player handle;

    public VelocityPlayer(Player player) {
        handle = player;
    }

    @Override
    public boolean isConnected() {
        return handle.isActive();
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        handle.sendMessage(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
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
        handle.createConnectionRequest((RegisteredServer) server.getHandle()).connect().thenAcceptAsync(
                result -> {
                    if(!result.isSuccessful()) {
                        QueueMain.getInstance().getEventHandler().onServerKick(
                                this,
                                server,
                                result.getReasonComponent().orElseGet(() -> Component.text("Connection failed")),
                                false
                        );
                    }
                }
        );
    }

    @Override
    public String getName() {
        return handle.getUsername();
    }

    @Override
    public List<String> getPermissions() {
        throw new IllegalStateException("AdaptedPlayer#getPermissions cannot be used on velocity");
    }

    @Override
    public Player getHandle() {
        return handle;
    }
}
