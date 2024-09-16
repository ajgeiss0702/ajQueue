package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import us.ajg0702.queue.api.server.AdaptedServerPing;

public class VelocityServerPing implements AdaptedServerPing {

    private final ServerPing handle;
    private final long sent;
    public VelocityServerPing(ServerPing handle, long sent) {
        this.handle = handle;
        this.sent = sent;
    }

    @Override
    public Component getDescriptionComponent() {
        return handle.getDescriptionComponent();
    }

    @Override
    public String getPlainDescription() {
        Component description = handle.getDescriptionComponent();
        if(description == null) return null;
        return PlainTextComponentSerializer.plainText().serialize(description);
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
    public long getFetchedTime() {
        return sent;
    }

    @Override
    public ServerPing getHandle() {
        return handle;
    }
}
