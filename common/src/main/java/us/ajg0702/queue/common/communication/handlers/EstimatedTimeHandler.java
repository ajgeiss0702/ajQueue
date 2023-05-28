package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;
import us.ajg0702.utils.common.TimeUtils;

public class EstimatedTimeHandler extends MessageHandler {

    public EstimatedTimeHandler(QueueMain main) {
        super(main);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        QueueServer server = main.getQueueManager().getSingleServer(player);

        int time;
        String timeString;
        if(server != null) {
            QueuePlayer queuePlayer = server.findPlayer(player);
            time = (int) Math.round(queuePlayer.getPosition() * main.getTimeBetweenPlayers());
            timeString = TimeUtils.timeString(
                    time,
                    main.getMessages().getString("format.time.mins"),
                    main.getMessages().getString("format.time.secs")
            );
        } else {
            timeString = main.getMessages().getString("placeholders.estimated_time.none");
        }
        return ComResponse
                .from("estimated_time")
                .id(player.getUniqueId())
                .with(timeString);
    }
}
