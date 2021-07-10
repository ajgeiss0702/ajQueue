package us.ajg0702.queue.api.server;

import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.util.Handle;

public interface AdaptedServerPing extends Handle {
    /**
     * Gets the component of the description (aka MOTD)
     * @return A compoent of the description
     */
    Component getDescriptionComponent();

    /**
     * Gets the description stripped of any color or styling
     * @return The description, but no colors
     */
    String getPlainDescription();

    /**
     * Gets the number of players currently online
     * @return The number of players online
     */
    int getPlayerCount();

    /**
     * Gets the maximum number of players that can join.
     * @return The maximum number of players that can join
     */
    int getMaxPlayers();
}
