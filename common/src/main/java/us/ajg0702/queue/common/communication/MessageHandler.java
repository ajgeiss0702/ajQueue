package us.ajg0702.queue.common.communication;

import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;

public abstract class MessageHandler {
    protected final QueueMain main;

    public MessageHandler(QueueMain main) {
        this.main = main;
    }

    public abstract ComResponse handleMessage(AdaptedPlayer player, String data);
}
