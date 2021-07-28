package us.ajg0702.queue.platforms.velocity.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.platforms.velocity.players.VelocityPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class VelocityServer implements AdaptedServer {

    private final RegisteredServer handle;
    public VelocityServer(RegisteredServer handle) {
        this.handle = handle;
    }

    @Override
    public AdaptedServerInfo getServerInfo() {
        return new VelocityServerInfo(handle.getServerInfo());
    }

    @Override
    public String getName() {
        return handle.getServerInfo().getName();
    }

    @Override
    public CompletableFuture<AdaptedServerPing> ping() {
        CompletableFuture<AdaptedServerPing> future = new CompletableFuture<>();
        CompletableFuture<ServerPing> serverPing = handle.ping();
        serverPing.thenRunAsync(() -> {
            AdaptedServerPing aPing = null;
            try {
                aPing = new VelocityServerPing(serverPing.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            future.complete(aPing);
        });
        return future;
    }

    @Override
    public boolean canAccess(AdaptedPlayer player) {
        return true;
    }

    @Override
    public List<AdaptedPlayer> getPlayers() {
        List<AdaptedPlayer> players = new ArrayList<>();
        for(Player player : handle.getPlayersConnected()) {
            players.add(new VelocityPlayer(player));
        }
        return players;
    }

    @Override
    public RegisteredServer getHandle() {
        return handle;
    }
}
