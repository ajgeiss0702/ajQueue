package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.ServerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VelocityServerBuilder implements ServerBuilder {

    private final ProxyServer proxyServer;

    public VelocityServerBuilder(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public List<AdaptedServer> getServers() {
        List<AdaptedServer> result = new ArrayList<>();

        proxyServer.getAllServers().forEach(registeredServer -> result.add(new VelocityServer(registeredServer)));

        return result;
    }

    @SuppressWarnings("OptionalIsPresent")
    @Override
    public AdaptedServer getServer(String name) {
        Optional<RegisteredServer> serverOptional = proxyServer.getServer(name);
        if(!serverOptional.isPresent()) return null;
        return new VelocityServer(serverOptional.get());
    }
}
