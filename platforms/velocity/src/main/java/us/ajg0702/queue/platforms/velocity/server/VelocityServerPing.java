package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import us.ajg0702.queue.api.server.AdaptedServerPing;

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

    int add = 0;

    @Override
    public int getPlayerCount() {
        return handle.getPlayers().map(ServerPing.Players::getOnline).orElse(0)+add;
    }

    @Override
    public int getMaxPlayers() {
        return handle.getPlayers().map(ServerPing.Players::getMax).orElse(0);
    }

    @Override
    public void addPlayer() {
        add++;
    }

    @Override
    public ServerPing getHandle() {
        return handle;
    }
}
