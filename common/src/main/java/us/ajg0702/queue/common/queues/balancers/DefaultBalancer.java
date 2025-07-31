package us.ajg0702.queue.common.queues.balancers;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.GenUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class DefaultBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public DefaultBalancer(QueueServer server, QueueMain main) {
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
        List<AdaptedServer> servers = server.getServers();
        AdaptedServer selected = null;
        int selectednum = 0;
        if(servers.size() == 1) {
            selected = servers.get(0);
        } else {
            for(AdaptedServer sv : servers) {
                if(!sv.isOnline()) continue;
                if(sv.equals(alreadyConnected)) continue;
                int online = sv.getPlayerCount();
                if(selected == null) {
                    selected = sv;
                    selectednum = online;
                    continue;
                }
                if(!selected.isJoinable(player) && sv.isJoinable(player)) {
                    if(protocol != null) {
                        List<QueueServer> queues = main.getQueueManager().getServers();
                        AdaptedServer finalSelected = selected;
                        Optional<QueueServer> selectedQueueServer = queues.stream()
                                .filter(s -> Objects.equals(s.getName(), finalSelected.getName()))
                                .findAny();
                        Optional<QueueServer> svQueueServer = queues.stream()
                                .filter(s -> Objects.equals(s.getName(), sv.getName()))
                                .findAny();
                        if(selectedQueueServer.isPresent() && svQueueServer.isPresent()) {
                            QueueServer selectedQueue = selectedQueueServer.get();
                            QueueServer svQueue = svQueueServer.get();
                            if(selectedQueue.isSupportedProtocol(protocol) && !svQueue.isSupportedProtocol(protocol)) {
                                continue;
                            }
                        }
                    }
                    selected = sv;
                    selectednum = online;
                    continue;
                }
                if(selectednum > online && sv.isJoinable(player)) {
                    selected = sv;
                    selectednum = online;
                }
            }
        }
        if(selected == null && servers.size() > 0) {
            selected = servers.get(0);
        }
        if(selected == null) {
            main.getLogger().warning("Unable to find ideal server, using random server from group.");
            int r = GenUtils.randomInt(0, server.getServers().size()-1);
            selected = server.getServers().get(r);
        }
        return selected;
    }
}
