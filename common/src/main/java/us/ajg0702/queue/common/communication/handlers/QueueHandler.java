package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class QueueHandler extends MessageHandler {
    private final IBaseCommand moveCommand;

    public QueueHandler(QueueMain main) {
        super(main);
        moveCommand = main.getPlatformMethods().getCommands().get(0);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        String[] args = new String[1];
        args[0] = data;
        moveCommand.execute(new PlayerSender(player), args);
        return null;
    }
}
