package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.communication.MessageHandler;

public class PlayerStatusHandler extends MessageHandler {
    public PlayerStatusHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueueServer server = main.getQueueManager().findServer(data);
        if(server == null) {
            return ComResponse
                    .from("playerstatus")
                    .id(data)
                    .with("invalid_server");
        }
        if(!player.isConnected() || player.getServerName() == null) return null;
        return ComResponse
                .from("playerstatus")
                .id(player.getUniqueId() + data)
                .with(
                        main.getMessages().getRawString(
                                "placeholders.status." + server.getStatus(player)
                        )
                );
    }
}
