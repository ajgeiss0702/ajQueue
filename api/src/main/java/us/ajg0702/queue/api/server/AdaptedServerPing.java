package us.ajg0702.queue.api.server;

import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.util.Handle;

@SuppressWarnings("unused")
public interface AdaptedServerPing extends Handle {
    /**
     * Gets the component of the description (aka MOTD)
     * @return A compoent of the description
     */
    Component getDescriptionComponent();

    /**
     * Gets the description (aka MOTD) stripped of any color or styling
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

    /**
     * Temporarly adds one player to the player count
     */
    void addPlayer();

    /**
     * Returns an epoch timestamp of when this ping was <b>sent</b>.
     * @return A long of an epoch timestamp
     */
    long getFetchedTime();
}
