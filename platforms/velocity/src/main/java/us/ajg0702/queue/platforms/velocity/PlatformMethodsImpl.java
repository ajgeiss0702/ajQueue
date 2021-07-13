package us.ajg0702.queue.platforms.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.platforms.velocity.players.VelocityPlayer;

import java.util.Optional;
import java.util.logging.Logger;

public class PlatformMethodsImpl implements PlatformMethods {

    final ProxyServer proxyServer;
    final Logger logger;

    public PlatformMethodsImpl(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void sendJoinQueueChannelMessages(QueueServer queueServer, QueuePlayer queuePlayer) {
        AdaptedPlayer player = queuePlayer.getPlayer();
        if(player == null) return;
        player.sendMessage(Component.text());
    }

    @Override
    public void sendPluginMessage(AdaptedPlayer player, String channel, String... data) {

    }

    @Override
    public AdaptedPlayer senderToPlayer(ICommandSender sender) {
        return new VelocityPlayer((Player) sender.getHandle());
    }

    @Override
    public String getPluginVersion() {
        Optional<PluginContainer> plugin = proxyServer.getPluginManager().getPlugin("ajqueue");
        if(!plugin.isPresent()) return "?E";
        Optional<String> version = plugin.get().getDescription().getVersion();
        if(!version.isPresent()) return "?V";
        return version.get();
    }
}
