package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class LeaveQueueHandler extends MessageHandler {
    IBaseCommand leaveCommand;

    public LeaveQueueHandler(QueueMain main) {
        super(main);
        leaveCommand = main.getPlatformMethods().getCommands().get(1);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        String[] args = new String[1];
        args[0] = data;
        leaveCommand.execute(new PlayerSender(player), args);
        return null;
    }
}
