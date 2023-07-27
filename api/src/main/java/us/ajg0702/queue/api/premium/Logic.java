package us.ajg0702.queue.api.premium;

import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.utils.common.Config;

@SuppressWarnings({"SameReturnValue", "unused"})

public interface Logic {
    /**
     * Returns if the plugin is premium or not
     * @return True if premium, false if not
     */
    boolean isPremium();

    /**
     * The priority logic that is executed if the plugin is premium.
     *
     * @param queueServer The server/group name that is being queued for
     * @param player      The player that is being queued
     * @param server      The server/group name that is being queued for
     */
    QueuePlayer priorityLogic(QueueServer queueServer, AdaptedPlayer player, AdaptedServer server);

    /**
     * The logic for checking if a player has been disconnected for too long
     * @param player The player to check
     * @return true if the player has been disconnected for too long and should be removed from the queue
     */
    boolean playerDisconnectedTooLong(QueuePlayer player);

    /**
     * Gets the permissionGetter. Only available on ajQueuePlus
     * @return the permission getter
     */
    PermissionGetter getPermissionGetter();

    int getHighestPriority(QueueServer queueServer, AdaptedServer server, AdaptedPlayer player);

    static int getUnJoinablePriorities(QueueServer queueServer, AdaptedServer server, AdaptedPlayer player) {
        Config config = AjQueueAPI.getInstance().getConfig();
        int highest = 0;

        int whitelistedPriority = config.getInt("give-whitelisted-players-priority");
        int bypassPausedPriority = config.getInt("give-pausedbypass-players-priority");
        int fulljoinPriority = config.getInt("give-fulljoin-players-priority");

        if(whitelistedPriority > 0) {
            if(server.isWhitelisted() && server.getWhitelistedPlayers().contains(player.getUniqueId())) {
                highest = whitelistedPriority;
            }
        }

        if(bypassPausedPriority > 0) {
            if(queueServer.isPaused() && (player.hasPermission("ajqueue.bypasspaused"))) {
                highest = Math.max(highest, bypassPausedPriority);
            }
        }

        if(fulljoinPriority > 0) {
            if(server.isFull() && (server.canJoinFull(player) ||
                    (
                            player.hasPermission("ajqueue.make-room") &&
                                    AjQueueAPI.getInstance().getConfig().getBoolean("enable-make-room-permission")
                    )
            )) {
                highest = Math.max(highest, fulljoinPriority);
            }
        }


        return highest;
    }

    boolean hasAnyBypass(AdaptedPlayer player, String server);
}
