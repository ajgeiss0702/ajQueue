package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class QueueNameHandler extends MessageHandler {

    public QueueNameHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueueServer server = main.getQueueManager().getSingleServer(player);
        String name = null;
        String none = null;
        if(server != null) {
            name = server.getAlias();
        } else {
            none = main.getMessages().getString("placeholders.position.none");
        }
        return ComResponse
                .from("queuename")
                .id(player.getUniqueId())
                .with(name)
                .noneMessage(none);
    }
}
