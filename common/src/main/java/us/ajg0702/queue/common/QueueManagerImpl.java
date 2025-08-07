package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.events.BuildServersEvent;
import us.ajg0702.queue.api.events.PreQueueEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.premium.Logic;
import us.ajg0702.queue.api.queueholders.QueueHolder;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.queues.QueueType;
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
import java.util.function.Supplier;

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

        BuildServersEvent buildServersEvent = new BuildServersEvent(main.getPlatformMethods().getServers());
        main.call(buildServersEvent);

        for(AdaptedServer server : buildServersEvent.getServers()) {
            QueueServer previousServer = findServer(server.getName());
            List<QueuePlayer> previousStandardPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllStandardPlayers();
            List<QueuePlayer> previousExpressPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllExpressPlayers();
            if(!previousStandardPlayers.isEmpty() || !previousExpressPlayers.isEmpty()) {
                Debug.info("Adding "+previousStandardPlayers.size()+"+"+previousExpressPlayers.size()+" players back to the queue for "+server.getName());
            }
            QueueServer queueServer = new QueueServerImpl(server.getName(), main, server, previousStandardPlayers, previousExpressPlayers);
            if(previousServer != null) {
                queueServer.setPaused(previousServer.isPaused());
                queueServer.setLastSentTime(previousServer.getLastSentTime());
            }
            result.add(queueServer);
        }

        // Mirror the logic from below regarding server groups, just using the ones supplied from the event
        for (Map.Entry<String, List<AdaptedServer>> groups : buildServersEvent.groupEntrySet()) {
            String groupName = groups.getKey();
            if (findServer(groupName, result) != null) {
                main.getLogger().warning("The name of a group ('"+groupName+"') cannot be the same as the name of a server!");
                continue;
            }

            List<AdaptedServer> groupServers = groups.getValue();
            if (groupServers.isEmpty()) {
                main.getLogger().warning("Server group '"+groupName+"' has no servers! Ignoring it.");
                continue;
            }

            QueueServer previousServer = main.getQueueManager().findServer(groupName);
            List<QueuePlayer> previousStandardPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllStandardPlayers();
            List<QueuePlayer> previousExpressPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllExpressPlayers();
            if(!previousStandardPlayers.isEmpty() || !previousExpressPlayers.isEmpty()) {
                Debug.info("Adding "+previousStandardPlayers.size()+"+"+previousExpressPlayers.size()+" players back to the queue for "+groupName);
            }
            result.add(new QueueServerImpl(groupName, main, groupServers, previousStandardPlayers, previousExpressPlayers));
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
            String[] serversRaw = groupRaw.split(":")[1].split(",");

            if(findServer(groupName, result) != null) {
                main.getLogger().warning("The name of a group ('"+groupName+"') cannot be the same as the name of a server!");
                continue;
            }

            List<AdaptedServer> groupServers = new ArrayList<>();

            for(String serverRaw : serversRaw) {
                QueueServer found = findServer(serverRaw, result);
                if(found == null) {
                    main.getLogger().warning("Could not find server named '"+serverRaw+"' in servergroup '"+groupName+"'!");
                    continue;
                }
                if(found.isGroup()) continue;

                groupServers.add(found.getServers().get(0));
            }

            if(groupServers.isEmpty()) {
                main.getLogger().warning("Server group '"+groupName+"' has no servers! Ignoring it.");
                continue;
            }

            QueueServer previousServer = main.getQueueManager().findServer(groupName);
            List<QueuePlayer> previousStandardPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllStandardPlayers();
            List<QueuePlayer> previousExpressPlayers = previousServer == null ? new ArrayList<>() : previousServer.getQueueHolder().getAllExpressPlayers();
            if(!previousStandardPlayers.isEmpty() || !previousExpressPlayers.isEmpty()) {
                Debug.info("Adding "+previousStandardPlayers.size()+"+"+previousExpressPlayers.size()+" players back to the queue for "+groupName);
            }

            result.add(new QueueServerImpl(groupName, main, groupServers, previousStandardPlayers, previousExpressPlayers));
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

        if(!server.isGroup() || !main.getConfig().getBoolean("allow-group-requeue"))  {
            List<AdaptedServer> notInServers = new ArrayList<>(server.getServers());
            notInServers.removeIf(adaptedServer -> !adaptedServer.getName().equals(player.getServerName()));
            if(notInServers.size() > 0) {
                player.sendMessage(msgs.getComponent("errors.already-connected", "SERVER:"+server.getAlias()));
                return false;
            }
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

        QueuePlayer queuePlayer;
        AdaptedServer ideal = server.getIdealServer(player);
        if(main.isPremium()) {
            queuePlayer = main.getLogic().priorityLogic(server, player, ideal);
        } else {
            int priority = player.hasPermission("ajqueue.priority") ||
                    player.hasPermission("ajqueue.serverpriority."+server.getName()) ? 1 : 0;
            priority = Math.max(priority, Logic.getUnJoinablePriorities(server, ideal, player) > 0 ? 1 : 0);
            int maxOfflineTime = player.hasPermission("ajqueue.stayqueued") ? 60 : 0;
            queuePlayer = new QueuePlayerImpl(player, server, priority, maxOfflineTime, QueueType.STANDARD);
            List<QueuePlayer> list = server.getQueueHolder().getAllStandardPlayers();
            if(
                    priority == 1 &&
                    server.getQueueHolder().getStandardQueueSize() > 0
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


        int pos = queuePlayer.getPosition();
        int len = queuePlayer.isInExpressQueue() ?
                server.getQueueHolder().getExpressQueueSize() :
                server.getQueueHolder().getStandardQueueSize();

        boolean sentInstantly = canSendInstantly(player, server);
        boolean hasBypass = main.getLogic().hasAnyBypass(player, server.getName());

        String express = queuePlayer.isInExpressQueue() ?
                main.getMessages().getString("actionbar.express") :
                main.getMessages().getString("actionbar.non-express");

        if(sentInstantly) {
            if(!hasBypass) {
                sendPlayers(server);
            }
            if(!msgs.isEmpty("status.now-in-empty-queue")) {
                player.sendMessage(msgs.getComponent("status.now-in-empty-queue",
                        "POS:"+pos,
                        "LEN:"+len,
                        "SERVER:"+server.getAlias(),
                        "EXPRESS:"+express
                ));
            }
        } else {
            player.sendMessage(
                    msgs.getComponent("status.now-in-queue",
                            "POS:"+pos,
                            "LEN:"+len,
                            "SERVER:"+server.getAlias(),
                            "SERVERNAME:"+server.getName(),
                            "EXPRESS:"+express
                    )
            );
            if(main.getConfig().getBoolean("enable-priority-messages")) {
                for(String rawPriorityMessage : main.getConfig().getStringList("priority-messages")) {
                    List<String> parts = new ArrayList<>(Arrays.asList(rawPriorityMessage.split(":")));
                    if(parts.size() == 1) continue;
                    String level = parts.remove(0);
                    String messageRaw = String.join(":", parts);
                    if((level.equals("*") && queuePlayer.getPriority() > 0) || level.equals(queuePlayer.getPriority()+"")) {
                        player.sendMessage(main.getMessages().toComponent(messageRaw.replaceAll("\\{PRIORITY}", queuePlayer.getPriority()+"")));
                    }
                }
            }
        }

        if(!server.isJoinable(player)) {
            sendMessage(queuePlayer);
        }
        main.getPlatformMethods().sendPluginMessage(player, "player-joined-queue", player.getUniqueId().toString());
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
    public boolean canSendInstantly(AdaptedPlayer player, QueueServer queueServer) {
        boolean isJoinable = queueServer.isJoinable(player);
        boolean sizeGood = (
                main.getLogic().isPremium() && player.hasPermission("ajqueue.express."+queueServer.getName()) ?
                    queueServer.getQueueHolder().getExpressQueueSize() :
                    queueServer.getQueueHolder().getStandardQueueSize()
                )
                <= 1 && isJoinable;
        boolean timeGood = !main.getConfig().getBoolean("check-last-player-sent-time") || queueServer.getLastSentTime() > Math.floor(main.getTimeBetweenPlayers() * 1000);
        boolean alwaysSendInstantly = main.getConfig().getStringList("send-instantly").contains(queueServer.getName());
        boolean hasBypass = main.getLogic().hasAnyBypass(player, queueServer.getName());

        boolean sentInstantly = isJoinable && (sizeGood || hasBypass) && (alwaysSendInstantly || timeGood || hasBypass);
        Debug.info("should send instantly (" + sentInstantly + "): " + isJoinable + " && (" + sizeGood + " || " + hasBypass + ") && (" + alwaysSendInstantly + " || " + timeGood + " || " + hasBypass + ")");
        return sentInstantly;
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
    public QueuePlayer getSingleQueuePlayer(AdaptedPlayer player) {
        ImmutableList<QueuePlayer> queued = findPlayerInQueues(player);
        if(queued.isEmpty()) {
            return null;
        }
        QueuePlayer selected = queued.get(0);

        if(main.getConfig().getString("multi-server-queue-pick").equalsIgnoreCase("last")) {
            selected = queued.get(queued.size()-1);
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
            for(QueuePlayer queuePlayer : server.getQueueHolder().getAllPlayers()) {

                int pos = queuePlayer.getPosition();
                int len = queuePlayer.getQueueType() == QueueType.STANDARD ?
                        server.getQueueHolder().getStandardQueueSize() :
                        server.getQueueHolder().getExpressQueueSize();
                if(pos == 0) {
                    server.removePlayer(queuePlayer);
                    continue;
                }

                AdaptedPlayer player = queuePlayer.getPlayer();
                if(player == null) continue;

                String status = server.getStatusString(player);

                QueueServer singleServer = getSingleServer(player);
                if(singleServer == null || !singleServer.equals(server)) continue;

                String express = queuePlayer.isInExpressQueue() ?
                        main.getMessages().getString("actionbar.express") :
                        main.getMessages().getString("actionbar.non-express");

                if(!server.isJoinable(player)) {
                    player.sendActionBar(msgs.getComponent("actionbar.offline",
                            "POS:"+pos,
                            "LEN:"+len,
                            "SERVER:"+server.getAlias(),
                            "STATUS:"+status,
                            "EXPRESS:"+express
                    ));
                } else {
                    int time = (int) Math.round(pos * main.getTimeBetweenPlayers());
                    player.sendActionBar(msgs.getComponent("actionbar.online",
                            "POS:"+pos,
                            "LEN:"+len,
                            "SERVER:"+server.getAlias(),
                            "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs")),
                            "EXPRESS:"+express
                    ));
                }

            }
        }
    }

    @Override
    public void sendTitles() {
        if(!main.getConfig().getBoolean("send-title")) return;

        for(QueueServer server : servers) {
            for(QueuePlayer queuePlayer : server.getQueueHolder().getAllPlayers()) {

                int pos = queuePlayer.getPosition();
                if(pos == 0) {
                    server.removePlayer(queuePlayer);
                    continue;
                }
                int len = queuePlayer.getQueueType() == QueueType.STANDARD ?
                        server.getQueueHolder().getStandardQueueSize() :
                        server.getQueueHolder().getExpressQueueSize();

                AdaptedPlayer player = queuePlayer.getPlayer();
                if(player == null) continue;

                QueueServer singleServer = getSingleServer(player);
                if(singleServer == null || !singleServer.equals(server)) continue;

                String status = Messages.color(main.getMessages().getRawString("placeholders.status."+server.getStatus(player)));

                int time = (int) Math.round(pos * main.getTimeBetweenPlayers());

                Component titleMessage = msgs.getComponent("title.title",
                        "POS:"+pos,
                        "LEN:"+len,
                        "SERVER:"+server.getAlias(),
                        "STATUS:"+status,
                        "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs"))
                );
                Component subTitleMessage = msgs.getComponent("title.subtitle",
                        "POS:"+pos,
                        "LEN:"+len,
                        "SERVER:"+server.getAlias(),
                        "STATUS:"+status,
                        "TIME:"+ TimeUtils.timeString(time, msgs.getString("format.time.mins"), msgs.getString("format.time.secs"))
                );

                Title title = Title.title(titleMessage, subTitleMessage, Title.Times.times(Duration.ZERO, Duration.ofSeconds(2L), Duration.ZERO));
                player.showTitle(title);
            }
        }
    }

    protected final Map<AdaptedPlayer, Long> pausedAntiSpam = new ConcurrentHashMap<>();
    private boolean skipPriorityCheck = true;

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
                    long lastSwitch = main.getServerTimeManager().getLastServerChange(player);
                    int delay = Math.min(Math.max(main.getConfig().getInt("queue-server-delay"), 0), 3000);
                    if(System.currentTimeMillis() - lastSwitch < delay + 1000 || !player.getCurrentServer().equals(from)) {
                        return;
                    }
                    if(
                            !getPlayerQueues(player).contains(to) &&
                                    (
                                            !main.getConfig().getBoolean("require-queueserver-permission") ||
                                                    player.hasPermission("ajqueue.queueserver." + to.getName())
                                    )
                    ) {
                        addToQueue(player, to);
                    }
                });
            }
        }
        for (QueueServer s : servers) {
            for (QueuePlayer queuePlayer : s.getQueueHolder().getAllPlayers()) {
                AdaptedPlayer player = queuePlayer.getPlayer();
                if (player == null || !player.isConnected()) continue;
                if(player.getServerName() == null) continue;
                main.getPlatformMethods().sendPluginMessage(player, "inqueueevent", "true");
            }
        }
        if(main.getConfig().getBoolean("re-check-priority")) {
            if(skipPriorityCheck) {
                skipPriorityCheck = false;
            } else {
                for (QueueServer server : servers) {
                    for (QueuePlayer queuePlayer : server.getQueueHolder().getAllPlayers()) {
                        if(queuePlayer.getPlayer() == null) continue;
                        AdaptedPlayer player = queuePlayer.getPlayer();
                        AdaptedServer ideal = server.getIdealServer(player);

                        int currentHighestPriority = main.getLogic().getHighestPriority(server, ideal, player);
                        if(queuePlayer.getPriority() >= currentHighestPriority) continue;

                        player.sendMessage(main.getMessages().getComponent("status.priority-increased"));

                        server.removePlayer(queuePlayer);
                        addToQueue(player, server);
                    }
                }
            }
        }
    }

    @Override
    public void sendMessages() {
        try {
            for(QueueServer server : servers) {
                for(QueuePlayer queuePlayer : server.getQueueHolder().getAllPlayers()) {
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
        int len = queuePlayer.getQueueType() == QueueType.STANDARD ?
                server.getQueueHolder().getStandardQueueSize() :
                server.getQueueHolder().getExpressQueueSize();

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
            // Create a 'set' of AdaptedServer by server name
            Map<String, AdaptedServer> serverMap = new HashMap<>();
            for (QueueServer qServer : servers) {
                qServer.getServers().forEach(server -> serverMap.put(server.getName(), server));
            }
            // Ping each server (registered in buildServers and affected by BuildServersEvent)
            for (AdaptedServer server : serverMap.values()) {
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
    final Map<QueuePlayer, Long> makeRoomAntispam = new WeakHashMap<>();

    @Override
    public synchronized void sendPlayers(QueueServer queueServer) {
        List<QueueServer> sendingServers;
        if(queueServer == null) {
            sendingServers = new ArrayList<>(servers);
        } else {
            sendingServers = Collections.singletonList(queueServer);
        }

        for(QueueServer server : sendingServers) {
            QueueHolder queueHolder = server.getQueueHolder();
            for(QueuePlayer queuePlayer : queueHolder.getAllPlayers()) {
                if(queuePlayer.getPlayer() != null) continue;
                if(main.getLogic().playerDisconnectedTooLong(queuePlayer)) {
                    Debug.info("Removing " + queuePlayer.getName() + " due to them being disconnected too long");
                    server.removePlayer(queuePlayer);
                }
            }

            if(!server.isOnline()) continue;

            QueueType lastSend = server.getLastQueueSend();

            boolean express;
            if(lastSend == QueueType.EXPRESS) {
                express = queueHolder.getStandardQueueSize() <= 0;
            } else {
                express = queueHolder.getExpressQueueSize() >= 0;
            }

            ((QueueServerImpl) server).setLastQueueSend(express ? QueueType.EXPRESS : QueueType.STANDARD);

            Debug.info("should send when back online: " + !server.isGroup() + " && " + main.getConfig().getBoolean("send-all-when-back-online") + " && " + server.getServers().get(0).justWentOnline());
            if(!server.isGroup() && main.getConfig().getBoolean("send-all-when-back-online") && server.getServers().get(0).justWentOnline()) {
                List<QueuePlayer> expressPlayers = server.getQueueHolder().getAllExpressPlayers();
                List<QueuePlayer> standardPlayers = server.getQueueHolder().getAllStandardPlayers();
                List<QueuePlayer> players = new ArrayList<>();
                // alternate between standard and express players, even in send-all-when-back-online
                for (int i = 0; i < Math.max(expressPlayers.size(), standardPlayers.size()); i++) {
                    if(i < expressPlayers.size()) {
                        players.add(expressPlayers.get(i));
                    }
                    if(i < standardPlayers.size()) {
                        players.add(standardPlayers.get(i));
                    }
                }
                for(QueuePlayer p : players) {

                    AdaptedPlayer player = p.getPlayer();
                    if(player == null) continue;

                    AdaptedServer selected = server.getIdealServer(player);

                    if(selected == null) {
                        main.getLogger().severe("Could not find ideal server for server '"+server.getName()+"'!");
                        continue;
                    }

                    if(
                            (selected.isFull() && !selected.canJoinFull(player)) ||
                                    (server.isManuallyFull() && !AdaptedServer.canJoinFull(player, server.getName()))
                    ) continue;

                    player.sendMessage(msgs.getComponent("status.sending-now", "SERVER:"+server.getAlias()));
                    Debug.info("Calling QueuePlayer.connect for " + player.getName() + "(send when back online)");
                    // Use QueuePlayer.connect which will fire the PreConnectEvent then call player.connect
                    p.connect(selected);
                }
                continue;
            }

            if((express ? queueHolder.getExpressQueueSize() : queueHolder.getStandardQueueSize()) <= 0) continue;

            QueuePlayer nextQueuePlayer = express ?
                    queueHolder.getAllExpressPlayers().get(0) :
                    queueHolder.getAllStandardPlayers().get(0);
            AdaptedPlayer nextPlayer = nextQueuePlayer.getPlayer();

            Supplier<Integer> queueSize = () -> express ?
                    queueHolder.getExpressQueueSize() :
                    queueHolder.getStandardQueueSize();



            // If the first person int the queue is offline or already in the server, find the next online player in the queue
            int i = 0;
            List<String> excludableServers = new ArrayList<>(server.getServerNames());
            if(nextQueuePlayer.getInitialServer() != null) excludableServers.remove(nextQueuePlayer.getInitialServer().getName());
            while((nextPlayer == null || excludableServers.contains(nextPlayer.getServerName())) && i < queueSize.get()) {
                if(nextPlayer != null) { // Remove them if they are already in the server
                    server.removePlayer(nextQueuePlayer);
                    if(queueSize.get() > i) {
                        nextQueuePlayer = express ?
                                queueHolder.getAllExpressPlayers().get(i) :
                                queueHolder.getAllStandardPlayers().get(i);
                        nextPlayer = nextQueuePlayer.getPlayer();
                    } else {
                        nextPlayer = null;
                        break;
                    }
                } else {
                    i++;
                    if(i > queueSize.get()-1) {
                        break;
                    }
                    nextQueuePlayer = express ?
                            queueHolder.getAllExpressPlayers().get(i) :
                            queueHolder.getAllStandardPlayers().get(i);
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

            if(
                    (
                            (selected.isFull() && !selected.canJoinFull(nextPlayer)) ||
                            (server.isManuallyFull() && !AdaptedServer.canJoinFull(nextPlayer, server.getName()))
                    ) &&
                            !(
                                    nextPlayer.hasPermission("ajqueue.make-room") &&
                                            main.getConfig().getBoolean("enable-make-room-permission") &&
                                            (!server.isGroup() || server.isManuallyFull()) // only use make-room on groups if the server is manually full
                            )
            ) continue;


            // ajqueue.make-room logic
            if(
                    (
                            (selected.isFull() && !selected.canJoinFull(nextPlayer)) ||
                                    (server.isManuallyFull() && !AdaptedServer.canJoinFull(nextPlayer, server.getName()))
                    ) &&
                            main.getConfig().getBoolean("enable-make-room-permission") &&
                            nextPlayer.hasPermission("ajqueue.make-room") &&
                            (!server.isGroup() || server.isManuallyFull()) && // only use make-room on groups if the server is manually full
                            ( // don't make room more than the minimum ping time
                                    System.currentTimeMillis() - makeRoomAntispam.getOrDefault(nextQueuePlayer, 0L)
                                            >= (main.getConfig().getDouble("minimum-ping-time") * 1e3)
                            )
            ) {
                makeRoomAntispam.put(nextQueuePlayer, System.currentTimeMillis());
                List<AdaptedPlayer> players = selected.getPlayers();

                // first, we need to find what the lowest priority on the server is
                int lowestPriority = Integer.MAX_VALUE;
                for (AdaptedPlayer player : players) {
                    int priority = main.getLogic().getHighestPriority(server, selected, player);
                    if(priority < lowestPriority) lowestPriority = priority;
                }

                boolean kickLongest = main.getConfig().getBoolean("make-room-kick-longest-playtime");

                long selectedTime = kickLongest ? Long.MAX_VALUE : 0;
                AdaptedPlayer selectedPlayer = null;
                for (AdaptedPlayer player : players) {
                    int priority = main.getLogic().getHighestPriority(server, selected, player);
                    if(priority > lowestPriority) continue; // don't select players with higher priorities
                    long switchTime = main.getServerTimeManager().getLastServerChange(player);
                    if(selectedPlayer == null) {
                        selectedPlayer = player;
                        selectedTime = switchTime;
                        continue;
                    }
                    if(kickLongest) {
                        if(switchTime < selectedTime) {
                            selectedTime = switchTime;
                            selectedPlayer = player;
                        }
                    } else {
                        if(switchTime > selectedTime) {
                            selectedTime = switchTime;
                            selectedPlayer = player;
                        }
                    }
                }


                if(selectedPlayer == null) {
                    main.getLogger().warn(
                            "Unable to find player to kick from " + selected.getName() + " " +
                                    "to let " + nextPlayer.getName() + "join!"
                    );
                } else {
                    Debug.info(
                            "Selected " + selectedPlayer.getName() + " " +
                                    "to make room for " + nextPlayer.getName() + " in " + selected.getName()
                    );
                    String kickToName = main.getConfig().getString("make-room-kick-to");
                    AdaptedServer kickTo = main.getPlatformMethods().getServer(kickToName);
                    if(kickTo == null) {
                        main.getLogger().warn(
                                "Unable to make room due to '" + kickToName + "' not existing! " +
                                        "Please configure make-room-kick-to in the config"
                        );
                        boolean isAdmin = nextPlayer.hasPermission("ajqueue.manage");
                        nextPlayer.sendMessage(
                                main.getMessages().getComponent(
                                        isAdmin ? "errors.make-room-failed.admin" : "errors.make-room-failed.player"
                                )
                        );
                    } else {
                        // Use direct connect method, as opposed to the QueuePlayer connect
                        selectedPlayer.connect(kickTo);
                        selectedPlayer.sendMessage(main.getMessages().getComponent("errors.kicked-to-make-room"));

                        if(main.getTimeBetweenPlayers() >= 1d) {
                            nextPlayer.sendMessage(main.getMessages().getComponent("status.making-room"));
                        }

                        continue;
                    }
                }
            }

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
                            Title.Times.times(Duration.ZERO, Duration.ofSeconds(2L), Duration.ZERO)
                    ));
                }
                sendingNowAntiSpam.put(nextPlayer, System.currentTimeMillis());
            }


            server.setLastSentTime(System.currentTimeMillis());
            Debug.info("calling nextQueuePlayer.connect on " + nextPlayer.getName());
            // Use QueuePlayer.connect which will fire the PreConnectEvent then call player.connect
            nextQueuePlayer.connect(selected);
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
