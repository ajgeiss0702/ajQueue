package us.ajg0702.queue.common.queues;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.events.PositionChangeEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queueholders.QueueHolder;
import us.ajg0702.queue.api.queues.Balancer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.queues.balancers.DefaultBalancer;
import us.ajg0702.queue.common.queues.balancers.FirstBalancer;
import us.ajg0702.queue.common.queues.balancers.MinigameBalancer;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.Messages;

import java.util.*;

public class QueueServerImpl implements QueueServer {

    private final String name;

    private final QueueMain main;

    private final List<AdaptedServer> servers;

    private final QueueHolder queueHolder = AjQueueAPI.getQueueHolderRegistry().getQueueHolder(this);

    private List<Integer> supportedProtocols = new ArrayList<>();

    private Balancer balancer;

    private boolean paused;

    private long lastSentTime = 0;
    private int lastSendQueueSize = 0;

    private int manualMaxPlayers = Integer.MAX_VALUE;

    private QueueType lastQueueSend = QueueType.STANDARD;
    private int sendCount = 0;

    private List<Float> sendTimes = new ArrayList<>();


    public QueueServerImpl(String name, QueueMain main, AdaptedServer server, List<QueuePlayer> previousStandardPlayers, List<QueuePlayer> previousExpressPlayers) {
        this(name, main, Collections.singletonList(server), previousStandardPlayers, previousExpressPlayers);
    }

