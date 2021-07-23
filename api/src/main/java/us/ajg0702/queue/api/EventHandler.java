package us.ajg0702.queue.api;

import us.ajg0702.queue.api.players.AdaptedPlayer;

public interface EventHandler {

    void handleMessage(AdaptedPlayer reciever, byte[] data);

    void onPlayerJoin(AdaptedPlayer player);

    void onPlayerLeave(AdaptedPlayer player);
}
