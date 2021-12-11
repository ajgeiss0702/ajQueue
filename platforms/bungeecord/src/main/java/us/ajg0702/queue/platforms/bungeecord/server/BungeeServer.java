package us.ajg0702.queue.platforms.bungeecord.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.platforms.bungeecord.players.BungeePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BungeeServer implements AdaptedServer {

    final ServerInfo handle;
    final BungeeServerInfo serverInfo;

    public BungeeServer(ServerInfo handle) {
        this.handle = handle;
        serverInfo = new BungeeServerInfo(handle);
    }

    @Override
    public AdaptedServerInfo getServerInfo() {
        return serverInfo;
    }

    @Override
    public String getName() {
        return serverInfo.getName();
    }

    @Override
    public CompletableFuture<AdaptedServerPing> ping() {
        CompletableFuture<AdaptedServerPing> future = new CompletableFuture<>();
        handle.ping((pp, error) -> {
            if(error != null) {
                future.completeExceptionally(error);
                return;
            }

            future.complete(new BungeeServerPing(pp));
        });
        return future;
    }

    @Override
    public boolean canAccess(AdaptedPlayer player) {
        return handle.canAccess((ProxiedPlayer) player.getHandle());
    }

    @Override
    public List<AdaptedPlayer> getPlayers() {
        List<AdaptedPlayer> players = new ArrayList<>();
        handle.getPlayers().forEach(pp -> players.add(new BungeePlayer(pp)));
        return players;
    }

    @Override
    public ServerInfo getHandle() {
        return handle;
    }
}
