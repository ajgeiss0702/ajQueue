package us.ajg0702.queue.platforms.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import us.ajg0702.queue.api.ServerBuilder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerBuilderImpl implements ServerBuilder {

    private final ProxyServer proxyServer;
    public ServerBuilderImpl(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public List<QueueServer> getServers() {
        List<QueueServer> result = new ArrayList<>();
        Collection<RegisteredServer> servers = proxyServer.getAllServers();



        return result;
    }

    @Override
    public QueueServer buildGroup(String name, List<AdaptedServer> servers) {
        return null;
    }
}
