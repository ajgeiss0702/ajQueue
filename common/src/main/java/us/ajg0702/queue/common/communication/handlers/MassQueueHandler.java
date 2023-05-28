package us.ajg0702.queue.common.communication.handlers;

import us.ajg0702.queue.api.commands.IBaseCommand;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.commands.commands.PlayerSender;
import us.ajg0702.queue.common.QueueMain;
import us.ajg0702.queue.api.communication.ComResponse;
import us.ajg0702.queue.common.communication.MessageHandler;

public class MassQueueHandler extends MessageHandler {
    private final IBaseCommand moveCommand;

    public MassQueueHandler(QueueMain main) {
        super(main);
        moveCommand = main.getPlatformMethods().getCommands().get(0);
    }

    @Override
    public ComResponse handleMessage(AdaptedPlayer player, String data) {
        String[] parts = data.split(",");
        for(String part : parts) {
            String[] playerParts = part.split(":");
            if(playerParts.length < 2) continue;
            String playerName = playerParts[0];
            String targetServer = playerParts[1];
            AdaptedPlayer p = main.getPlatformMethods().getPlayer(playerName);
            String[] args = new String[1];
            args[0] = targetServer;
            moveCommand.execute(new PlayerSender(p), args);
        }
        return null;
    }
}
