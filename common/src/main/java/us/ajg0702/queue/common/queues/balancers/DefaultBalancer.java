package us.ajg0702.queue.common.queues.balancers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.GenUtils;

import java.util.HashMap;
import java.util.List;

public class DefaultBalancer implements Balancer {

    private final QueueServer server;
    private final QueueMain main;
    public DefaultBalancer(QueueServer server, QueueMain main) {
        this.server = server;
        this.main = main;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        List<AdaptedServer> servers = server.getServers();
        AdaptedServer selected = null;
        int selectednum = 0;
        if(servers.size() == 1) {
            selected = servers.get(0);
        } else {
            for(AdaptedServer sv : servers) {
                if(!sv.isOnline()) continue;
                int online = sv.getPlayerCount();
                if(selected == null) {
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
