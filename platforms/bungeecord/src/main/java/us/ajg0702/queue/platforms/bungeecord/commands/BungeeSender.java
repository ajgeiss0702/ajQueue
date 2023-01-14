package us.ajg0702.queue.platforms.bungeecord.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.platforms.bungeecord.BungeeQueue;

import java.util.UUID;

public class BungeeSender implements ICommandSender {

    final CommandSender handle;

    public BungeeSender(CommandSender handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        if(permission == null) return true;
        return handle.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return handle instanceof ProxiedPlayer;
    }

    @Override
    public UUID getUniqueId() throws IllegalStateException {
        if(!(handle instanceof ProxiedPlayer)) throw new IllegalStateException("Cannot get UUID of non-player!");
        return ((ProxiedPlayer) handle).getUniqueId();
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        BungeeQueue.adventure().sender(handle).sendMessage(message);
    }

    @Override
    public CommandSender getHandle() {
        return handle;
    }
}
