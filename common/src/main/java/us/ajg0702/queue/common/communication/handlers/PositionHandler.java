package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class PositionHandler extends MessageHandler {

    public PositionHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueuePlayer queuePlayer = main.getQueueManager().getSingleQueuePlayer(player);
        Integer pos = null;
        String noneMessage = null;
        if(queuePlayer != null) {
            pos = queuePlayer.getPosition();
        } else {
            noneMessage = main.getMessages().getString("placeholders.position.none");
        }
        return ComResponse
                .from("position")
                .id(player.getUniqueId())
                .with(pos)
                .noneMessage(noneMessage);
    }
}
