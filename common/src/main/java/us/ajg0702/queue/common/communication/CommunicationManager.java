package us.ajg0702.queue.common.communication;

import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.common.communication.handlers.*;
import us.ajg0702.queue.common.utils.MapBuilder;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class CommunicationManager {
    private final QueueMain main;
    Map<String, MessageHandler> handlers;

    public CommunicationManager(QueueMain main) {
        this.main = main;

        handlers = new MapBuilder<>(
            "ack", new AckHandler(main),

                "queue", new QueueHandler(main),
                "massqueue", new MassQueueHandler(main),
                "leavequeue", new LeaveQueueHandler(main),

                "queuename", new QueueNameHandler(main),
                "position", new PositionHandler(main),
                "positionof", new PositionOfHandler(main),
                "estimated_time", new EstimatedTimeHandler(main),
                "inqueue", new InQueueHandler(main),
                "queuedfor", new QueuedForHandler(main),
                "status", new StatusHandler(main),
                "playerstatus", new PlayerStatusHandler(main),
                "serverqueue", new ServerQueueHandler(main)
        );
    }

    public void handle(AdaptedPlayer receivingPlayer, byte[] data) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        String subChannel = in.readUTF();

        MessageHandler handler = handlers.get(subChannel);

        if(handler == null) {
            main.getLogger().warn("Invalid sub-channel " + subChannel);
            return;
        }

        ComResponse response = handler.handleMessage(receivingPlayer, in.readUTF());

        if(response == null) return;

        main.getPlatformMethods().sendPluginMessage(
                receivingPlayer,
                s(response.getFrom()),
                s(response.getIdentifier()),
                s(response.getResponse()),
                s(response.getNoneMessage())
                );
    }

    private String s(String s) {
        return s + "";
    }
}
