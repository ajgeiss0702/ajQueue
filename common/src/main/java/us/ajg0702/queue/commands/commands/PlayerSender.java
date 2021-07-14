package us.ajg0702.queue.commands.commands;

import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;

public class PlayerSender implements ICommandSender {

    final AdaptedPlayer handle;

    public VelocitySender(CommandSource handle) {
        this.handle = handle;
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public Object getHandle() {
        return null;
    }
}
