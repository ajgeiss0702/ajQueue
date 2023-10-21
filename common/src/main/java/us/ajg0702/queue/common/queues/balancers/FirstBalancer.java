package us.ajg0702.queue.common.queues.balancers;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;

public class FirstBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public FirstBalancer(QueueServer server, QueueMain main) {
        this.server = server;
        this.main = main;
    }

    @Override
    public AdaptedServer getIdealServer(@Nullable AdaptedPlayer player) {
        AdaptedServer alreadyConnected;
        if(player == null) {
            alreadyConnected = null;
        } else {
            alreadyConnected = player.getCurrentServer();
        }
        for (AdaptedServer sv : server.getServers()) {
            if(!sv.isOnline()) continue;
            if(sv.equals(alreadyConnected)) continue;
            if(!sv.isJoinable(player)) continue;
            return sv;
        }

        // If all servers are unavailable, just select the first one
        return server.getServers().get(0);
    }
}
