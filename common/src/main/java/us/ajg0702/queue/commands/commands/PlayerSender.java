package us.ajg0702.queue.commands.commands;

import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;

import java.util.UUID;

public class PlayerSender implements ICommandSender {

    final AdaptedPlayer handle;

    public PlayerSender(AdaptedPlayer handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        return handle.hasPermission(permission);
    }

    @Override
    public boolean isPlayer() {
        return true;
    }

    @Override
    public UUID getUniqueId() throws IllegalStateException {
        return handle.getUniqueId();
    }

    @Override
    public AdaptedPlayer getHandle() {
        return handle;
    }
}
