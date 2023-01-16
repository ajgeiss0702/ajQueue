package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;

public class FreeLogic implements Logic {
    @Override
    public boolean isPremium() {
        return false;
    }

    @Override
    public QueuePlayer priorityLogic(QueueServer queueServer, AdaptedPlayer player, AdaptedServer server) {
        return null;
    }

    @Override
    public boolean playerDisconnectedTooLong(QueuePlayer player) {
        return player.getTimeSinceOnline() > player.getMaxOfflineTime()*1000L;
    }

    @Override
    public PermissionGetter getPermissionGetter() {
        return null;
    }

    @Override
    public boolean hasAnyBypass(AdaptedPlayer player, String server) {
        return false;
    }
}
