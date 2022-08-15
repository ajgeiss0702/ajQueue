package us.ajg0702.queue.common.queues.balancers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;

import java.util.*;

public class MinigameBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public MinigameBalancer(QueueServer server, QueueMain main) {
        this.server = server;
        this.main = main;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        List<AdaptedServer> servers = server.getServers();
        if(servers.size() == 1) {
            return servers.get(0);
        } else {

            List<AdaptedServer> svs = new ArrayList<>(servers);
            svs.sort(Comparator.comparingInt(o -> ((AdaptedServer)o).getPlayerCount()).reversed());

            for(AdaptedServer si : svs) {
                if(!si.isOnline()) continue;
                int online = si.getPlayerCount();
                int max = si.getMaxPlayers();
                if(online < max) {
                    return si;
                }
            }
            return svs.get(svs.size()-1);
        }
    }
}