    public QueueServerImpl(String name, QueueMain main, List<AdaptedServer> servers, List<QueuePlayer> previousStandardPlayers, List<QueuePlayer> previousExpressPlayers) {
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
                switch(balancerType.toLowerCase(Locale.ROOT)) {
                    case "minigame":
                        balancer = new MinigameBalancer(this, main);
                        break;
                    case "first":
                        balancer = new FirstBalancer(this, main);
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

        List<String> manualLimits = main.getConfig().getStringList("manual-max-players");
        for (String manualLimit : manualLimits) {
            String[] parts = manualLimit.split(":");
            if(parts.length != 2) {
                main.getLogger().warn("Invalid manual limit: " + manualLimit);
                continue;
            }
            String limitFor = parts[0];

            if(!limitFor.equals(name)) continue;

            String limitStr = parts[1];
            try {
                manualMaxPlayers = Integer.parseInt(limitStr);
            } catch(NumberFormatException e) {
                main.getLogger().warn("Invalid limit number for " + limitFor);
            }
            break;
        }

        List<QueuePlayer> previousPlayers = new ArrayList<>();
        previousPlayers.addAll(previousExpressPlayers);
        previousPlayers.addAll(previousStandardPlayers);

        for(QueuePlayer queuePlayer : previousPlayers) {
            if(queuePlayer.getPlayer() == null) {
                addPlayer(
                        new QueuePlayerImpl(
                                queuePlayer.getUniqueId(),
                                queuePlayer.getName(),
                                this,
                                queuePlayer.getPriority(),
                                queuePlayer.getMaxOfflineTime(),
                                queuePlayer.getQueueType()
                        )
                );
            } else {
                addPlayer(
                        new QueuePlayerImpl(
                                queuePlayer.getPlayer(),
                                this,
                                queuePlayer.getPriority(),
                                queuePlayer.getMaxOfflineTime(),
                                queuePlayer.getQueueType()
                        )
                );
            }
        }
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

        if(server.isWhitelisted() && (p == null || !server.getWhitelistedPlayers().contains(p.getUniqueId()))) {
            return msgs.getString("status.offline.whitelisted");
        }

        if((server.isFull() && !server.canJoinFull(p)) || (isManuallyFull() && !AdaptedServer.canJoinFull(p, getName()))) {
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

        if(server.isWhitelisted() && (p == null || !server.getWhitelistedPlayers().contains(p.getUniqueId()))) {
            return "whitelisted";
        }

        if(((server.isFull() && !server.canJoinFull(p)) || (isManuallyFull() && !AdaptedServer.canJoinFull(p, getName())))) {
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
        long previousSendTime = this.lastSentTime;
        int previousQueueSize = this.lastSendQueueSize;

        // We don't add a queue time if the previous send resulted an in an empty queue.
        // This is so we don't count the time that the queue was sitting idle with 0 players in it.
        // The setLastSentTime method is called after removing the player from the queue,
        //  so if the last size is 0, then the last send resulted in an empty queue.
        if(previousQueueSize != 0) {
            sendTimes.add((float) (lastSentTime - previousSendTime) / 1000);
            if(sendTimes.size() > main.getConfig().getInt("send-times-to-keep")) {
                sendTimes.remove(0);
            }
        }

        this.lastSendQueueSize = queueHolder.getTotalQueueSize();
        this.lastSentTime = lastSentTime;
    }

    @Override
    public double getAverageSendTime() {
        // don't allow the average send time to be lower than the wait-time set in the config
        return Math.max(
                main.getTimeBetweenPlayers(),
                sendTimes.isEmpty() ?
                        0 :
                        (
                                sendTimes
                                    .stream()
                                    .mapToDouble(Float::doubleValue)
                                    .average()
                                    .orElse(0)
                                        / 1e3
                        )
        );
    }

    @Override
    public boolean isJoinable(AdaptedPlayer p) {
        return isJoinable(p, false);
    }

    @Override
    public boolean isJoinable(AdaptedPlayer p, boolean ignoreFull) {
        if(isManuallyFull() && !AdaptedServer.canJoinFull(p, getName())) return false;
        AdaptedServer server = getIdealServer(p);
        if(server == null) return false;
        return server.isJoinable(p, ignoreFull) && !isPaused();
    }

    @Override
    public int getManualMaxPlayers() {
        return manualMaxPlayers;
    }

    @Override
    public boolean isManuallyFull() {
        int total = 0;
        for (AdaptedServer server : servers) {
            Optional<AdaptedServerPing> lastPing = server.getLastPing();
            if(!lastPing.isPresent()) continue;
            total += lastPing.get().getPlayerCount();
        }

//        Debug.info(total + " >= " + getManualMaxPlayers() + " = " + (total >= getManualMaxPlayers()));

        return total >= getManualMaxPlayers();
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
    public void removePlayer(QueuePlayer player) {
        main.getQueueManager().getSendingAttempts().remove(player);
        queueHolder.removePlayer(player);
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
    public void addPlayer(QueuePlayer player, int position) {
        if(!player.getQueueServer().equals(this) || queueHolder.findPlayer(player.getUniqueId()) != null) return;

        if(position >= 0) {
            queueHolder.addPlayer(player, position);
        } else {
            queueHolder.addPlayer(player);
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
    public boolean isOnline() {
        return QueueServer.super.isOnline();
    }

    @Override
    public boolean isGroup() {
        return servers.size() > 1;
    }

    @Override
    public QueuePlayer findPlayer(String player) {
        return queueHolder.findPlayer(player);
    }
    @Override
    public QueuePlayer findPlayer(AdaptedPlayer player) {
        return findPlayer(player.getUniqueId());
    }
    @Override
    public QueuePlayer findPlayer(UUID uuid) {
        return queueHolder.findPlayer(uuid);
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

    @Override
    public QueueHolder getQueueHolder() {
        return queueHolder;
    }

    @Override
    public QueueType getLastQueueSend() {
        return lastQueueSend;
    }

    @Override
    public int getSendCount() {
        return sendCount;
    }

    @Override
    public void incrementSendCount() {
        sendCount++;
    }

    @Override
    public void resetSendCount() {
        sendCount = 0;
    }

    public void setLastQueueSend(QueueType lastQueueSend) {
        this.lastQueueSend = lastQueueSend;
    }

    private void positionChange() {
        main.getTaskManager().runNow(
                () -> queueHolder.getAllPlayers().forEach(queuePlayer -> {
                    if(((QueuePlayerImpl) queuePlayer).lastPosition != queuePlayer.getPosition()) {
                        main.call(new PositionChangeEvent(queuePlayer));
                    }
                })
        );
    }

}
