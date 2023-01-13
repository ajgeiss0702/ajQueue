package us.ajg0702.queue.api.commands;

import net.kyori.adventure.audience.Audience;
import us.ajg0702.queue.api.util.Handle;

import java.util.UUID;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public interface ICommandSender extends Handle, Audience {
    boolean hasPermission(String permission);
    boolean isPlayer();

    /**
     * @throws IllegalStateException if the sender is not a player
     */
    UUID getUniqueId() throws IllegalStateException;
}
