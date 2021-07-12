package us.ajg0702.queue.common.queues;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.api.server.AdaptedServerPing;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.utils.common.GenUtils;
import us.ajg0702.utils.common.Messages;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueueServerImpl implements QueueServer {

    private final String name;

    public QueueServerImpl(String name, QueueMain main, AdaptedServer server) {
        this(name, main, Collections.singletonList(server));
    }

    public QueueServerImpl(String name, QueueMain main, List<AdaptedServer> servers) {
        this.name = name;
        this.servers = servers;
        this.main = main;
    }

    private final QueueMain main;

    private final HashMap<AdaptedServer, AdaptedServerPing> pings = new HashMap<>();

    private final List<AdaptedServer> servers;

    private final List<QueuePlayer> queue = new ArrayList<>();


    private int playerCount;
    private int maxPlayers;

    private boolean online;

    private boolean paused;


    private long lastUpdate = 0;

    private int offlineTime = 0;

    private long lastSentTime = 0;

    private long lastOffline;


    boolean whitelisted = false;
    List<UUID> whitelistedUUIDs = new ArrayList<>();


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
        HashMap<AdaptedServer, CompletableFuture<AdaptedServerPing>> pingsFutures = new HashMap<>();
        for(AdaptedServer server : servers) {
            if(main.getConfig().getBoolean("pinger-debug")) {
                main.getLogger().info("[pinger] ["+server.getServerInfo().getName()+"] sending ping");
            }
            pingsFutures.put(server, server.ping());
        }

        int i = 0;
        for(AdaptedServer server : pingsFutures.keySet()) {
            CompletableFuture<AdaptedServerPing> futurePing = pingsFutures.get(server);
            AdaptedServerPing ping = null;
            try {
                ping = futurePing.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                if(main.getConfig().getBoolean("pinger-debug")) {
                    main.getLogger().info("[pinger] ["+server.getServerInfo().getName()+"] offline:");
                    e.printStackTrace();
                }
            }
            if(ping != null && main.getConfig().getBoolean("pinger-debug")) {
                main.getLogger().info("[pinger] ["+server.getServerInfo().getName()+"] online. motd: "+ping.getPlainDescription()+"  players: "+ping.getPlayerCount()+"/"+ping.getMaxPlayers());
            }

            pings.put(server, ping);
            i++;
            if(i == servers.size()) {
                int onlineCount = 0;
                playerCount = 0;
                maxPlayers = 0;
                for(AdaptedServer pingedServer : pings.keySet()) {
                    AdaptedServerPing serverPing = pings.get(pingedServer);
                    if(serverPing == null) {
                        continue;
                    }
                    onlineCount++;
                    playerCount += serverPing.getPlayerCount();
                    maxPlayers += serverPing.getMaxPlayers();
                }
                online = onlineCount > 0;

                if(lastUpdate == -1) {
                    lastUpdate = System.currentTimeMillis();
                    offlineTime = 0;
                } else {
                    int timesincelast = (int) Math.round((System.currentTimeMillis() - lastUpdate*1.0)/1000);
                    lastUpdate = System.currentTimeMillis();
                    if(!online) {
                        offlineTime += timesincelast;
                    } else {
                        offlineTime = 0;
                    }
                }
            }
        }
    }

    @Override
    public int getOfflineTime() {
        return offlineTime;
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
    public boolean isWhitelisted() {
        return whitelisted;
    }

    @Override
    public void setWhitelisted(boolean whitelisted) {
        this.whitelisted = whitelisted;
    }

    @Override
    public ImmutableList<UUID> getWhitelistedPlayers() {
        return ImmutableList.copyOf(whitelistedUUIDs);
    }

    @Override
    public synchronized void setWhitelistedPlayers(List<UUID> whitelistedPlayers) {
        whitelistedUUIDs = whitelistedPlayers;
    }

    @Override
    public boolean isJoinable(AdaptedPlayer p) {
        return (!whitelisted || whitelistedUUIDs.contains(p.getUniqueId())) &&
                this.isOnline() &&
                this.canAccess(p) &&
                !this.isFull() &&
                !this.isPaused();
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
    public boolean isOnline() {
        if(System.currentTimeMillis()-lastOffline <= (main.getConfig().getInt("wait-after-online")*1000) && online) {
            return false;
        }
        if(!online) {
            lastOffline = System.currentTimeMillis();
        }
        return online;
    }

    @Override
    public boolean justWentOnline() {
        return System.currentTimeMillis()-lastOffline <= (main.getConfig().getDouble("wait-time")) && online;
    }

    @Override
    public boolean isFull() {
        return playerCount >= maxPlayers;
    }

    @Override
    public synchronized void removePlayer(QueuePlayer player) {
        queue.remove(player);
    }

    @Override
    public void removePlayer(AdaptedPlayer player) {
        QueuePlayer queuePlayer = findPlayer(player);
        if(queuePlayer == null) return;
        removePlayer(queuePlayer);
    }

    @Override
    public synchronized void addPlayer(QueuePlayer player) {
        addPlayer(player, -1);
    }

    @Override
    public void addPlayer(QueuePlayer player, int position) {
        if(!player.getQueueServer().equals(this) || queue.contains(player)) return;
        if(position > 0) {
            queue.add(position, player);
        } else {
            queue.add(player);
        }
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
    public synchronized QueuePlayer findPlayer(AdaptedPlayer player) {
        for(QueuePlayer queuePlayer : queue) {
            AdaptedPlayer queuedPlayer = queuePlayer.getPlayer();
            if(queuedPlayer == null) continue;
            if(
                    queuedPlayer
                            .getUniqueId()
                            .equals(
                                    player
                                            .getUniqueId()
                            )
            ) {
                return queuePlayer;
            }
        }
        return null;
    }

    @Override
    public AdaptedServer getIdealServer(AdaptedPlayer player) {
        HashMap<AdaptedServer, AdaptedServerPing> serverInfos = pings;
        AdaptedServer selected = null;
        int selectednum = 0;
        if(serverInfos.keySet().size() == 1) {
            selected = serverInfos.keySet().iterator().next();
        } else {
            for(AdaptedServer si : serverInfos.keySet()) {
                AdaptedServerPing sp = serverInfos.get(si);
                if(sp == null) continue;
                int online = sp.getPlayerCount();
                if(selected == null) {
                    selected = si;
                    selectednum = online;
                    continue;
                }
                if(selectednum > online && main.getQueueManager().findServer(si.getName()).isJoinable(player)) {
                    selected = si;
                    selectednum = online;
                }
            }
        }
        if(selected == null && serverInfos.size() > 0) {
            selected = serverInfos.keySet().iterator().next();
        }
        if(selected == null) {
            main.getLogger().warning("Unable to find ideal server, using random server from group.");
            int r = GenUtils.randomInt(0, getServers().size()-1);
            selected = getServers().get(r);
        }
        return selected;
    }

    @Override
    public HashMap<AdaptedServer, AdaptedServerPing> getLastPings() {
        return new HashMap<>(pings);
    }
}
