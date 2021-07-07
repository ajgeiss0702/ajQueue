package us.ajg0702.queue.common.queues;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.Messages;

import java.util.*;

public class QueueServerImpl implements QueueServer {

    public QueueServerImpl(QueueMain main, AdaptedServer server) {
        this(main, Collections.singletonList(server));
    }

    public QueueServerImpl(QueueMain main, List<AdaptedServer> servers) {
        this.servers = servers;
        this.main = main;
    }

    QueueMain main;

    private List<AdaptedServer> servers;

    private final List<QueuePlayer> queue = new ArrayList<>();

    @Override
    public ImmutableList<QueuePlayer> getQueue() {
        return ImmutableList.copyOf(queue);
    }

    @Override
    public String getStatusString(AdaptedPlayer p) {
        Messages msgs = main.getMessages();

        if(getOfflineTime() > main.getConfig().getInt("offline-time")) {
            return msgs.getString("status.offline.offline");
        }

        if(!isOnline()) {
            return msgs.getString("status.offline.restarting");
        }

        if(isPaused()) {
            return msgs.getString("status.offline.paused");
        }

        if(isFull()) {
            return msgs.getString("status.offline.full");
        }

        if(p != null && !canAccess(p)) {
            return msgs.getString("status.offline.restricted");
        }


        return "online";
    }

    @Override
    public String getStatusString() {
        return getStatusString(null);
    }

    @Override
    public void updatePing() {

    }

    @Override
    public int getOfflineTime() {
        return 0;
    }

    @Override
    public int getLastSentTime() {
        return 0;
    }

    @Override
    public boolean isWhitelisted() {
        return false;
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {

    }

    @Override
    public ImmutableList<UUID> getWhitelistedPlayers() {
        return null;
    }

    @Override
    public void setWhitelistedPlayers(List<UUID> whitelistedPlayers) {

    }

    @Override
    public boolean isJoinable(AdaptedPlayer p) {
        return false;
    }

    @Override
    public void setPaused(boolean paused) {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public boolean justWentOnline() {
        return false;
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public void removePlayer(QueuePlayer player) {

    }

    @Override
    public void removePlayer(AdaptedPlayer player) {

    }

    @Override
    public void addPlayer(QueuePlayer player) {

    }

    @Override
    public void addPlayer(QueuePlayer player, int position) {

    }

    @Override
    public void sendPlayer() {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean canAccess(AdaptedPlayer ply) {
        return false;
    }

    @Override
    public String getAlias() {
        return null;
    }

    @Override
    public ImmutableList<AdaptedServer> getServers() {
        return null;
    }

    @Override
    public ImmutableList<String> getServerNames() {
        return null;
    }

    @Override
    public boolean isGroup() {
        return false;
    }

    @Override
    public QueuePlayer findPlayer(AdaptedPlayer player) {
        return null;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        return null;
    }
}
