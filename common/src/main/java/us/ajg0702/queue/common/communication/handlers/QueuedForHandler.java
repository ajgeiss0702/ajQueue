package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class QueuedForHandler extends MessageHandler {

    public QueuedForHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueueServer server = main.getQueueManager().findServer(data);
        if(server == null) {
            return ComResponse
                    .from("queuedfor")
                    .id(data)
                    .with("invalid_server");
        }
        return ComResponse
                .from("queuedfor")
                .id(data)
                .with(server.getQueue().size());
    }
}
