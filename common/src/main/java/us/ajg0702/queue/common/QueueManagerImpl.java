package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.events.PreQueueEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.commands.commands.manage.PauseQueueServer;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.queues.QueueServerImpl;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.Messages;
import us.ajg0702.utils.common.TimeUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class QueueManagerImpl implements QueueManager {

    private CopyOnWriteArrayList<QueueServer> servers = new CopyOnWriteArrayList<>();

    private final QueueMain main;
    private final Messages msgs;

    public QueueManagerImpl(QueueMain main) {
        this.main = main;
        this.msgs = main.getMessages();

        int delay = main.getConfig().getBoolean("wait-to-load-servers") ? main.getConfig().getInt("wait-to-load-servers-delay") : 0;

        main.getTaskManager().runLater(this::reloadServers, delay, TimeUnit.MILLISECONDS);
    }

    public List<QueueServer> buildServers() {
        List<QueueServer> result = new ArrayList<>();
        List<? extends AdaptedServer> servers = main.getPlatformMethods().getServers();

        for(AdaptedServer server : servers) {
            QueueServer previousServer = findServer(server.getName());
            List<QueuePlayer> previousPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueue();
            if(previousPlayers.size() > 0) {
                main.getLogger().info("Adding "+previousPlayers.size()+" players back to the queue for "+server.getName());
            }
            QueueServer queueServer = new QueueServerImpl(server.getName(), main, server, previousPlayers);
            if(previousServer != null) {
                queueServer.setPaused(previousServer.isPaused());
                queueServer.setLastSentTime(previousServer.getLastSentTime());
            }
            result.add(queueServer);
        }

        List<String> groupsRaw = main.getConfig().getStringList("server-groups");
        for(String groupRaw : groupsRaw) {
            if(groupRaw.isEmpty()) {
                main.getLogger().warning("Empty group string! If you dont want server groups, set server-groups like this: server-groups: []");
                continue;
            }

            if(!groupRaw.contains(":")) {
                main.getLogger().warning("Incorrect formatting! Each server group needs to have a name and a list of servers seperated by a colon (:).");
                continue;
            }

            String groupName = groupRaw.split(":")[0];
            String[] serversraw = groupRaw.split(":")[1].split(",");

            if(findServer(groupName, result) != null) {
                main.getLogger().warning("The name of a group ('"+groupName+"') cannot be the same as the name of a server!");
                continue;
            }

            List<AdaptedServer> groupServers = new ArrayList<>();

            for(String serverraw : serversraw) {
                QueueServer found = findServer(serverraw, result);
                if(found == null) {
                    main.getLogger().warning("Could not find server named '"+serverraw+"' in servergroup '"+groupName+"'!");
                    continue;
                }
                if(found.isGroup()) continue;

                groupServers.add(found.getServers().get(0));
            }

            if(groupServers.size() == 0) {
                main.getLogger().warning("Server group '"+groupName+"' has no servers! Ignoring it.");
                continue;
            }


            QueueServer previousServer = main.getQueueManager().findServer(groupName);
            List<QueuePlayer> previousPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueue();
            if(previousPlayers.size() > 0) {
                main.getLogger().info("Adding "+previousPlayers.size()+" players back to the queue for "+groupName);
            }

            result.add(new QueueServerImpl(groupName, main, groupServers, previousPlayers));
        }

        List<String> supportedProtocolsRaw = main.getConfig().getStringList("supported-protocols");
        for(String supportedProtocolsString : supportedProtocolsRaw) {
            String[] parts = supportedProtocolsString.split(":");
            if(parts.length < 2) {
                main.getLogger().warn("Invalid supported protocols entry! Must have a colon to seperate the server(s) and the protocols");
                continue;
            }
            String serversRaw = parts[0];
            String protocolsRaw = parts[1];

            List<Integer> protocols = new ArrayList<>();
            for(String protocolString : protocolsRaw.split(",")) {
                try {
                    protocols.add(Integer.valueOf(protocolString));
                } catch(NumberFormatException e) {
                    main.getLogger().info("The protocol "+protocolString+" is not a valid number!");
                }
            }

            for(String serverName : serversRaw.split(",")) {
                boolean found = false;
                for(QueueServer server : result) {
                    if(serverName.equalsIgnoreCase(server.getName())) {
                        server.setSupportedProtocols(protocols);
                        found = true;
                        Debug.info("Applied " + protocols + " to " + server.getName() + "(" + serverName + ")");
                        break;
                    }
                }
                if(!found) {
                    Debug.info("Found no server named " + serverName);
                }
            }

        }

        return result;
    }

    @Override
    public boolean addToQueue(AdaptedPlayer player, QueueServer server) {
        if(player == null || server == null) {
            Debug.info("addToQueue method called, but something is null");
            return false;
        }
        if(!player.isConnected()) {
            Debug.info("addToQueue method called, but player is not connected");
            return false;
        }

        if(player.getServerName() == null) {
            main.getLogger().warning("Tried to queue " + player.getName() + " when they aren't connected!");
            return false;
        }

        Debug.info("addToQueue method called for "+player.getName()+" to "+server.getName());

        int playerVersion = player.getProtocolVersion();
        List<Integer> supportedProtocols = server.getSupportedProtocols();
        if(!supportedProtocols.contains(playerVersion) && supportedProtocols.size() > 0) {
            StringBuilder versions = new StringBuilder();
            for(int protocol : supportedProtocols) {
                versions.append(main.getProtocolNameManager().getProtocolName(protocol));
                if(supportedProtocols.indexOf(protocol) == supportedProtocols.size()-2) {
                    versions.append(msgs.getString("errors.wrong-version.or"));
                } else if(supportedProtocols.indexOf(protocol) != supportedProtocols.size()-1) {
                    versions.append(msgs.getString("errors.wrong-version.comma"));
                }
            }
            player.sendMessage(msgs.getComponent(
                    "errors.wrong-version.base",
                    "VERSIONS:" + versions,
                    "SERVER:"+server.getAlias()
            ));
            return false;
        }

        boolean enableBypassPaused = main.getConfig().getBoolean("enable-bypasspaused-permission");
        if(server.isPaused() && main.getConfig().getBoolean("prevent-joining-paused")) {
            if(!enableBypassPaused || !player.hasPermission("ajqueue.bypasspaused")) {
                player.sendMessage(msgs.getComponent("errors.cant-join-paused", "SERVER:"+server.getAlias()));
                return false;
            }
        }

        List<AdaptedServer> notInServers = new ArrayList<>(server.getServers());
        notInServers.removeIf(adaptedServer -> !adaptedServer.getName().equals(player.getServerName()));
        if(notInServers.size() > 0) {
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


        PreQueueEvent preQueueEvent = new PreQueueEvent(player, server);
        main.call(preQueueEvent);
        if(preQueueEvent.isCancelled()) return false;


        // Player should be added!

        ImmutableList<QueuePlayer> list = server.getQueue();
        QueuePlayer queuePlayer;
        AdaptedServer ideal = server.getIdealServer(player);
        if(main.isPremium()) {
            queuePlayer = main.getLogic().priorityLogic(server, player, ideal);
        } else {
            int priority = player.hasPermission("ajqueue.priority") ||
                    player.hasPermission("ajqueue.serverpriority."+server.getName()) ? 1 : 0;
            priority = Math.max(priority, Logic.getUnJoinablePriorities(server, ideal, player) > 0 ? 1 : 0);
            int maxOfflineTime = player.hasPermission("ajqueue.stayqueued") ? 60 : 0;
            queuePlayer = new QueuePlayerImpl(player, server, priority, maxOfflineTime);
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

        boolean sendInstant = server.isJoinable(player);
        boolean sendInstantp = list.size() <= 1 && server.isJoinable(player);
        boolean timeGood = !main.getConfig().getBoolean("check-last-player-sent-time") || server.getLastSentTime() > Math.floor(main.getTimeBetweenPlayers() * 1000);
        boolean alwaysSendInstantly = main.getConfig().getStringList("send-instantly").contains(server.getName());
        boolean hasBypass = main.getLogic().hasAnyBypass(player, server.getName());

        boolean sentInstantly = alwaysSendInstantly || (sendInstant && (sendInstantp && timeGood)) || hasBypass;

        Debug.info("should send instantly (" + sentInstantly + "): " + alwaysSendInstantly + " || (" + sendInstant + " && (" + sendInstantp + " && " + timeGood + ") && " + (!hasBypass) + ")");
        if(sentInstantly) {
            if(!hasBypass) {
                sendPlayers(server);
            }
            if(!msgs.isEmpty("status.now-in-empty-queue")) {
                player.sendMessage(msgs.getComponent("status.now-in-empty-queue",
                        "POS:"+pos,
                        "LEN:"+len,
                        "SERVER:"+server.getAlias()));
            }
        } else {
            player.sendMessage(
                    msgs.getComponent("status.now-in-queue",
                      "POS:"+pos,
                       "LEN:"+len,
                        "SERVER:"+server.getAlias(),
                       "SERVERNAME:"+server.getName()
                    )
            );
            if(main.getConfig().getBoolean("enable-priority-messages")) {
                for(String rawPriorityMessage : main.getConfig().getStringList("priority-messages")) {
                    String[] parts = rawPriorityMessage.split(":");
                    if(parts.length != 2) continue;
                    String level = parts[0];
                    String messageRaw = parts[1];
                    if((level.equals("*") && queuePlayer.getPriority() > 0) || level.equals(queuePlayer.getPriority()+"")) {
                        player.sendMessage(main.getMessages().toComponent(messageRaw.replaceAll("\\{PRIORITY}", queuePlayer.getPriority()+"")));
                    }
                }
            }
        }

        if(!server.isJoinable(player)) {
            sendMessage(queuePlayer);
        }
        main.getPlatformMethods().sendPluginMessage(player, "position", pos+"");
        main.getPlatformMethods().sendPluginMessage(player, "positionof", len+"");
        main.getPlatformMethods().sendPluginMessage(player, "queuename", server.getAlias());
        main.getPlatformMethods().sendPluginMessage(player, "inqueue", "true");
        main.getPlatformMethods().sendPluginMessage(player, "inqueueevent", "true");
        return true;
    }

    @Override
    public boolean addToQueue(AdaptedPlayer player, String serverName) {
        QueueServer server = findServer(serverName);
        if(server == null) {
            player.sendMessage(msgs.getComponent("errors.server-not-exist", "SERVER:"+serverName));
            return false;
        }
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
        QueueServer server = getSingleServer(player);
        if(server == null) return main.getMessages().getString("placeholders.queued.none");
        return server.getName();
    }

    @Override
    public void reloadServers() {
        if(main.getConfig() == null) {
            main.getLogger().severe("[MAN] Config is null");
        }

        servers = new CopyOnWriteArrayList<>(buildServers());
    }

    @Override
    public void sendActionBars() {
        if(!main.getConfig().getBoolean("send-actionbar")) return;

        for(QueueServer server : servers) {
            for(QueuePlayer queuePlayer : server.getQueue()) {

                int pos = queuePlayer.getPosition();
                if(pos == 0) {
                    server.removePlayer(queuePlayer);
                    continue;
                }

                AdaptedPlayer player = queuePlayer.getPlayer();
                if(player == null) continue;

                String status = server.getStatusString(player);

                QueueServer singleServer = getSingleServer(player);
                if(singleServer == null || !singleServer.equals(server)) continue;

                if(!server.isJoinable(player)) {
                    player.sendActionBar(msgs.getComponent("spigot.actionbar.offline",
                            "POS:"+pos,
                            "LEN:"+server.getQueue().size(),
                            "SERVER:"+server.getAlias(),
                            "STATUS:"+status
                    ));
                } else {
                    int time = (int) Math.round(pos * main.getTimeBetweenPlayers());
                    player.sendActionBar(msgs.getComponent("spigot.actionbar.online",
                            "POS:"+pos,
                            "LEN:"+server.getQueue().size(),
                            "SERVER:"+server.getAlias(),
                            "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs"))
                    ));
                }

            }
        }
    }

    @Override
    public void sendTitles() {
        if(!main.getConfig().getBoolean("send-title")) return;

        for(QueueServer server : servers) {
            for(QueuePlayer queuePlayer : server.getQueue()) {

                int pos = queuePlayer.getPosition();
                if(pos == 0) {
                    server.removePlayer(queuePlayer);
                    continue;
                }

                AdaptedPlayer player = queuePlayer.getPlayer();
                if(player == null) continue;

                QueueServer singleServer = getSingleServer(player);
                if(singleServer == null || !singleServer.equals(server)) continue;

                String status = Messages.color(main.getMessages().getRawString("placeholders.status."+server.getStatus(player)));

                int time = (int) Math.round(pos * main.getTimeBetweenPlayers());

                Component titleMessage = msgs.getComponent("title.title",
                        "POS:"+pos,
                        "LEN:"+server.getQueue().size(),
                        "SERVER:"+server.getAlias(),
                        "STATUS:"+status,
                        "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs"))
                );
                Component subTitleMessage = msgs.getComponent("title.subtitle",
                        "POS:"+pos,
                        "LEN:"+server.getQueue().size(),
                        "SERVER:"+server.getAlias(),
                        "STATUS:"+status,
                        "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs"))
                );

                Title title = Title.title(titleMessage, subTitleMessage, Title.Times.of(Duration.ZERO, Duration.ofSeconds(2L), Duration.ZERO));
                player.showTitle(title);
            }
        }
    }

    protected final Map<AdaptedPlayer, Long> pausedAntiSpam = new ConcurrentHashMap<>();

    @Override
    public void sendQueueEvents() {
        if(main.getConfig().getBoolean("force-queue-server-target")) {
            List<String> svs = main.getConfig().getStringList("queue-servers");
            for(String s : svs) {
                if(!s.contains(":")) continue;
                String[] parts = s.split(":");
                String fromName = parts[0];
                String toName = parts[1];
                AdaptedServer from = main.getPlatformMethods().getServer(fromName);
                QueueServer to = findServer(toName);
                if(from == null || to == null) continue;
                from.getPlayers().forEach(player -> {
                    if(PauseQueueServer.pausedPlayers.contains(player)) {
                        long lastReminder = pausedAntiSpam.getOrDefault(player, 0L);
                        if(System.currentTimeMillis() - lastReminder > 60e3) { // 60 second cooldown on the reminder messages
                            player.sendMessage(main.getMessages().getComponent("commands.pausequeueserver.reminder"));
                            pausedAntiSpam.put(player, System.currentTimeMillis());
                        }
                        return;
                    }
                    if(!getPlayerQueues(player).contains(to)) {
                        addToQueue(player, to);
                    }
                });
            }
        }
        for (QueueServer s : servers) {
            for (QueuePlayer queuePlayer : s.getQueue()) {
                AdaptedPlayer player =  queuePlayer.getPlayer();
                if (player == null || !player.isConnected()) continue;
                if(player.getServerName() == null) continue;
                main.getPlatformMethods().sendPluginMessage(player, "inqueueevent", "true");
            }
        }
    }

    @Override
    public void sendMessages() {
        try {
            for(QueueServer server : servers) {
                for(QueuePlayer queuePlayer : server.getQueue()) {
                    sendMessage(queuePlayer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(QueuePlayer queuePlayer) {
        AdaptedPlayer player = queuePlayer.getPlayer();
        if(player == null || !player.isConnected()) return;

        QueueServer server = queuePlayer.getQueueServer();

        int pos = queuePlayer.getPosition();
        int len = server.getQueue().size();

        if(!server.isJoinable(player)) {
            String status = server.getStatusString(player);

            if(msgs.getString("status.offline.base").isEmpty()) return;

            player.sendMessage(msgs.getComponent("status.offline.base",
                    "STATUS:"+status,
                    "POS:"+pos,
                    "LEN:"+len,
                    "SERVER:"+server.getAlias()
                    ));
        } else {
            if(msgs.getString("status.online.base").isEmpty()) return;
            int time = (int) Math.round(pos * main.getTimeBetweenPlayers());
            player.sendMessage(msgs.getComponent("status.online.base",
                    "TIME:" + TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs")),
                    "POS:"+pos,
                    "LEN:"+len,
                    "SERVER:"+server.getAlias()
                    ));
        }
    }

    @Override
    public void updateServers() {
        ExecutorService pool = main.getTaskManager().getServersUpdateExecutor();
        if (pool instanceof ThreadPoolExecutor && main.getConfig().getBoolean("pinger-debug")) {
            main.getLogger().info("[pinger] Server update thread pool has "
                    +((ThreadPoolExecutor) pool).getActiveCount()+" threads");
        }
        try {
            for(AdaptedServer server : main.getPlatformMethods().getServers()) {
                pool.submit(() -> server.ping(main.getConfig().getBoolean("pinger-debug"), main.getLogger()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public QueueServer findServer(String name) {
        return findServer(name, servers);
    }

    public QueueServer findServer(String name, List<QueueServer> servers) {
        for(QueueServer server : servers) {
            if(server == null) continue;
            if(server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    @Override
    public void sendPlayers() {
        sendPlayers(null);
    }


    final ConcurrentHashMap<AdaptedPlayer, Long> sendingNowAntiSpam = new ConcurrentHashMap<>();
    final Map<QueuePlayer, Integer> sendingAttempts = new WeakHashMap<>();

    @Override
    public void sendPlayers(QueueServer queueServer) {
        List<QueueServer> sendingServers;
        if(queueServer == null) {
            sendingServers = new ArrayList<>(servers);
        } else {
            sendingServers = Collections.singletonList(queueServer);
        }

        for(QueueServer server : sendingServers) {
            for(QueuePlayer queuePlayer : server.getQueue()) {
                if(queuePlayer.getPlayer() != null) continue;
                if(main.getLogic().playerDisconnectedTooLong(queuePlayer)) {
                    server.removePlayer(queuePlayer);
                }
            }

            if(!server.isOnline()) continue;
            if(server.getQueue().size() == 0) continue;

            Debug.info("should send when back online: " + !server.isGroup() + " && " + main.getConfig().getBoolean("send-all-when-back-online") + " && " + server.getServers().get(0).justWentOnline());
            if(!server.isGroup() && main.getConfig().getBoolean("send-all-when-back-online") && server.getServers().get(0).justWentOnline()) {
                for(QueuePlayer p : server.getQueue()) {

                    AdaptedPlayer player = p.getPlayer();
                    if(player == null) continue;

                    AdaptedServer selected = server.getIdealServer(player);

                    if(selected == null) {
                        main.getLogger().severe("Could not find ideal server for server '"+server.getName()+"'!");
                        continue;
                    }

                    if(selected.isFull() && !selected.canJoinFull(p.getPlayer())) continue;

                    player.sendMessage(msgs.getComponent("status.sending-now", "SERVER:"+server.getAlias()));
                    Debug.info("Calling player.connect for " + player.getName() + "(send when back online)");
                    player.connect(selected);
                }
                continue;
            }

            QueuePlayer nextQueuePlayer = server.getQueue().get(0);
            AdaptedPlayer nextPlayer = nextQueuePlayer.getPlayer();


            // If the first person int the queue is offline or already in the server, find the next online player in the queue
            int i = 0;
            while((nextPlayer == null || server.getServerNames().contains(nextPlayer.getServerName())) && i < server.getQueue().size()) {
                if(nextPlayer != null) { // Remove them if they are already in the server
                    server.removePlayer(nextQueuePlayer);
                    if(server.getQueue().size() > i) {
                        nextQueuePlayer = server.getQueue().get(i);
                        nextPlayer = nextQueuePlayer.getPlayer();
                    } else {
                        nextPlayer = null;
                        break;
                    }
                } else {
                    i++;
                    if(i > server.getQueue().size()-1) {
                        break;
                    }
                    nextQueuePlayer = server.getQueue().get(i);
                    nextPlayer = nextQueuePlayer.getPlayer();
                }
            }

            if(nextPlayer == null) continue; // None of the players in the queue are online

            AdaptedServer selected = server.getIdealServer(nextPlayer);
            if(selected == null) {
                main.getLogger().severe("Could not find ideal server for server/group '"+server.getName()+"'");
                continue;
            }

            if(selected.isWhitelisted() && !selected.getWhitelistedPlayers().contains(nextPlayer.getUniqueId())) continue;

            if(!server.canAccess(nextPlayer)) continue;

            if(selected.isFull() && !selected.canJoinFull(nextPlayer)) continue;

            if(main.getConfig().getBoolean("enable-bypasspaused-permission")) {
                if(server.isPaused() && !nextPlayer.hasPermission("ajqueue.bypasspaused")) continue;
            } else if(server.isPaused()) { continue; }

            int tries = sendingAttempts.getOrDefault(nextQueuePlayer, 0);
            int maxTries = main.getConfig().getInt("max-tries");
            if(tries >= maxTries && maxTries > 0) {
                server.removePlayer(nextQueuePlayer);
                sendingAttempts.remove(nextQueuePlayer);
                nextPlayer.sendMessage(msgs.getComponent("max-tries-reached", "SERVER:"+server.getAlias()));
                continue;
            }
            tries++;
            sendingAttempts.put(nextQueuePlayer, tries);

            if(!sendingNowAntiSpam.containsKey(nextPlayer)) {
                sendingNowAntiSpam.put(nextPlayer, (long) 0);
            }
            if(System.currentTimeMillis() - sendingNowAntiSpam.get(nextPlayer) >= 5000) {
                nextPlayer.sendMessage(msgs.getComponent("status.sending-now", "SERVER:"+server.getAlias()));
                if(main.getConfig().getBoolean("send-title")) {
                    nextPlayer.showTitle(Title.title(
                            main.getMessages().getComponent(
                                    "title.sending-now.title",
                                    "SERVER:"+server.getAlias()
                            ),
                            main.getMessages().getComponent(
                                    "title.sending-now.subtitle",
                                    "SERVER:"+server.getAlias()
                            ),
                            Title.Times.of(Duration.ZERO, Duration.ofSeconds(2L), Duration.ZERO)
                    ));
                }
                sendingNowAntiSpam.put(nextPlayer, System.currentTimeMillis());
            }


            server.setLastSentTime(System.currentTimeMillis());
            Debug.info("calling nextPlayer.connect on " + nextPlayer.getName());
            nextPlayer.connect(selected);
            selected.addPlayer();
            Debug.info(selected.getName()+" player count is now set to "+ selected.getPlayerCount());
        }
    }

    @Override
    public ImmutableList<QueuePlayer> findPlayerInQueues(AdaptedPlayer p) {
        List<QueuePlayer> srs = new ArrayList<>();
        for(QueueServer s : servers) {
            QueuePlayer player = s.findPlayer(p);
            if(player != null) {
                srs.add(player);
            }
        }
        return ImmutableList.copyOf(srs);
    }

    @Override
    public ImmutableList<QueuePlayer> findPlayerInQueuesByName(String name) {
        List<QueuePlayer> srs = new ArrayList<>();
        for(QueueServer s : servers) {
            QueuePlayer player = s.findPlayer(name);
            if(player != null) {
                srs.add(player);
            }
        }
        return ImmutableList.copyOf(srs);
    }

    @Override
    public ImmutableList<QueueServer> getPlayerQueues(AdaptedPlayer p) {
        List<QueueServer> srs = new ArrayList<>();
        for(QueueServer s : servers) {
            QueuePlayer player = s.findPlayer(p);
            if(player != null) {
                srs.add(s);
            }
        }
        return ImmutableList.copyOf(srs);
    }

    @Override
    public void clear(AdaptedPlayer player) {
        for (AdaptedPlayer next : sendingNowAntiSpam.keySet()) {
            if(!next.equals(player)) continue;
            sendingNowAntiSpam.remove(next);
        }
    }

    @Override
    public Map<QueuePlayer, Integer> getSendingAttempts() {
        return sendingAttempts;
    }
}
