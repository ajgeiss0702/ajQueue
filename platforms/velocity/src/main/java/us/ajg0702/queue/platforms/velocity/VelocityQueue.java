package us.ajg0702.queue.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.commands.commands.leavequeue.LeaveCommand;
import us.ajg0702.queue.commands.commands.listqueues.ListCommand;
import us.ajg0702.queue.commands.commands.manage.ManageCommand;
import us.ajg0702.queue.commands.commands.queue.QueueCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.platforms.velocity.commands.VelocityCommand;
import us.ajg0702.queue.platforms.velocity.players.VelocityPlayer;
import us.ajg0702.queue.platforms.velocity.server.VelocityServerBuilder;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import us.ajg0702.queue.platforms.velocity.server.VelocityServer;

@Plugin(
        id = "ajqueue",
        name = "ajQueue",
        version = "@VERSION@",
        url = "https://ajg0702.us",
        description = "Queue for servers",
        authors = {"ajgeiss0702"}
)

public class VelocityQueue  {
    final ProxyServer proxyServer;
    final VelocityLogger logger;

    QueueMain main;

    final File dataFolder;

    @Inject
    public VelocityQueue(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataFolder) {
        this.proxyServer = proxyServer;
        this.logger = new VelocityLogger(logger);

        this.dataFolder = dataFolder.toFile();
    }

    List<IBaseCommand> commands;

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent e) {
        main = new QueueMain(
                logger,
                new VelocityMethods(this, proxyServer, logger),
                dataFolder
        );
        main.setServerBuilder(new VelocityServerBuilder(main, proxyServer));

        commands = Arrays.asList(
                new QueueCommand(main),
                new LeaveCommand(main),
                new ListCommand(main),
                new ManageCommand(main)
        );

        CommandManager commandManager = proxyServer.getCommandManager();


        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.create("ajqueue", "tospigot"));
        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("ajqueue:toproxy"));


        for(IBaseCommand command : commands) {
            commandManager.register(
                    commandManager.metaBuilder(command.getName())
                    .aliases(command.getAliases().toArray(new String[]{}))
                    .build(),
                    new VelocityCommand(main, (BaseCommand) command)
            );
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent e) {
        main.shutdown();
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {

        if(e.getIdentifier().getId().equals("ajqueue:tospigot")) {
            e.setResult(PluginMessageEvent.ForwardResult.handled());
            return;
        }
        if(!e.getIdentifier().getId().equals("ajqueue:toproxy")) return;
        e.setResult(PluginMessageEvent.ForwardResult.handled());

        if(!(e.getTarget() instanceof Player)) return;

        main.getEventHandler().handleMessage(new VelocityPlayer((Player) e.getTarget()), e.getData());
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    public void onJoin(ServerPostConnectEvent e) {
        if(e.getPreviousServer() == null) { // only run if the player just joined
            main.getEventHandler().onPlayerJoin(new VelocityPlayer(e.getPlayer()));
        }
        main.getEventHandler().onPlayerJoinServer(new VelocityPlayer(e.getPlayer()));
    }

    @Subscribe
    public void onLeave(DisconnectEvent e) {
        main.getEventHandler().onPlayerLeave(new VelocityPlayer(e.getPlayer()));
    }

    @Subscribe
    public void onKick(KickedFromServerEvent e) {
        if(!e.getPlayer().getCurrentServer().isPresent()) return; // if the player is kicked on initial join, we dont care
        Optional<Component> reasonOptional = e.getServerKickReason();
        main.getEventHandler().onServerKick(
                new VelocityPlayer(e.getPlayer()),
                new VelocityServer(e.getServer()),
                reasonOptional.orElseGet(() -> Component.text("Proxy lost connection")),
                // According to Tux on discord, velocity doesnt give a reason when the proxy loses connection to the connected server
                e.kickedDuringServerConnect()
        );
    }

}
