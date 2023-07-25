package us.ajg0702.queue.api;

import us.ajg0702.queue.api.players.AdaptedPlayer;

public interface ServerTimeManager {
    /**
     * Gets the time that the player specified was last seen switching servers
     * @param player The player to check
     * @return The time that they last switched servers, in miliseconds since midnight, January 1, 1970, UTC
     */
    long getLastServerChange(AdaptedPlayer player);
}
