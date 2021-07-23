package us.ajg0702.queue.common;

import com.google.common.collect.ImmutableList;
import us.ajg0702.queue.api.EventHandler;
import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.players.QueuePlayerImpl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventHandlerImpl implements EventHandler {

    QueueMain main;
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
                main.getPlatformMethods().sendPluginMessage(recievingPlayer, "queuename", main.getQueueManager().getQueuedName(recievingPlayer));
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
        }
    }
}
