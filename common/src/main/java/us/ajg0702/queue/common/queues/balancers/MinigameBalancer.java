package us.ajg0702.queue.common.queues.balancers;

import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MinigameBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public MinigameBalancer(QueueServer server, QueueMain main) {
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
        List<AdaptedServer> servers = server.getServers();
        if(servers.size() == 1) {
            return servers.get(0);
        } else {

            List<AdaptedServer> svs = new ArrayList<>(servers);
            svs.sort(Comparator.comparingInt(o -> ((AdaptedServer)o).getPlayerCount()).reversed());

            for(AdaptedServer si : svs) {
                if(!si.isOnline()) continue;
                if(si.equals(alreadyConnected)) continue;
                int online = si.getPlayerCount();
                int max = si.getMaxPlayers();
                if(online < max && si.isJoinable(player)) {
                    return si;
                }
            }
            return svs.get(svs.size()-1);
        }
    }
}
