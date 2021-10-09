package us.ajg0702.queue.api.queues;

import us.ajg0702.queue.api.players.AdaptedPlayer;
import us.ajg0702.queue.api.server.AdaptedServer;

public interface Balancer {
    AdaptedServer getIdealServer(AdaptedPlayer player);
}
