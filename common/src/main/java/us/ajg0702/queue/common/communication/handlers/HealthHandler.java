package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.communication.MessageHandler;

public class HealthHandler extends MessageHandler {
    public HealthHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        AdaptedServer server = player.getCurrentServer();
        if(server == null) return null;

        int i = 0;
        for (String part : data.split(";")) {
            if(i++ > 2) {
                main.getLogger().warn("Dropping extra too-long health message from " + player.getName());
                break;
            }
            String[] split = part.split("=");
            if(split.length < 2) continue;
            String key =  split[0].trim();
            String value = split[1].trim();
            if(key.equals("tps") || key.equals("mspt")) {
                double valueD = Double.parseDouble(value);
                if(key.equals("tps")) {
                    server.setTPS(valueD);
                } else {
                    server.setMSPT(valueD);
                }
            }
        }

        return null;
    }
}
