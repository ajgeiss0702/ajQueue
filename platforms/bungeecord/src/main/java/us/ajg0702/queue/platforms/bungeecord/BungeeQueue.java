package us.ajg0702.queue.platforms.bungeecord;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.checkerframework.checker.nullness.qual.NonNull;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.Implementation;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerInfo;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.commands.BaseCommand;
import us.ajg0702.queue.commands.commands.leavequeue.LeaveCommand;
import us.ajg0702.queue.commands.commands.listqueues.ListCommand;
import us.ajg0702.queue.commands.commands.manage.ManageCommand;
import us.ajg0702.queue.commands.commands.queue.QueueCommand;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.platforms.bungeecord.commands.BungeeCommand;
import us.ajg0702.queue.platforms.bungeecord.players.BungeePlayer;
import us.ajg0702.queue.platforms.bungeecord.server.BungeeServer;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class BungeeQueue extends Plugin implements Listener, Implementation {

    private QueueMain main;

    List<IBaseCommand> commands;

    Map<String, BungeeCommand> commandMap;

    @Override
    public void onEnable() {
        QueueLogger logger = new BungeeLogger(getLogger());
        File dataFolder = getDataFolder();

        commandMap = new HashMap<>();

        adventure = BungeeAudiences.create(this);

        main = new QueueMain(
                this,
                logger,
                new BungeeMethods(this, getProxy(), logger),
                dataFolder
        );

        getProxy().registerChannel("ajqueue:tospigot");
        getProxy().registerChannel("ajqueue:toproxy");

        commands = Arrays.asList(
                new QueueCommand(main),
                new LeaveCommand(main),
                new ListCommand(main),
                new ManageCommand(main)
        );

        for(IBaseCommand command : commands) {
            registerCommand(command);
        }

        getProxy().getPluginManager().registerListener(this, this);


        Metrics metrics = new Metrics(this, 7404);

        metrics.addCustomChart(new SimplePie("premium", () -> String.valueOf(main.getLogic().isPremium())));
        metrics.addCustomChart(new SimplePie("implementation", () -> main.getPlatformMethods().getImplementationName()));
    }

    private static BungeeAudiences adventure;

    public static @NonNull BungeeAudiences adventure() {
        if(adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider. Not loaded yet.");
        }
        return adventure;
    }

    @Override
    public void onDisable() {
        main.shutdown();
        if(adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent e) {
        if(e.getTag().equals("ajqueue:tospigot")) {
            e.setCancelled(true);
            return;
        }
        if(!e.getTag().equals("ajqueue:toproxy")) return;
        e.setCancelled(true);

        if(!(e.getReceiver() instanceof ProxiedPlayer)) return;

        ProxyServer.getInstance().getScheduler().runAsync(this, () ->
                main.getEventHandler()
                        .handleMessage(
                                new BungeePlayer((ProxiedPlayer) e.getReceiver()),
                                e.getData()
                        )
        );
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        main.getEventHandler().onPlayerJoin(new BungeePlayer(e.getPlayer()));
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        ProxyServer.getInstance().getScheduler().runAsync(this, () ->
                main.getEventHandler().onPlayerJoinServer(new BungeePlayer(e.getPlayer()))
        );

    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        ProxyServer.getInstance().getScheduler().runAsync(this, () ->
            main.getEventHandler().onPlayerLeave(new BungeePlayer(e.getPlayer()))
        );
    }

    @EventHandler
    public void onKick(ServerKickEvent e) {
        if(!e.getPlayer().isConnected()) return;
        if(e.getPlayer().getServer() == null) return; // if the player is kicked on initial join, we dont care
        ProxyServer.getInstance().getScheduler().runAsync(this, () -> {
            Component reason = BungeeComponentSerializer.get().deserialize(e.getKickReasonComponent());
            main.getEventHandler().onServerKick(
                    new BungeePlayer(e.getPlayer()),
                    new BungeeServer(e.getKickedFrom()),
                    reason,
                    false
            );
        });
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent e) {
        AdaptedServer newServer = main.getEventHandler().changeTargetServer(new BungeePlayer(e.getPlayer()), new BungeeServer(e.getTarget()));

        if(newServer == null) return;

        e.setTarget((ServerInfo) newServer.getHandle());
    }

    @Override
    public void unregisterCommand(String name) {
        BungeeCommand bungeeCommand = commandMap.get(name);
        if(bungeeCommand == null) return;
        getProxy().getPluginManager().unregisterCommand(bungeeCommand);
        commandMap.remove(name);
    }

    @Override
    public void registerCommand(IBaseCommand command) {
        BungeeCommand bungeeCommand = new BungeeCommand((BaseCommand) command);
        commandMap.put(command.getName(), bungeeCommand);
        getProxy().getPluginManager()
                .registerCommand(this, bungeeCommand);
    }

    public QueueMain getMain() {
        return main;
    }
}
