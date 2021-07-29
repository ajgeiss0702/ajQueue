package us.ajg0702.queue.platforms.bungeecord.server;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.ServerBuilder;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.queues.QueueServerImpl;

import java.util.*;

public class BungeeServerBuilder implements ServerBuilder {

    private final ProxyServer proxyServer;
    private final QueueMain main;
    public BungeeServerBuilder(QueueMain main, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.main = main;
    }

    @Override
    public List<QueueServer> buildServers() {
        List<QueueServer> result = new ArrayList<>();
        Map<String, ServerInfo> servers = proxyServer.getServers();

        for(String serverName : servers.keySet()) {
            ServerInfo serverInfo = servers.get(serverName);
            AdaptedServer adaptedServer = new BungeeServer(serverInfo);
            result.add(new QueueServerImpl(adaptedServer.getName(), main, adaptedServer));
        }

        return result;
    }

    @Override
    public AdaptedServer getServer(String name) {
        ServerInfo server = proxyServer.getServerInfo(name);
        if(server == null) return null;
        return new BungeeServer(server);
    }
}
