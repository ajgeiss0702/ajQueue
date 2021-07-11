package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import us.ajg0702.queue.api.server.AdaptedServerPing;

import java.util.Optional;

public class VelocityServerPing implements AdaptedServerPing {

    private final ServerPing handle;
    public VelocityServerPing(ServerPing handle) {
        this.handle = handle;
    }

    @Override
    public Component getDescriptionComponent() {
        return handle.getDescriptionComponent();
    }

    @Override
    public String getPlainDescription() {
        return PlainTextComponentSerializer.plainText().serialize(handle.getDescriptionComponent());
    }

    @Override
    public int getPlayerCount() {
        Optional<ServerPing.Players> players = handle.getPlayers();
        if(players.isEmpty()) return 0;
        return players.get().getOnline();
    }

    @Override
    public int getMaxPlayers() {
        Optional<ServerPing.Players> players = handle.getPlayers();
        if(players.isEmpty()) return 0;
        return players.get().getMax();
    }

    @Override
    public ServerPing getHandle() {
        return handle;
    }
}
