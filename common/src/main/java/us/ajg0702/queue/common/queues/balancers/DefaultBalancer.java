package us.ajg0702.queue.common.queues.balancers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.GenUtils;

import java.util.HashMap;

public class DefaultBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public DefaultBalancer(QueueServer server, QueueMain main) {
        this.server = server;
        this.main = main;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        HashMap<AdaptedServer, AdaptedServerPing> serverInfos = server.getLastPings();
        AdaptedServer selected = null;
        int selectednum = 0;
        if(serverInfos.keySet().size() == 1) {
            selected = serverInfos.keySet().iterator().next();
        } else {
            for(AdaptedServer si : serverInfos.keySet()) {
                AdaptedServerPing sp = serverInfos.get(si);
                if(sp == null) continue;
                int online = sp.getPlayerCount();
                if(selected == null) {
                    selected = si;
                    selectednum = online;
                    continue;
                }
                if(selectednum > online && main.getQueueManager().findServer(si.getName()).isJoinable(player)) {
                    selected = si;
                    selectednum = online;
                }
            }
        }
        if(selected == null && serverInfos.size() > 0) {
            selected = serverInfos.keySet().iterator().next();
        }
        if(selected == null) {
            main.getLogger().warning("Unable to find ideal server, using random server from group.");
            int r = GenUtils.randomInt(0, server.getServers().size()-1);
            selected = server.getServers().get(r);
        }
        return selected;
    }
}
