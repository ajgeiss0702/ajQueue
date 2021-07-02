package us.ajg0702.queue.api.players;

import javax.annotation.Nullable;
import java.util.UUID;

public interface QueuePlayer {
    /**
     * Returns the player's UUID
     * @return the player's UUID
     */
    UUID getUniqueId();

    /**
     * Get the player this represents.
     * Can be null because the player could not be online
     * @return The player if they are online, null otherwise
     */
    @Nullable AdaptedPlayer getPlayer();
}
