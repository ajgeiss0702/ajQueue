package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.utils.bungee.BungeeUtils;
import us.ajg0702.utils.common.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueueManagerImpl implements QueueManager {

    private List<QueueServer> servers;

    private final QueueMain main;
    private final Messages msgs;

    public QueueManagerImpl(QueueMain main) {
        this.main = main;
        this.msgs = main.getMessages();
    }

    @Override
    public boolean addToQueue(AdaptedPlayer player, QueueServer server) {
        if(player == null || server == null) {
            return false;
        }
        if(!player.isConnected()) return false;

        if(main.getConfig().getBoolean("joinfrom-server-permission") && !player.hasPermission("ajqueue.joinfrom."+player.getServerName())) {
            player.sendMessage(msgs.getComponent("errors.deny-joining-from-server"));
            return false;
        }

        if(server.isPaused() && main.getConfig().getBoolean("prevent-joining-paused")) {
            player.sendMessage(msgs.getComponent("errors.cant-join-paused", "SERVER:"+server.getAlias()));
            return false;
        }

        if(player.getServerName().equals(server.getName())) {
            player.sendMessage(msgs.getComponent("errors.already-connected", "SERVER:"+server.getAlias()));
            return false;
        }

        ImmutableList<QueueServer> beforeQueues = getPlayerQueues(player);
        if(beforeQueues.size() > 0) {
            if(beforeQueues.contains(server)) {
                player.sendMessage(msgs.getComponent("errors.already-queued"));
                return false;
            }
            if(!main.getConfig().getBoolean("allow-multiple-queues")) {
                player.sendMessage(msgs.getComponent("status.left-last-queue", "SERVER:"+server.getAlias()));
                for(QueueServer ser : beforeQueues) {
                    ser.removePlayer(player);
                }
            }
        }

        ImmutableList<QueuePlayer> list = server.getQueue();
        QueuePlayer queuePlayer;
        if(main.isPremium()) {
            queuePlayer = main.getLogic().priorityLogic(server, player);
        } else {
            int priority = player.hasPermission("ajqueue.priority") ||
                    player.hasPermission("ajqueue.serverpriority."+server.getName()) ? 1 : 0;
            queuePlayer = new QueuePlayerImpl(player, server, priority);
            if(
                    priority == 1 &&
                    server.getQueue().size() > 0
            ) {
                int i = 0;
                for(QueuePlayer ply : list) {
                    if(!ply.hasPriority()) {
                        server.addPlayer(queuePlayer, i);
                        break;
                    }
                    i++;
                }
            }

            if(!list.contains(queuePlayer)) {
                server.addPlayer(queuePlayer);
            }
        }

        list = server.getQueue();

        int pos = queuePlayer.getPosition();
        int len = list.size();

        boolean sendInstant = main.getConfig().getStringList("send-instantly").contains(server.getName()) || server.isJoinable(p);
        boolean sendInstantp = list.size() <= 1 && server.canAccess(player);
        boolean timeGood = !main.getConfig().getBoolean("check-last-player-sent-time") || System.currentTimeMillis() - server.getLastSentTime() > Math.floor(main.getConfig().getDouble("wait-time") * 1000);

        if((sendInstant && (sendInstantp && timeGood))) {
            sendPlayers(server);
            if(!msgs.isEmpty("status.now-in-empty-queue")) {
                player.sendMessage(msgs.getComponent("status.now-in-empty-queue",
                        "POS:"+pos,
                        "LEN:"+len,
                        "SERVER:"+server.getAlias()));
            }
        } else {
            player.sendMessage(msgs.getComponent("status.now-in-queue",
                    "POS:"+pos,
                    "LEN:"+len,
                    "SERVER:"+server.getAlias(),
                    "SERVERNAME:"+server.getName()
            ));
        }

        main.getPlatformMethods().sendJoinQueueChannelMessages();
        return true;
    }

    @Override
    public boolean addToQueue(AdaptedPlayer player, String serverName) {
        QueueServer server = findServer(serverName);
        if(server == null) return false;
        return addToQueue(player, server);
    }

    @Override
    public ImmutableList<QueueServer> getServers() {
        return ImmutableList.copyOf(servers);
    }

    @Override
    public ImmutableList<String> getServerNames() {
        List<String> names = new ArrayList<>();
        for(QueueServer s : servers) {
            names.add(s.getName());
        }
        return ImmutableList.copyOf(names);
    }

    @Override
    public QueueServer getSingleServer(AdaptedPlayer player) {
        ImmutableList<QueuePlayer> queued = findPlayerInQueues(player);
        if(queued.size() <= 0) {
            return null;
        }
        QueueServer selected = queued.get(0).getQueueServer();

        if(main.getConfig().getString("multi-server-queue-pick").equalsIgnoreCase("last")) {
            selected = queued.get(queued.size()-1).getQueueServer();
        }
        return selected;
    }

    @Override
    public String getQueuedName(AdaptedPlayer player) {
        return getSingleServer(player).getName();
    }

    @Override
    public void reloadServers() {
        if(main.getConfig() == null) {
            main.getLogger().severe("[MAN] Config is null");
        }

        servers.clear();

        servers.addAll(main.getServerBuilder().getServers());

        List<String> groupsraw = main.getConfig().getStringList("server-groups");
        for(String groupraw : groupsraw) {
            if(groupraw.isEmpty()) {
                main.getLogger().warning("Empty group string! If you dont want server groups, set server-groups like this: server-groups: []");
                continue;
            }

            String groupname = groupraw.split(":")[0];
            String[] serversraw = groupraw.split(":")[1].split(",");

            if(findServer(groupname) != null) {
                main.getLogger().warning("The name of a group ('"+groupname+"') cannot be the same as the name of a server!");
                continue;
            }


            for(String serverraw : serversraw) {
                ServerInfo si = svs.get(serverraw);
                if(si == null) {
                    pl.getLogger().warning("Could not find server named '"+serverraw+"' in servergroup '"+groupname+"'!");
                    continue;
                }
                servers.add(si);
            }

            if(servers.size() == 0) {
                pl.getLogger().warning("Server group '"+groupname+"' has no servers! Ignoring it.");
                continue;
            }

            this.servers.add(new QueueServer(groupname, servers));
        }
    }

    @Override
    public void sendActionBars() {

    }

    @Override
    public void sendQueueEvents() {

    }

    @Override
    public void sendMessages() {

    }

    @Override
    public void sendMessage(QueuePlayer player) {

    }

    @Override
    public QueueServer findServer(String name) {
        return null;
    }

    @Override
    public void sendPlayers() {

    }

    @Override
    public void sendPlayers(QueueServer server) {

    }

    @Override
    public ImmutableList<QueuePlayer> findPlayerInQueues(AdaptedPlayer p) {
        return null;
    }

    @Override
    public ImmutableList<QueueServer> getPlayerQueues(AdaptedPlayer p) {
        return null;
    }
}
