package us.ajg0702.queue.platforms.velocity;

import com.velocitypowered.api.proxy.ProxyServer;
import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

import java.util.logging.Logger;

public class PlatformMethodImpl implements PlatformMethods {

    ProxyServer proxyServer;
    Logger logger;

    public PlatformMethodImpl(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void sendJoinQueueChannelMessages(QueueServer queueServer, QueuePlayer queuePlayer) {
        AdaptedPlayer player = queuePlayer.getPlayer();
        if(player == null) return;

    }

    @Override
    public void sendPluginMessage(AdaptedPlayer player, String channel, String... data) {

    }
}
