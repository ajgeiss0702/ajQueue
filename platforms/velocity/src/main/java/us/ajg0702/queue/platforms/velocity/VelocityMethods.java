package us.ajg0702.queue.platforms.velocity;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import us.ajg0702.queue.api.PlatformMethods;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.commands.ICommandSender;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.queue.platforms.velocity.players.VelocityPlayer;
import us.ajg0702.queue.platforms.velocity.server.VelocityServer;

import java.util.*;

@SuppressWarnings("OptionalIsPresent")
public class VelocityMethods implements PlatformMethods {

    final ProxyServer proxyServer;
    final QueueLogger logger;
    final VelocityQueue plugin;

    public VelocityMethods(VelocityQueue plugin, ProxyServer proxyServer, QueueLogger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.plugin = plugin;
    }

    @Override
    public void sendPluginMessage(AdaptedPlayer player, String channel, String... data) {
        if(player == null) return;
        Player velocityPlayer = ((VelocityPlayer) player).getHandle();
        @SuppressWarnings("UnstableApiUsage") ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF( channel );
        out.writeUTF(player.getName());
        for(String s : data) {
            out.writeUTF( s );
        }
        Optional<ServerConnection> server = velocityPlayer.getCurrentServer();
        if(server.isEmpty()) {
            throw new IllegalStateException("No server to send data to");
        }
        server.get().sendPluginMessage(MinecraftChannelIdentifier.from("ajqueue:tospigot"), out.toByteArray());
    }

    @Override
    public AdaptedPlayer senderToPlayer(ICommandSender sender) {
        if(sender instanceof PlayerSender) {
            return ((PlayerSender) sender).getHandle();
        }
        return new VelocityPlayer((Player) sender.getHandle());
    }

    @Override
    public String getPluginVersion() {
        Optional<PluginContainer> plugin = proxyServer.getPluginManager().getPlugin("ajqueue");
        if(plugin.isEmpty()) return "?E";
        Optional<String> version = plugin.get().getDescription().getVersion();
        return version.orElse("?V");
    }

    @Override
    public List<AdaptedPlayer> getOnlinePlayers() {
        List<AdaptedPlayer> players = new ArrayList<>();
        for(Player player : proxyServer.getAllPlayers()) {
            players.add(new VelocityPlayer(player));
        }
        return players;
    }

    @Override
    public List<String> getPlayerNames(boolean lowercase) {
        List<String> players = new ArrayList<>();
        for(Player player : proxyServer.getAllPlayers()) {
            if(lowercase) {
                players.add(player.getUsername().toLowerCase(Locale.ROOT));
            } else {
                players.add(player.getUsername());
            }
        }
        return players;
    }

    @Override
    public AdaptedPlayer getPlayer(String name) {
        Optional<Player> player = proxyServer.getPlayer(name);
        if(player.isEmpty()) {
            return null;
        }
        return new VelocityPlayer(player.get());
    }

    @Override
    public AdaptedPlayer getPlayer(UUID uuid) {
        Optional<Player> player = proxyServer.getPlayer(uuid);
        if(player.isEmpty()) {
            return null;
        }
        return new VelocityPlayer(player.get());
    }

    @Override
    public List<String> getServerNames() {
        List<String> names = new ArrayList<>();
        for(RegisteredServer server : proxyServer.getAllServers()) {
            names.add(server.getServerInfo().getName());
        }
        return names;
    }

    @Override
    public String getImplementationName() {
        return "velocity";
    }

    @Override
    public List<IBaseCommand> getCommands() {
        return plugin.commands;
    }

    @Override
    public boolean hasPlugin(String pluginName) {
        return proxyServer.getPluginManager().getPlugin(pluginName.toLowerCase(Locale.ROOT)).isPresent();
    }

    @Override
    public AdaptedServer getServer(String name) {
        List<? extends AdaptedServer> servers = getServers();
        for (AdaptedServer server : servers) {
            if(server.getName().equals(name)) return server;
        }
        return null;
    }

    List<VelocityServer> serverList = new ArrayList<>();


    @Override
    public List<? extends AdaptedServer> getServers() {

        for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
            boolean found = false;
            for(VelocityServer sv : new ArrayList<>(serverList)) {
                if(sv.getHandle().equals(registeredServer)) {
                    found = true;
                    break;
                }
            }
            if(found) continue;

            Debug.info("Added "+registeredServer.getServerInfo().getName());
            serverList.add(new VelocityServer(registeredServer));
        }

        for(VelocityServer sv : new ArrayList<>(serverList)) {
            boolean found = false;
            for (RegisteredServer registeredServer : proxyServer.getAllServers()) {
                if(sv.getHandle().equals(registeredServer)) {
                    found = true;
                    break;
                }
            }
            if(found) continue;

            Debug.info("Removed "+sv.getName());
            serverList.remove(sv);
        }

        return serverList;
    }

    @Override
    public String getProtocolName(int protocol) {
        String version = ProtocolVersion.getProtocolVersion(protocol).getMostRecentSupportedVersion();
        if(version.equalsIgnoreCase("Unknown")) {
            return protocol+"";
        }
        return version;
    }
}
