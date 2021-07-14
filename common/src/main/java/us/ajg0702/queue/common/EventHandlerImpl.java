package us.ajg0702.queue.common;

import us.ajg0702.queue.api.EventHandler;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.utils.bungee.BungeeUtils;

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
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        try {
            String subchannel = in.readUTF();

            if(subchannel.equals("queue")) {
                String rawData = in.readUTF();
                String[] args = new String[1];
                args[0] = rawData;
                main.getPlatformMethods().getCommands().get(0).execute(new , args);
                //man.addToQueue(player, data);

            }
            if(subchannel.equals("massqueue")) {
                String data = in.readUTF();
                String[] parts = data.split(",");
                for(String part : parts) {
                    String[] pparts = part.split(":");
                    if(pparts.length < 2) continue;
                    String pname = pparts[0];
                    String pserver = pparts[1];
                    ProxiedPlayer p = ProxyServer.getInstance().getPlayer(pname);
                    String[] args = new String[1];
                    args[0] = pserver;
                    moveCommand.execute(p, args);
                }
            }
            if(subchannel.equals("queuename")) {
                BungeeUtils.sendCustomData(player, "queuename", aliases.getAlias(man.getQueuedName(player)));
            }
            if(subchannel.equals("position")) {
                QueueServer server = man.getSingleServer(player);
                String pos = msgs.getString("placeholders.position.none");
                if(server != null) {
                    pos = server.getQueue().indexOf(player)+1+"";
                }
                BungeeUtils.sendCustomData(player, "position", pos);
            }
            if(subchannel.equals("positionof")) {
                QueueServer server = man.getSingleServer(player);
                String pos = msgs.getString("placeholders.position.none");
                if(server != null) {
                    pos = server.getQueue().size()+"";
                }
                BungeeUtils.sendCustomData(player, "positionof", pos);
            }
            if(subchannel.equals("inqueue")) {
                QueueServer server = man.getSingleServer(player);
                BungeeUtils.sendCustomData(player, "inqueue", (server != null)+"");
            }
            if(subchannel.equals("queuedfor")) {
                String srv = in.readUTF();
                QueueServer server = man.findServer(srv);
                if(server == null) return;
                BungeeUtils.sendCustomData(player, "queuedfor", srv, server.getQueue().size()+"");
            }
            if(subchannel.equals("leavequeue")) {
                String arg = "";
                try {
                    arg = in.readUTF();
                } catch(Exception ignored) {}
                getProxy().getPluginManager().dispatchCommand(player, "leavequeue"+arg);
            }

        } catch (IOException e1) {
            getLogger().warning("An error occured while reading data from spigot side:");
            e1.printStackTrace();
        }
    }
}
