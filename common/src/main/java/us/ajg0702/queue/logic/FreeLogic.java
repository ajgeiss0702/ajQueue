package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.events.PriorityCalculationEvent;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.premium.PermissionGetter;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;

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
    public int getHighestPriority(QueueServer queueServer, AdaptedServer server, AdaptedPlayer player) {
        int existingPriority = player.hasPermission("ajqueue.priority") ? 1 : 0;

        PriorityCalculationEvent event = new PriorityCalculationEvent(player, existingPriority);

        QueueMain.getInstance().call(event);

        return event.getHighestPriority() > 0 ? 1 : 0;
    }

    @Override
    public boolean hasAnyBypass(AdaptedPlayer player, String server) {
        return false;
    }
}
