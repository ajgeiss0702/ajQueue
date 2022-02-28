package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import us.ajg0702.queue.api.EventHandler;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.players.QueuePlayerImpl;
import us.ajg0702.queue.common.utils.Debug;
import us.ajg0702.utils.common.TimeUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventHandlerImpl implements EventHandler {

    final QueueMain main;
    public EventHandlerImpl(QueueMain main) {
        this.main = main;
    }

    @Override
    public void handleMessage(AdaptedPlayer recievingPlayer, byte[] data) {
        IBaseCommand moveCommand = main.getPlatformMethods().getCommands().get(0);
        IBaseCommand leaveCommand = main.getPlatformMethods().getCommands().get(1);
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        try {
            String subchannel = in.readUTF();

            if(subchannel.equals("ack")) {
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "ack", "yes, im here");
            }

            if(subchannel.equals("queue")) {
                String rawData = in.readUTF();
                String[] args = new String[1];
                args[0] = rawData;
                moveCommand.execute(new PlayerSender(recievingPlayer), args);
            }
            if(subchannel.equals("massqueue")) {
                String inData = in.readUTF();
                String[] parts = inData.split(",");
                for(String part : parts) {
                    String[] pparts = part.split(":");
                    if(pparts.length < 2) continue;
                    String pname = pparts[0];
                    String pserver = pparts[1];
                    AdaptedPlayer p = main.getPlatformMethods().getPlayer(pname);
                    String[] args = new String[1];
                    args[0] = pserver;
                    moveCommand.execute(new PlayerSender(p), args);
                }
            }
            if(subchannel.equals("queuename")) {
                QueueServer server = main.getQueueManager().getSingleServer(recievingPlayer);
                String name = main.getMessages().getString("placeholders.position.none");
                if(server != null) {
                    name = server.getAlias();
                }
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "queuename", name);
            }
            if(subchannel.equals("position")) {
                QueueServer server = main.getQueueManager().getSingleServer(recievingPlayer);
                String pos = main.getMessages().getString("placeholders.position.none");
                if(server != null) {
                    pos = server.getQueue().indexOf(server.findPlayer(recievingPlayer))+1+"";
                }
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "position", pos);
            }
            if(subchannel.equals("positionof")) {
                QueueServer server = main.getQueueManager().getSingleServer(recievingPlayer);
                String pos = main.getMessages().getString("placeholders.position.none");
                if(server != null) {
                    pos = server.getQueue().size()+"";
                }
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "positionof", pos);
            }
            if(subchannel.equals("estimated_time")) {
                QueueServer server = main.getQueueManager().getSingleServer(recievingPlayer);

                int time;
                String timeString;
                if(server != null) {
                    QueuePlayer queuePlayer = server.findPlayer(recievingPlayer);
                    time = (int) Math.round(queuePlayer.getPosition() * main.getTimeBetweenPlayers());
                    timeString = TimeUtils.timeString(
                            time,
                            main.getMessages().getString("format.time.mins"),
                            main.getMessages().getString("format.time.secs")
                    );
                } else {
                    timeString = main.getMessages().getString("placeholders.estimated_time.none");
                }
                main.getPlatformMethods().sendPluginMessage(
                        recievingPlayer,
                        "estimated_time",
                        timeString
                        );
            }
            if(subchannel.equals("inqueue")) {
                QueueServer server = main.getQueueManager().getSingleServer(recievingPlayer);
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "inqueue", (server != null)+"");
            }
            if(subchannel.equals("queuedfor")) {
                String srv = in.readUTF();
                QueueServer server = main.getQueueManager().findServer(srv);
                if(server == null) return;
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "queuedfor", srv, server.getQueue().size()+"");
            }
            if(subchannel.equals("leavequeue")) {
                String[] args = new String[1];
                try {
                    args[0] = in.readUTF();
                } catch(Exception ignored) {}
                leaveCommand.execute(new PlayerSender(recievingPlayer), args);
            }

        } catch (IOException e1) {
            main.getLogger().warning("An error occured while reading data from spigot side:");
            e1.printStackTrace();
        }
    }

    @Override
    public void onPlayerJoin(AdaptedPlayer player) {
        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            if (main.getUpdater().isUpdateAvailable() && player.hasPermission("ajqueue.manage.update")) {
                player.sendMessage(main.getMessages().getComponent("updater.update-available"));
            }
        }).start();

        ImmutableList<QueuePlayer> queues = main.getQueueManager().findPlayerInQueues(player);
        for(QueuePlayer queuePlayer : queues) {
            queuePlayer.setPlayer(player);
        }
        if(queues.size() > 0) {
            main.getQueueManager().sendMessage(main.getQueueManager().getSingleServer(player).findPlayer(player));
        }
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
            }
        }


        String serverName = player.getServerName();
        List<String> svs = main.getConfig().getStringList("queue-servers");
        for(String s : svs) {
            if(!s.contains(":")) continue;
            String[] parts = s.split(":");
            String from = parts[0];
            QueueServer to = main.getQueueManager().findServer(parts[1]);
            if(from.equalsIgnoreCase(serverName) && to != null) {
                main.getQueueManager().addToQueue(player, to);
            }
        }

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
