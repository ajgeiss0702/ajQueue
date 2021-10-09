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
        HashMap<AdaptedServer, AdaptedServerPing> serverInfos = server.getLastPings();
        if(serverInfos.keySet().size() == 1) {
            return serverInfos.keySet().iterator().next();
        } else {

            List<Map.Entry<AdaptedServer, AdaptedServerPing>> servers = new ArrayList<>(serverInfos.entrySet());
            servers.sort(Comparator.comparingInt(o -> {
                @SuppressWarnings("unchecked")
                Map.Entry<AdaptedServer, AdaptedServerPing> e = (Map.Entry<AdaptedServer, AdaptedServerPing>) o;
                return e.getValue().getPlayerCount();
            }).reversed());
            LinkedHashMap<AdaptedServer, AdaptedServerPing> sortedServers = new LinkedHashMap<>();
            for(Map.Entry<AdaptedServer, AdaptedServerPing> entry : servers) {
                sortedServers.put(entry.getKey(), entry.getValue());
            }

            for(AdaptedServer si : sortedServers.keySet()) {
                AdaptedServerPing sp = sortedServers.get(si);
                if(sp == null) continue;
                int online = sp.getPlayerCount();
                int max = sp.getMaxPlayers();
                if(online < max) {
                    return si;
                }
            }
            return new ArrayList<AdaptedServer>(sortedServers.keySet().size()).get(sortedServers.keySet().size()-1);
        }
    }
}
