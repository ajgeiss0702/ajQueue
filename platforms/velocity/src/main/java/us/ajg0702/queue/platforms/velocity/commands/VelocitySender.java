package us.ajg0702.queue.platforms.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.commands.ICommandSender;

public class VelocitySender implements ICommandSender {

    final CommandSource handle;

    public VelocitySender(CommandSource handle) {
       this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        if(permission == null) return true;
        return handle.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return !(handle instanceof ConsoleCommandSource);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        if(PlainTextComponentSerializer.plainText().serialize(message).isEmpty()) return;
        handle.sendMessage(message);
    }

    @Override
    public CommandSource getHandle() {
        return handle;
    }
}
