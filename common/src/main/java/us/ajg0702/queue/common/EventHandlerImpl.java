package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.EventHandler;
import us.ajg0702.queue.api.events.SuccessfulSendEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.commands.commands.manage.PauseQueueServer;
import us.ajg0702.queue.commands.commands.queue.QueueCommand;
import us.ajg0702.queue.common.communication.CommunicationManager;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.utils.Debug;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventHandlerImpl implements EventHandler {

    final QueueMain main;
    CommunicationManager communicationManager;
    public EventHandlerImpl(QueueMain main) {
        this.main = main;
    }

    @Override
    public void handleMessage(AdaptedPlayer receivingPlayer, byte[] data) {
        if(!receivingPlayer.isConnected()) return;
        if(communicationManager == null) {
            communicationManager = new CommunicationManager(main);
        }
        try {
            communicationManager.handle(receivingPlayer, data);
        } catch (IOException e) {
            main.getLogger().warning("An error occurred while reading data from spigot side:", e);
        }
    }

    @Override
    public void onPlayerJoin(AdaptedPlayer player) {
        if(player.hasPermission("ajqueue.manage.update")) {
            main.getTaskManager().runLater(() -> {
                if (main.getUpdater().isUpdateAvailable() && !main.getUpdater().isAlreadyDownloaded()) {
                    player.sendMessage(main.getMessages().getComponent("updater.update-available"));
                }
            }, 2, TimeUnit.SECONDS);
        }

        ImmutableList<QueuePlayer> queues = main.getQueueManager().findPlayerInQueues(player);
        for(QueuePlayer queuePlayer : queues) {
            queuePlayer.setPlayer(player);
        }
        if(queues.size() > 0) {
            main.getQueueManager().sendMessage(main.getQueueManager().getSingleServer(player).findPlayer(player));
        }

        main.serverTimeManager.playerChanged(player);
    }

    @Override
    public void onPlayerLeave(AdaptedPlayer player) {
        ImmutableList<QueuePlayer> queues = main.getQueueManager().findPlayerInQueues(player);
        for(QueuePlayer queuePlayer : queues) {
            ((QueuePlayerImpl) queuePlayer).setLeaveTime(System.currentTimeMillis());
            List<String> svs = main.getConfig().getStringList("queue-servers");
            for(String s : svs) {
                if(!s.contains(":")) continue;
                String[] parts = s.split(":");
                String from = parts[0];
                if(queuePlayer.getQueueServer().getServerNames().contains(from)) {
                    queuePlayer.getQueueServer().removePlayer(queuePlayer);
                }
            }
        }
        main.getQueueManager().clear(player);
        QueueCommand.cooldowns.remove(player);
        main.serverTimeManager.removePlayer(player);
    }

    @Override
    public void onPlayerJoinServer(AdaptedPlayer player) {
        ImmutableList<QueuePlayer> alreadyqueued = main.getQueueManager().findPlayerInQueues(player);
        for(QueuePlayer queuePlayer : alreadyqueued) {
            QueueServer server = queuePlayer.getQueueServer();
            int pos = queuePlayer.getPosition();
            if((pos <= 1 && server.getServerNames().contains(player.getServerName())) || main.getConfig().getBoolean("remove-player-on-server-switch")) {
                server.removePlayer(player);
                server.setLastSentTime(System.currentTimeMillis());
                main.getQueueManager().getSendingAttempts().remove(queuePlayer);
                main.getTaskManager().runNow(() -> {
                    main.call(new SuccessfulSendEvent(queuePlayer, player.getCurrentServer()));
                });
            }
        }

        if(main.getConfig().getBoolean("include-server-switch-in-cooldown")) {
            QueueCommand.cooldowns.put(player, System.currentTimeMillis());
        }


        if(!PauseQueueServer.pausedPlayers.contains(player)) {
            String serverName = player.getServerName();
            List<String> svs = main.getConfig().getStringList("queue-servers");
            for(String s : svs) {
                if(!s.contains(":")) continue;
                String[] parts = s.split(":");
                String from = parts[0];
                QueueServer to = main.getQueueManager().findServer(parts[1]);
                if(
                        from.equalsIgnoreCase(serverName) && to != null &&
                            (
                                    !main.getConfig().getBoolean("require-queueserver-permission") ||
                                            player.hasPermission("ajqueue.queueserver." + to.getName())
                            )
                ) {
                    int delay = Math.min(main.getConfig().getInt("queue-server-delay"), 15000);
                    Runnable task = () -> {
                        if(to.getServers().contains(player.getCurrentServer())) return;
                        main.getQueueManager().addToQueue(player, to);
                    };

                    Debug.info("Delaying queue-server by " + delay);

                    if(delay > 0) {
                        main.getTaskManager().executor.schedule(task, delay, TimeUnit.MILLISECONDS);
                    } else {
                        task.run();
                    }
                }
            }
        }

        main.serverTimeManager.playerChanged(player);

    }

    @Override
    public void onServerKick(AdaptedPlayer player, @NotNull AdaptedServer from, Component reason, boolean moving) {

        if(!player.isConnected()) return;

        String plainReason = PlainTextComponentSerializer.plainText().serialize(reason);
        
        Debug.info(player.getName()+" kicked! Moving: "+moving+" from: "+from.getName()+" plainReason: "+plainReason    );

        if(!moving && main.getConfig().getBoolean("send-fail-debug")) {
            main.getLogger().warning("Failed to send "+player.getName()+" to "+from.getName()+". Kicked with reason: "+plainReason);
        }

        ImmutableList<QueueServer> queuedServers = main.getQueueManager().getPlayerQueues(player);
        if(!queuedServers.contains(main.getQueueManager().findServer(from.getName())) && main.getConfig().getBoolean("auto-add-to-queue-on-kick")) {

            List<String> reasons = main.getConfig().getStringList("auto-add-kick-reasons");
            boolean shouldqueue = false;
            for(String kickReason : reasons) {
                if(plainReason.toLowerCase().contains(kickReason.toLowerCase())) {
                    shouldqueue = true;
                    break;
                }
            }

            if(shouldqueue || reasons.isEmpty()) {
                main.getTaskManager().runLater(() -> {
                    if(!player.isConnected()) return;

                    String toName = from.getName();
                    player.sendMessage(main.getMessages().getComponent("auto-queued", "SERVER:"+toName));
                    main.getQueueManager().addToQueue(player, toName);
                }, (long) (main.getConfig().getDouble("auto-add-to-queue-on-kick-delay")*1000), TimeUnit.MILLISECONDS);
                return;
            }

        }

        for(QueueServer server : queuedServers) {
            if(!(server.getServerNames().contains(from.getName()))) continue;
            QueuePlayer queuePlayer = server.findPlayer(player);
            if(queuePlayer.getPosition() != 1) continue;
            List<String> kickReasons = main.getConfig().getStringList("kick-reasons");
            boolean kickPlayer = main.getConfig().getBoolean("kick-kicked-players");
            if(kickPlayer) {
                Debug.info("Initially kicking player");
                List<String> svs = main.getConfig().getStringList("queue-servers");
                boolean found = false;
                for(String s : svs) {
                    if(!s.contains(":")) continue;
                    String[] parts = s.split(":");
                    String fromName = parts[0];
                    QueueServer toServer = main.getQueueManager().findServer(parts[1]);
                    if(toServer == null) continue;
                    Debug.info("fromName equals: "+fromName.equalsIgnoreCase(player.getServerName())+" ("+fromName+" = "+player.getServerName()+") toServer equals: "+toServer.equals(server));
                    if(fromName.equalsIgnoreCase(player.getServerName()) && toServer.equals(server)) {
                        found = true;
                    }
                }
                kickPlayer = found;
            }
            Debug.info("Kick player: "+kickPlayer);

            for(String kickReason : kickReasons) {
                if(plainReason.toLowerCase().contains(kickReason.toLowerCase())) {
                    server.removePlayer(queuePlayer);
                    if(kickPlayer) {
                        player.kick(reason);
                    }
                    break;
                }
            }
        }
    }
}
