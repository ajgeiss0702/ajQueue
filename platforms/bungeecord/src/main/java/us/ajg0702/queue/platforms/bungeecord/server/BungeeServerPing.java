package us.ajg0702.queue.platforms.bungeecord.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.server.AdaptedServerPing;

public class BungeeServerPing implements AdaptedServerPing {

    final ServerPing handle;
    private final long sent;

    public BungeeServerPing(@NotNull ServerPing handle, long sent) {
        this.handle = handle;
        this.sent = sent;
    }

    @Override
    public Component getDescriptionComponent() {
        BaseComponent[] baseComponents = new BaseComponent[1];
        baseComponents[0] = handle.getDescriptionComponent();
        return BungeeComponentSerializer.get().deserialize(baseComponents);
    }

    @Override
    public String getPlainDescription() {
        BaseComponent desc = handle.getDescriptionComponent();
        if(desc == null) return null;
        return desc.toPlainText();
    }

    int add = 0;

    @Override
    public int getPlayerCount() {
        return handle.getPlayers().getOnline()+add;
    }

    @Override
    public int getMaxPlayers() {
        return handle.getPlayers().getMax();
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
