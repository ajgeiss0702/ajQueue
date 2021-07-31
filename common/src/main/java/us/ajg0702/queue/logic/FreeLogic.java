package us.ajg0702.queue.logic;

import us.ajg0702.queue.api.Logic;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

public class FreeLogic implements Logic {
    @Override
    public boolean isPremium() {
        return false;
    }

    @Override
    public QueuePlayer priorityLogic(QueueServer server, AdaptedPlayer player) {
        return null;
    }

    @Override
    public boolean playerDisconnectedTooLong(QueuePlayer player) {
        return player.getMaxOfflineTime() < player.getTimeSinceOnline()*1000;
    }
}
