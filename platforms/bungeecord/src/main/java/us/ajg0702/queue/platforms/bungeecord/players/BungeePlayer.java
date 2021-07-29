package us.ajg0702.queue.platforms.bungeecord.players;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.BaseComponentSerializer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.platforms.bungeecord.BungeeQueue;
import us.ajg0702.queue.platforms.bungeecord.server.BungeeServer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BungeePlayer implements AdaptedPlayer, Audience {

    final ProxiedPlayer handle;

    public BungeePlayer(ProxiedPlayer player) {
        handle = player;
    }

    @Override
    public boolean isConnected() {
        return handle.isConnected();
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        BungeeQueue.adventure().player(handle).sendMessage(message);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        BungeeQueue.adventure().player(handle).sendActionBar(message);
    }

    @Override
    public void sendMessage(String message) {
        if(message.isEmpty()) return;
        BungeeQueue.adventure().player(handle).sendMessage(Component.text(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public String getServerName() {
        return handle.getServer().getInfo().getName();
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public void connect(AdaptedServer server) {
        handle.connect(((BungeeServer) server).getHandle());
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public List<String> getPermissions() {
        return new ArrayList<>(handle.getPermissions());
    }

    @Override
    public ProxiedPlayer getHandle() {
        return handle;
    }
}
