package us.ajg0702.queue.common.queues;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.events.PositionChangeEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.queues.balancers.DefaultBalancer;
import us.ajg0702.queue.common.queues.balancers.MinigameBalancer;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.Messages;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class QueueServerImpl implements QueueServer {

    private final String name;

    private final QueueMain main;

    private final List<AdaptedServer> servers;

    private final List<QueuePlayer> queue = new ArrayList<>();

    private List<Integer> supportedProtocols = new ArrayList<>();

    private Balancer balancer;

    private boolean paused;

    private long lastSentTime = 0;

    public QueueServerImpl(String name, QueueMain main, AdaptedServer server, List<QueuePlayer> previousPlayers) {
        this(name, main, Collections.singletonList(server), previousPlayers);
    }

    public QueueServerImpl(String name, QueueMain main, List<AdaptedServer> servers, List<QueuePlayer> previousPlayers) {
        this.name = name;
        this.servers = servers;
        this.main = main;

        List<String> types = main.getConfig().getStringList("balancer-types");
        for(String type : types) {
            int colon = type.indexOf(":");
            if(colon == -1) continue;
            String groupName = type.substring(0, colon);
            String balancerType = type.substring(colon+1);

            if(groupName.equals(name)) {
                //noinspection SwitchStatementWithTooFewBranches
                switch(balancerType.toLowerCase(Locale.ROOT)) {
                    case "minigame":
                        balancer = new MinigameBalancer(this, main);
                        break;
                    default:
                        balancerType = "default";
                        balancer = new DefaultBalancer(this, main);
                }
                Debug.info("Using "+balancerType.toLowerCase(Locale.ROOT)+" balancer for "+name);
                break;
            }
        }
        if(balancer == null) {
            balancer = new DefaultBalancer(this, main);
            Debug.info("Using default balancer for "+name);
        }

        for(QueuePlayer queuePlayer : previousPlayers) {
            if(queuePlayer.getPlayer() == null) {
                addPlayer(
                        new QueuePlayerImpl(
                                queuePlayer.getUniqueId(),
                                queuePlayer.getName(),
                                this,
                                queuePlayer.getPriority(),
                                queuePlayer.getMaxOfflineTime()
                        )
                );
            } else {
                addPlayer(
                        new QueuePlayerImpl(
                                queuePlayer.getPlayer(),
                                this,
                                queuePlayer.getPriority(),
                                queuePlayer.getMaxOfflineTime()
                        )
                );
            }
        }
    }

    @Override
    public ImmutableList<QueuePlayer> getQueue() {
        return ImmutableList.copyOf(queue);
    }

    @Override
    public String getStatusString(AdaptedPlayer p) {
        Messages msgs = main.getMessages();
        AdaptedServer server = getIdealServer(p);

        if(server.getOfflineTime() > main.getConfig().getInt("offline-time")) {
            return msgs.getString("status.offline.offline");
        }

        if(!server.isOnline()) {
            return msgs.getString("status.offline.restarting");
        }

        if(isPaused()) {
            return msgs.getString("status.offline.paused");
        }

        if(p != null && server.isWhitelisted() && !server.getWhitelistedPlayers().contains(p.getUniqueId())) {
            return msgs.getString("status.offline.whitelisted");
        }

        if(server.isFull() && !server.canJoinFull(p)) {
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
    public String getStatus(AdaptedPlayer p) {
        AdaptedServer server = getIdealServer(p);
        if(server.getOfflineTime() > main.getConfig().getInt("offline-time")) {
            return "offline";
        }

        if(!server.isOnline()) {
            return "restarting";
        }

        if(isPaused()) {
            return "paused";
        }

        if(p != null && server.isWhitelisted() && !server.getWhitelistedPlayers().contains(p.getUniqueId())) {
            return "whitelisted";
        }

        if(server.isFull() && !server.canJoinFull(p)) {
            return "full";
        }

        if(p != null && !canAccess(p)) {
            return "restricted";
        }

        return "online";
    }

    @Override
    public String getStatus() {
        return getStatus(null);
    }

    @Override
    public long getLastSentTime() {
        return System.currentTimeMillis() - lastSentTime;
    }
    @Override
    public void setLastSentTime(long lastSentTime) {
        this.lastSentTime = lastSentTime;
    }

    @Override
    public boolean isJoinable(AdaptedPlayer p) {
        AdaptedServer server = getIdealServer(p);
        if(server == null) return false;
        return server.isJoinable(p) && !isPaused();
    }

    @Override
    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public synchronized void removePlayer(QueuePlayer player) {
        main.getQueueManager().getSendingAttempts().remove(player);
        queue.remove(player);
        positionChange();
    }

    @Override
    public void removePlayer(AdaptedPlayer player) {
        QueuePlayer queuePlayer = findPlayer(player);
        if(queuePlayer == null) return;
        removePlayer(queuePlayer);
    }

    @Override
    public void addPlayer(QueuePlayer player) {
        addPlayer(player, -1);
    }

    @Override
    public synchronized void addPlayer(QueuePlayer player, int position) {
        if(!player.getQueueServer().equals(this) || queue.contains(player)) return;

        if(position >= 0) {
            queue.add(position, player);
        } else {
            queue.add(player);
        }
        positionChange();
    }

    @Override
    public void sendPlayer() {
        main.getQueueManager().sendPlayers(this);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canAccess(AdaptedPlayer ply) {
        if(ply == null) return true;
        for(AdaptedServer si : servers) {
            if(si.canAccess(ply)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAlias() {
        return main.getAliasManager().getAlias(getName());
    }

    @Override
    public ImmutableList<AdaptedServer> getServers() {
        return ImmutableList.copyOf(servers);
    }

    @Override
    public ImmutableList<String> getServerNames() {
        List<String> names = new ArrayList<>();
        for(AdaptedServer server : servers) {
            names.add(server.getName());
        }
        return ImmutableList.copyOf(names);
    }

    @Override
    public boolean isGroup() {
        return servers.size() > 1;
    }

    @Override
    public QueuePlayer findPlayer(String player) {
        for(QueuePlayer queuePlayer : queue) {
            if(queuePlayer.getName().equalsIgnoreCase(player)) {
                return queuePlayer;
            }
        }
        return null;
    }
    @Override
    public QueuePlayer findPlayer(AdaptedPlayer player) {
        return findPlayer(player.getUniqueId());
    }
    @Override
    public synchronized QueuePlayer findPlayer(UUID uuid) {
        for(QueuePlayer queuePlayer : queue) {
            if(queuePlayer.getUniqueId().toString().equals(uuid.toString())) {
                return queuePlayer;
            }
        }
        return null;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        return getBalancer().getIdealServer(player);
    }

    @Override
    public List<Integer> getSupportedProtocols() {
        return new ArrayList<>(supportedProtocols);
    }

    @Override
    public void setSupportedProtocols(List<Integer> list) {
        supportedProtocols = new ArrayList<>(list);
    }

    @Override
    public Balancer getBalancer() {
        return balancer;
    }

    private void positionChange() {
        main.getTaskManager().runNow(
                () -> queue.forEach(queuePlayer -> main.call(new PositionChangeEvent(queuePlayer)))
        );
    }

}
