package us.ajg0702.queue.api.commands;

import net.kyori.adventure.audience.Audience;
import us.ajg0702.queue.api.util.Handle;

public interface ICommandSender extends Handle, Audience {
    boolean hasPermission(String permission);
    boolean isPlayer();
}
