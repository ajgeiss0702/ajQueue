package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class PositionOfHandler extends MessageHandler {


    public PositionOfHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueueServer server = main.getQueueManager().getSingleServer(player);
        Integer size = null;
        String noneMessage = null;
        if(server != null) {
            size = server.getQueue().size();
        } else {
            noneMessage = main.getMessages().getString("placeholders.position.none");
        }
        return ComResponse
                .from("positionof")
                .id(player.getUniqueId())
                .with(size)
                .noneMessage(noneMessage);
    }
}
