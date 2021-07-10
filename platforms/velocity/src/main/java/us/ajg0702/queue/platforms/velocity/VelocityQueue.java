package us.ajg0702.queue.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import us.ajg0702.queue.common.QueueMain;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
        id = "ajqueue",
        name = "ajQueue",
        version = "@VERSION@",
        url = "https://ajg0702.us",
        description = "Queue for servers",
        authors = {"ajgeiss0702"}
)

public class VelocityQueue  {
    ProxyServer proxyServer;
    Logger logger;

    QueueMain main;

    File dataFolder;

    @Inject
    public VelocityQueue(ProxyServer proxyServer, Logger logger, Path dataFolder) {
        this.proxyServer = proxyServer;
        this.logger = logger;

        this.dataFolder = dataFolder.toFile();
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        main = new QueueMain(
                logger,
                new ServerBuilderImpl(proxyServer),
                new PlatformMethodImpl(proxyServer, logger),
                dataFolder
        );
    }
}
