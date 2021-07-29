package us.ajg0702.queue.platforms.bungeecord;

import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import us.ajg0702.queue.api.commands.IBaseCommand;
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
import us.ajg0702.queue.platforms.bungeecord.server.BungeeServerBuilder;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class BungeeQueue extends Plugin implements Listener {

    private QueueMain main;

    List<IBaseCommand> commands;

    @Override
    public void onEnable() {
        QueueLogger logger = new BungeeLogger(getLogger());
        File dataFolder = getDataFolder();

        main = new QueueMain(
                logger,
                new BungeeMethods(this, getProxy(), logger),
                dataFolder
        );
        main.setServerBuilder(new BungeeServerBuilder(main, getProxy()));

        getProxy().registerChannel("ajqueue:tospigot");
        getProxy().registerChannel("ajqueue:tobungee");

        commands = Arrays.asList(
                new QueueCommand(main),
                new LeaveCommand(main),
                new ListCommand(main),
                new ManageCommand(main)
        );

        for(IBaseCommand command : commands) {
            getProxy().getPluginManager()
                    .registerCommand(this, new BungeeCommand((BaseCommand) command));
        }

        getProxy().getPluginManager().registerListener(this, this);

        adventure = BungeeAudiences.create(this);

    }

    private static BungeeAudiences adventure;

    public static @NonNull BungeeAudiences adventure() {
        if(adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
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

        main.getEventHandler().handleMessage(new BungeePlayer((ProxiedPlayer) e.getReceiver()), e.getData());
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        main.getEventHandler().onPlayerJoin(new BungeePlayer(e.getPlayer()));
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent e) {
        main.getEventHandler().onPlayerJoinServer(new BungeePlayer(e.getPlayer()));
    }

    @EventHandler
    public void onLeave(PlayerDisconnectEvent e) {
        main.getEventHandler().onPlayerLeave(new BungeePlayer(e.getPlayer()));
    }

    @EventHandler
    public void onKick(ServerKickEvent e) {
        if(!e.getPlayer().isConnected()) return;
        if(e.getPlayer().getServer() == null) return; // if the player is kicked on initial join, we dont care
        Component reason = BungeeComponentSerializer.get().deserialize(e.getKickReasonComponent());
        main.getEventHandler().onServerKick(
                new BungeePlayer(e.getPlayer()),
                new BungeeServer(e.getCancelServer()),
                reason,
                false
        );
    }
}
