package us.ajg0702.queue.platforms.bungeecord.server;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.ServerBuilder;

import java.util.ArrayList;
import java.util.List;

public class BungeeServerBuilder implements ServerBuilder {

    private final ProxyServer proxyServer;

    public BungeeServerBuilder(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public List<AdaptedServer> getServers() {
        List<AdaptedServer> result = new ArrayList<>();

        proxyServer.getServers().forEach((s, serverInfo) -> result.add(new BungeeServer(serverInfo)));

        return result;
    }

    @Override
    public AdaptedServer getServer(String name) {
        ServerInfo server = proxyServer.getServerInfo(name);
        if(server == null) return null;
        return new BungeeServer(server);
    }
}
