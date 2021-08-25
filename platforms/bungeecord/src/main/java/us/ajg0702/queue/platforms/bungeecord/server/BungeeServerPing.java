package us.ajg0702.queue.platforms.bungeecord.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import us.ajg0702.queue.api.server.AdaptedServerPing;

public class BungeeServerPing implements AdaptedServerPing {

    final ServerPing handle;

    public BungeeServerPing(ServerPing handle) {
        this.handle = handle;
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

    @Override
    public int getPlayerCount() {
        return handle.getPlayers().getOnline();
    }

    @Override
    public int getMaxPlayers() {
        return handle.getPlayers().getMax();
    }

    @Override
    public ServerPing getHandle() {
        return handle;
    }
}
