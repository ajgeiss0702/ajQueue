package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;

public class QueueManagerImpl implements QueueManager {
    @Override
    public boolean addToQueue(AdaptedPlayer player, QueueServer server) {
        return false;
    }

    @Override
    public boolean addToQueue(AdaptedPlayer player, String serverName) {
        return false;
    }

    @Override
    public ImmutableList<QueueServer> getServers() {
        return null;
    }

    @Override
    public ImmutableList<String> getServerNames() {
        return null;
    }
}
