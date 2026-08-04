package us.ajg0702.queue.common.queues.balancers;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.ServerHealth;
import us.ajg0702.queue.common.QueueMain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        Integer protocol = player == null ? null : player.getProtocolVersion();

        // if all servers are unavailable except for one that's unhealthy,
        // we set the fallback to be the first available but unhealthy server to fall back to if all other servers are completely unavailable
        AdaptedServer fallback = null;

        for (AdaptedServer sv : server.getServers()) {
            if(!sv.isOnline()) continue;
            if(sv.equals(alreadyConnected)) continue;
            if(!sv.isJoinable(player)) continue;
            if(protocol != null) {
                Optional<QueueServer> svQueueServer = main.getQueueManager().getServers().stream()
                        .filter(s -> Objects.equals(s.getName(), sv.getName()))
                        .findAny();
                if(svQueueServer.isPresent()) {
                    QueueServer svQueue = svQueueServer.get();
                    if(!svQueue.isSupportedProtocol(protocol)) continue;
                }
            }
            if(sv.getHealthStatus() != ServerHealth.HEALTHY) {
                if(fallback == null) fallback = sv;
                continue;
            }
            return sv;
        }

        // If all servers are unavailable, use the fallback
        if(fallback != null) {
            return fallback;
        } else {
            return server.getServers().get(0);
        }
    }
}
