package us.ajg0702.queue.platforms.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import us.ajg0702.queue.api.commands.ICommandSender;

public class VelocitySender implements ICommandSender {

    CommandSource handle;

    public VelocitySender(CommandSource handle) {
       this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return !(handle instanceof ConsoleCommandSource);
    }

    @Override
    public CommandSource getHandle() {
        return handle;
    }
}
