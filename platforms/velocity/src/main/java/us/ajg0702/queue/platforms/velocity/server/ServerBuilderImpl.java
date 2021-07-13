package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import us.ajg0702.queue.api.server.ServerBuilder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.queues.QueueServerImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ServerBuilderImpl implements ServerBuilder {

    private final ProxyServer proxyServer;
    private final QueueMain main;
    public ServerBuilderImpl(QueueMain main, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.main = main;
    }

    @Override
    public List<QueueServer> buildServers() {
        List<QueueServer> result = new ArrayList<>();
        Collection<RegisteredServer> servers = proxyServer.getAllServers();

        for(RegisteredServer server : servers) {
            AdaptedServer adaptedServer = new VelocityServer(server);
            result.add(new QueueServerImpl(adaptedServer.getName(), main, adaptedServer));
        }

        return result;
    }

    @Override
    public AdaptedServer getServer(String name) {
        Optional<RegisteredServer> serverOptional = proxyServer.getServer(name);
        if(!serverOptional.isPresent()) return null;
        return new VelocityServer(serverOptional.get());
    }

    @Override
    public QueueServer buildGroup(String name, List<AdaptedServer> servers) {
        return new QueueServerImpl(name, main, servers);
    }
}
