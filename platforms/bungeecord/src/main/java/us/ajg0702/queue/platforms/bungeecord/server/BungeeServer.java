package us.ajg0702.queue.platforms.bungeecord.server;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerInfo;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.api.util.QueueLogger;
import us.ajg0702.queue.platforms.bungeecord.players.BungeePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BungeeServer implements AdaptedServer {

    private final ServerInfo handle;
    private final BungeeServerInfo serverInfo;

    private AdaptedServerPing lastPing = null;
    private AdaptedServerPing lastSuccessfullPing = null;
    private long lastOffline;

    private int offlineTime = 0;

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
    public CompletableFuture<AdaptedServerPing> ping(boolean debug, QueueLogger logger) {
        CompletableFuture<AdaptedServerPing> future = new CompletableFuture<>();

        long sent = System.currentTimeMillis();

        if(debug) logger.info("[pinger] [" + getName() + "] sending ping");

        handle.ping((pp, error) -> {
            if(error != null || pp == null) {
                markOffline(debug, logger, future, sent, error);
                return;
            }

            offlineTime = 0;

            BungeeServerPing ping = new BungeeServerPing(pp, sent);
            lastSuccessfullPing = ping;

            if(debug) logger.info(
                    "[pinger] [" + getName() + "] online. motd: "+ping.getPlainDescription()+" " +
                            " players: "+ping.getPlayerCount()+"/"+ping.getMaxPlayers()
            );

            future.complete(ping);
            lastPing = ping;
        });
        return future;
    }

    private void markOffline(boolean debug, QueueLogger logger, CompletableFuture<AdaptedServerPing> future, long sent, @Nullable Throwable e) {
        long lastOnline = lastSuccessfullPing == null ? 0 : lastSuccessfullPing.getFetchedTime();
        offlineTime = (int) Math.min(sent - lastOnline, Integer.MAX_VALUE);

        lastOffline = sent;

        future.completeExceptionally(e);
        lastPing = null;
        if(debug) logger.info("[pinger] [" + getName() + "] offline:", e);
    }

    @Override
    public Optional<AdaptedServerPing> getLastPing() {
        return Optional.ofNullable(lastPing);
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
    public int getOfflineTime() {
        return offlineTime;
    }

    @Override
    public boolean canJoinFull(AdaptedPlayer player) {
        if(player == null) return true;
        return
                player.hasPermission("ajqueue.joinfull") ||
                        player.hasPermission("ajqueue.joinfullserver."+getName()) ||
                        player.hasPermission("ajqueue.joinfullandbypassserver."+getName()) ||
                        player.hasPermission("ajqueue.joinfullandbypass") ||
                        (AjQueueAPI.getInstance().isPremium() && AjQueueAPI.getInstance().getLogic().getPermissionGetter().hasUniqueFullBypass(player, getName()))
                ;
    }

    @Override
    public boolean justWentOnline() {
        return System.currentTimeMillis()-lastOffline <= (AjQueueAPI.getInstance().getConfig().getDouble("wait-time") * 2 * 1000) && isOnline();
    }

    @Override
    public ServerInfo getHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BungeeServer)) return false;
        BungeeServer that = (BungeeServer) o;
        return getHandle().equals(that.getHandle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHandle());
    }
}
